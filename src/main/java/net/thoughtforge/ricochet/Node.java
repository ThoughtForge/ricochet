package net.thoughtforge.ricochet;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.thoughtforge.commons.logging.Log;
import net.thoughtforge.commons.logging.LogLevel;
import net.thoughtforge.ricochet.message.AbstractMessage;
import net.thoughtforge.ricochet.message.AskForNodeRef;
import net.thoughtforge.ricochet.message.AskForPredecessor;
import net.thoughtforge.ricochet.message.AskForSuccessor;
import net.thoughtforge.ricochet.message.AskForSuccessorList;
import net.thoughtforge.ricochet.message.CheckPredecessor;
import net.thoughtforge.ricochet.message.Notify;
import net.thoughtforge.ricochet.message.Stabilise;
import net.thoughtforge.ricochet.message.StabiliseSuccessorList;
import net.thoughtforge.ricochet.token.Token;
import net.thoughtforge.ricochet.token.TokenFactory;

import org.apache.commons.lang3.builder.ToStringBuilder;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class Node extends UntypedActor {

	private static final Timeout ASK_TIMEOUT = new Timeout(Duration.create(1, "seconds"));
	
	private static final FiniteDuration SCHEDULER_INITIAL_DELAY = Duration.create(1, TimeUnit.SECONDS);
	
	private static final FiniteDuration SCHEDULER_INTERVAL = Duration.create(1, TimeUnit.SECONDS);
	
	private static final int SUCCESSOR_LIST_SIZE = 2;
	
	private NodeRef ref;
	
	private NodeRef predecessorRef;
	
	private NodeRef[] successorList;
	
	private Cancellable stabiliseJob;
	
	private Cancellable stabiliseSuccessorListJob;
	
	/**
	 * n.create()
	 *   predecessor = nil;
	 *   successor = n;
	 */
	public Node(String id) {
	
		ref = new NodeRef(getSelf(), new TokenFactory().createToken(id));
		
		predecessorRef = null;
		
		
		successorList = new NodeRef[SUCCESSOR_LIST_SIZE];
		successorList[0] = ref;
		
		Scheduler scheduler = getContext().system().scheduler();
		stabiliseJob = scheduler.schedule(
				SCHEDULER_INITIAL_DELAY,
				SCHEDULER_INTERVAL,
				ref.getActorRef(),
				new Stabilise(),
				getContext().system().dispatcher(),
				ActorRef.noSender());

		scheduler = getContext().system().scheduler();
		stabiliseJob = scheduler.schedule(
				SCHEDULER_INITIAL_DELAY,
				SCHEDULER_INTERVAL,
				ref.getActorRef(),
				new StabiliseSuccessorList(),
				getContext().system().dispatcher(),
				ActorRef.noSender());
		
		stabiliseSuccessorListJob = scheduler.schedule(
				SCHEDULER_INITIAL_DELAY,
				SCHEDULER_INTERVAL,
				ref.getActorRef(),
				new CheckPredecessor(),
				getContext().system().dispatcher(),
				ActorRef.noSender());
	}
	
	/**
	 * n.join(n')
	 *   predecessor = nill;
	 *   successor = n'.find_successor(n);
	 */
	public Node(String id, ActorRef existingNode) throws Exception {
		
		this(id);
		join(existingNode);
	}
	
	public NodeRef getRef() {
		
		return ref;
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public NodeRef getPredecessorRef() {
		
		return predecessorRef;
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public NodeRef getSuccessorRef() {
		
		return successorList[0];
	}
	
	@Log(logLevel=LogLevel.DEBUG)
	public void onReceive(Object message) throws Exception {
		
		((AbstractMessage) message).handleMessage(this);
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(AbstractMessage message) throws Exception {
		
		unhandled(message);
	}

	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(Notify notify) throws Exception {
		
		notify(notify.getNodeRef());
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(AskForPredecessor askForPredecessor) throws Exception {
		
		if (predecessorRef == null) {
		
			getSender().tell(new NodeRef(null, null), ref.getActorRef());
		} else {

			getSender().tell(predecessorRef, ref.getActorRef());
		}
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(AskForSuccessor askForSuccessor) throws Exception {
		
		Token token = askForSuccessor.getToken();
		
		getSender().tell(findSuccessor(token), ref.getActorRef());
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(AskForNodeRef askForNodeRef) throws Exception {
		
		getSender().tell(ref, ref.getActorRef());
	}

	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(AskForSuccessorList askForSuccessorList) throws Exception {
		
		getSender().tell(successorList, ref.getActorRef());
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(Stabilise stabilise) throws Exception {
		
		stabilise();
	}

	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(StabiliseSuccessorList stabilisesuccessorList) throws Exception {
		
		stabiliseSuccessorList();
	}
	
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void handleMessage(CheckPredecessor checkPredecessor) throws Exception {
		
		checkPredecessor();
	}
	
	/**
	 * n.notify(n')
	 *   if (predecessor is nil or n' in (predecessor, n))
	 *     predecessor = n';
	 */
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void notify(NodeRef nodeRef) throws Exception {
	
		if (predecessorRef == null || nodeRef.getToken().isBetween(predecessorRef.getToken(), ref.getToken())) {
			
			predecessorRef = nodeRef;
		}
	}
	
	/**
	 * n.stabilize()
	 *   x = successor.predecessor;
	 *   if (x in (n, successor))
	 *     successor = x;
	 *   
	 *   successor.notify(n);
	 */
	@Log(logLevel=LogLevel.DEBUG)
	public void stabilise() throws Exception {
		
		final Future<NodeRef> askForPredecessor = Futures.future(new Callable<NodeRef>() {

			@Override
			public NodeRef call() throws Exception {
				
				Future<Object> askForPredecessor = Patterns.ask(successorList[0].getActorRef(), new AskForPredecessor(), ASK_TIMEOUT);
				
				return (NodeRef) Await.result(askForPredecessor, ASK_TIMEOUT.duration());
			}
			
		}, getContext().system().dispatcher());
				
		askForPredecessor.onSuccess(new OnSuccess<NodeRef>() {

			@Override
			@Log(logLevel=LogLevel.DEBUG, logParameters=true)
			public final void onSuccess(NodeRef nodeRef) {
				if (!nodeRef.isEmpty() && nodeRef.getToken().isBetween(ref.getToken(), successorList[0].getToken())) {
					successorList[0] = nodeRef;
				}
			}
			
		}, getContext().system().dispatcher());
		
		successorList[0].getActorRef().tell(new Notify(ref), ref.getActorRef());
	}
	
	/**
	 * 
	 */
	@Log(logLevel=LogLevel.DEBUG)
	public void stabiliseSuccessorList() {
		
		final Future<NodeRef[]> askForSuccessorList = Futures.future(new Callable<NodeRef[]>() {

			@Override
			public NodeRef[] call() throws Exception {
				
				Future<Object> askForSuccessorList = Patterns.ask(successorList[0].getActorRef(), new AskForSuccessorList(), ASK_TIMEOUT);
				
				return (NodeRef[]) Await.result(askForSuccessorList, ASK_TIMEOUT.duration());
			}
			
		}, getContext().system().dispatcher());
				
		askForSuccessorList.onSuccess(new OnSuccess<NodeRef[]>() {

			@Override
			@Log(logLevel=LogLevel.DEBUG, logParameters=true)
			public final void onSuccess(NodeRef[] successorListReceived) {
				
				for (int i = 1; i < SUCCESSOR_LIST_SIZE; i++) {
					
					successorList[i] = successorListReceived[i - 1];
				}
			}
			
		}, getContext().system().dispatcher());
		
		askForSuccessorList.onFailure(new OnFailure() {

			@Override
			@Log(logLevel=LogLevel.DEBUG)
			public final void onFailure(final Throwable throwable) {
				
				for (int i = 1; i < SUCCESSOR_LIST_SIZE; i++) {
					
					successorList[i - 1] = successorList[i];
				}
				
				successorList[SUCCESSOR_LIST_SIZE - 1] = ref;
			}
			
		}, getContext().system().dispatcher());
	}
	
	/**
	 * n.check_predecessor()
	 *   if (predecessor has failed)
	 *     predecessor = nil;
	 */
	@Log(logLevel=LogLevel.DEBUG)
	public void checkPredecessor() {
		
		if (predecessorRef == null)
			return;
					
		final Future<NodeRef> askForSuccessor = Futures.future(new Callable<NodeRef>() {

			@Override
			public NodeRef call() throws Exception {
				
				Future<Object> askForNodeRef = Patterns.ask(predecessorRef.getActorRef(), new AskForNodeRef(), ASK_TIMEOUT);
				
				return (NodeRef) Await.result(askForNodeRef, ASK_TIMEOUT.duration());
			}
			
		}, getContext().system().dispatcher());
				
		askForSuccessor.onFailure(new OnFailure() {

			@Override
			@Log(logLevel=LogLevel.DEBUG)
			public final void onFailure(final Throwable throwable) {
				
				predecessorRef = null;
			}
			
		}, getContext().system().dispatcher());
	}
	
	/**
	 * n.join(n')
	 *   predecessor = nill;
	 *   successor = n'.find_successor(n);
	 */
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public void join(ActorRef existingNode) throws Exception {
		
		predecessorRef = null;
		
		final Future<Object> askForSuccessor = Patterns.ask(existingNode, new AskForSuccessor(ref.getToken()), ASK_TIMEOUT);
		successorList[0] = (NodeRef) Await.result(askForSuccessor, ASK_TIMEOUT.duration());
	}
	
	@Log(logLevel=LogLevel.DEBUG)
	public void leave() throws Exception {
	
		stabiliseJob.cancel();
		stabiliseSuccessorListJob.cancel();
		
		getContext().stop(getSelf());
	}
	
	/**
	 * n.find_successor(id)
	 *   if (id in (n, successor])
	 *     return successor;
	 *   else
	 *     n' = closest_preceding_node(id);
	 *     return n'.find_successor(id);
	 */
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	public NodeRef findSuccessor(Token token) throws Exception {
		
		if (token.equals(successorList[0].getToken()) || token.isBetween(ref.getToken(), successorList[0].getToken())) {
			
			return successorList[0];
		} else {
			
			final Future<Object> askForSuccessor = Patterns.ask(predecessorRef.getActorRef(), new AskForSuccessor(token), ASK_TIMEOUT);

			return (NodeRef) Await.result(askForSuccessor, ASK_TIMEOUT.duration());
		}
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder(this)
				.append("ref", ref)
				.append("successorList", successorList)
				.append("predecessorRef", predecessorRef)
				.toString();
	}
}
