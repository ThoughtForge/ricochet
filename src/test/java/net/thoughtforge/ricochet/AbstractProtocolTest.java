package net.thoughtforge.ricochet;

import static com.jayway.awaitility.Awaitility.await;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.hamcrest.Matchers.*;

import java.util.concurrent.Callable;

import net.thoughtforge.commons.logging.Log;
import net.thoughtforge.commons.logging.LogLevel;
import net.thoughtforge.ricochet.token.Token;

import org.junit.After;
import org.junit.Before;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;

public abstract class AbstractProtocolTest {

	private static final String NODE_A_ID = "http://localhost:8080/network/node/nodeA";
	
	private static final String NODE_B_ID = "http://localhost:8080/network/node/nodeB";
	
	private static final String NODE_C_ID = "http://localhost:8080/network/node/nodeC";
	
	private ActorSystem actorSystem;
	
	protected TestActorRef<Node> nodeA;
	
	protected TestActorRef<Node> nodeB;
	
	protected TestActorRef<Node> nodeC;
	
	/**
	 * nodeA, nodeB and nodeC join the cluster
	 */
	@Log(logLevel=LogLevel.DEBUG, logParameters=true)
	@Before
	public void before() throws Exception {

		actorSystem = ActorSystem.create("Ricochet");
		
		// create a new network with a single node (nodeA)
		nodeA = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_A_ID).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeA, nodeA);
		
		// nodeB joins the cluster
		nodeB = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_B_ID, nodeA).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeB, nodeB);
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeA);
		
		// nodeC joins the cluster
		nodeC = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_C_ID, nodeA).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeB);
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeC);
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeA);
	}
	
	/**
	 * nodeA, nodeB and nodeC leave the cluster
	 */
	@After
	public void after() throws Exception {

		nodeA.underlyingActor().leave();
		
		nodeB.underlyingActor().leave();
		
		nodeC.underlyingActor().leave();
		
		actorSystem.shutdown();
	}
	
	protected void assertNodeSuccessorAndPredecessor(final TestActorRef<Node> nodeRef, TestActorRef<Node> successorRef, TestActorRef<Node> predecessorRef) {
		
		await().atMost(5, SECONDS).until(
				new Callable<Token>() {

					@Override
					public Token call() throws Exception {

						return nodeRef.underlyingActor().getSuccessorRef().getToken();
					}
					
				},
				is(equalTo(successorRef.underlyingActor().getRef().getToken()))
			);
		
		await().atMost(5, SECONDS).until(
				new Callable<Token>() {

					@Override
					public Token call() throws Exception {

						if (nodeRef.underlyingActor().getPredecessorRef() == null)
							return null;
									
						return nodeRef.underlyingActor().getPredecessorRef().getToken();
					}
					
				},
				is(equalTo(predecessorRef.underlyingActor().getRef().getToken()))
			);
	}
}
