package net.thoughtforge.ricochet.message;

import net.thoughtforge.ricochet.Node;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AskForSuccessorList extends AbstractMessage {

	private static final long serialVersionUID = 6375995973467080706L;

	@Override
	public void handleMessage(Node node) throws Exception {

		node.handleMessage(this);
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder(this)
				.toString();
	}
}
