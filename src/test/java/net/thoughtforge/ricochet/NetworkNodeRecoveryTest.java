package net.thoughtforge.ricochet;

import net.thoughtforge.commons.logging.Log;
import net.thoughtforge.commons.logging.LogLevel;

import org.junit.Test;

import akka.actor.Props;
import akka.testkit.TestActorRef;

public class NetworkNodeRecoveryTest extends AbstractProtocolTest {

	/**
	 * Before test is executed a network of 3 nodes is established.  NodeA leaveClusters the network to
	 * simulate node failure.  The state of NodeB and NodeC is then verified.  NodeA then rejoins
	 * the network and the state of NodeA, NodeB and NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeAFailureAndRecovery() throws Exception {
		// sumulate failure of nodeA
		nodeA.underlyingActor().leave();
		
		// assert nodeB successor=NodeC predecessor=NodeC
		assertNodeSuccessorAndPredecessor(nodeB, nodeC, nodeC);
		
		// assert nodeC successor=NodeB predecessor=NodeB
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeB);
		
		// nodeA rejoins the cluster
		nodeA = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_A_ID, nodeB).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeB);
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeC);
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeA);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeB leaveClusters the network to
	 * simulate node failure.  The state of NodeA and NodeC is then verified.  NodeB then rejoins
	 * the network and the state of NodeA, NodeB and NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.TRACE)
	public void nodeBFailureAndRecovery() throws Exception {
		// sumulate failure of nodeB
		nodeB.underlyingActor().leave();
		
		// assert nodeA successor=NodeC predecessor=NodeC
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeC);
		
		// assert nodeC successor=NodeA predecessor=NodeA
		assertNodeSuccessorAndPredecessor(nodeC, nodeA, nodeA);

		// nodeB rejoins the cluster
		nodeB = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_B_ID, nodeA).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeB);
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeC);
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeA);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeC leaveClusters the network to
	 * simulate node failure.  The state of NodeA and NodeB is then verified.  NodeC then rejoins
	 * the network and the state of NodeA, NodeB and NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.TRACE)
	public void nodeCFailureAndRecovery() throws Exception {
		// sumulate failure of nodeC
		nodeC.underlyingActor().leave();
		
		// assert nodeA successor=NodeB predecessor=NodeB
		assertNodeSuccessorAndPredecessor(nodeA, nodeB, nodeB);
		
		// assert nodeB successor=NodeA predecessor=NodeA
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeA);
		
		// nodeC rejoins the cluster
		nodeC = TestActorRef.create(actorSystem, Props.create(Node.class, NODE_C_ID, nodeB).withDispatcher("akka.actor.default-dispatcher"));
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeB);
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeC);
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeA);
	}
}
