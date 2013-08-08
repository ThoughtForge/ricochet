package net.thoughtforge.ricochet.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

import net.thoughtforge.ricochet.Node;

public class Stabilise extends AbstractMessage {

	private static final long serialVersionUID = -3125088547959178586L;

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
