package net.thoughtforge.ricochet;

import net.thoughtforge.commons.logging.Log;
import net.thoughtforge.commons.logging.LogLevel;

import org.junit.Test;

public class NetworkNodeFailureTest extends AbstractProtocolTest {

	/**
	 * Before test is executed a network of 3 nodes is established.  NodeA leaves the network to
	 * simulate node failure.  The state of NodeB and NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeAFailure() throws Exception {
		
		// sumulate failure of nodeA
		nodeA.underlyingActor().leave();
		
		// assert nodeB successor=NodeC predecessor=NodeC
		assertNodeSuccessorAndPredecessor(nodeB, nodeC, nodeC);
		
		// assert nodeC successor=NodeB predecessor=NodeB
		assertNodeSuccessorAndPredecessor(nodeC, nodeB, nodeB);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeB leaves the network to
	 * simulate node failure.  The state of NodeA and NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeBFailure() throws Exception {
		
		// sumulate failure of nodeB
		nodeB.underlyingActor().leave();

		// assert nodeA successor=NodeC predecessor=NodeC
		assertNodeSuccessorAndPredecessor(nodeA, nodeC, nodeC);
		
		// assert nodeC successor=NodeA predecessor=NodeA
		assertNodeSuccessorAndPredecessor(nodeC, nodeA, nodeA);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeC leaves the network to
	 * simulate node failure.  The state of NodeA and NodeB is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeCFailure() throws Exception {
		
		// sumulate failure of nodeB
		nodeC.underlyingActor().leave();
		
		// assert nodeA successor=NodeB predecessor=NodeB
		assertNodeSuccessorAndPredecessor(nodeA, nodeB, nodeB);
		
		// assert nodeB successor=NodeA predecessor=NodeA
		assertNodeSuccessorAndPredecessor(nodeB, nodeA, nodeA);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeA and NodeB leave the
	 * network to simulate node failure.  The state of NodeC is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeAAndNodeBFailure() throws Exception {
		
		// sumulate failure of nodeA and nodeB
		nodeA.underlyingActor().leave();
		nodeB.underlyingActor().leave();
		
		// assert nodeC successor=NodeC predecessor=NodeC
		assertNodeSuccessorAndPredecessor(nodeC, nodeC, nodeC);
	}
	
	/**
	 * Before test is executed a network of 3 nodes is established.  NodeA and NodeC leave the
	 * network to simulate node failure.  The state of NodeB is then verified.
	 */
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeAAndNodeCFailure() throws Exception {
		
		// sumulate failure of nodeA and nodeC
		nodeA.underlyingActor().leave();
		nodeC.underlyingActor().leave();

		// assert nodeB successor=NodeB predecessor=NodeB
		assertNodeSuccessorAndPredecessor(nodeB, nodeB, nodeB);
	}
	
	@Test
	@Log(logLevel=LogLevel.DEBUG)
	public void nodeBAndNodeCFailure() throws Exception {
		// sumulate failure of nodeB and nodeC
		nodeB.underlyingActor().leave();
		nodeC.underlyingActor().leave();
		
		// assert nodeA successor=NodeA predecessor=NodeA
		assertNodeSuccessorAndPredecessor(nodeA, nodeA, nodeA);
	}
}
