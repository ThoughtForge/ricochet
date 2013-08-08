package net.thoughtforge.ricochet;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import net.thoughtforge.ricochet.token.Token;
import akka.actor.ActorRef;

public class NodeRef implements Serializable {

	private static final long serialVersionUID = 2329859674574857972L;

	private ActorRef actorRef;
	
	private Token token;
	
	public NodeRef(ActorRef actorRef, Token token) {
		
		this.actorRef = actorRef;
		
		this.token = token;
	}
	
	public ActorRef getActorRef() {
		
		return actorRef;
	}
	
	public Token getToken() {
		
		return token;
	}
	
	public boolean isEmpty() {
		
		return (actorRef == null || token == null);
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder(this)
				.append("token", token)
				.toString();
	}
}
