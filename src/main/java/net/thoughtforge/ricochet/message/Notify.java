package net.thoughtforge.ricochet.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

import net.thoughtforge.ricochet.Node;
import net.thoughtforge.ricochet.NodeRef;

public class Notify extends AbstractMessage {

	private static final long serialVersionUID = -7220494023650490073L;

	private NodeRef nodeRef;
	
	public Notify(NodeRef nodeRef) {
		
		this.nodeRef = nodeRef;
	}
	
	public NodeRef getNodeRef() {
		
		return nodeRef;
	}

	@Override
	public void handleMessage(Node node) throws Exception {

		node.handleMessage(this);
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder(this)
				.append("nodeRef", nodeRef)
				.toString();
	}
}
