package net.thoughtforge.ricochet.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

import net.thoughtforge.ricochet.Node;
import net.thoughtforge.ricochet.token.Token;

public class AskForSuccessor extends AbstractMessage {

	private static final long serialVersionUID = 5547917776512651945L;

	private Token token;
	
	public AskForSuccessor(Token token) {
		
		this.token = token;
	}
	
	public Token getToken() {
		
		return token;
	}

	@Override
	public void handleMessage(Node node) throws Exception {
		
		node.handleMessage(this);
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder(this)
				.append("token", token)
				.toString();
	}
}
