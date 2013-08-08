package net.thoughtforge.ricochet.token;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Token {

	private String id;
	
	private Key key;
	
	Token(String id, Key key) {
		
		this.id = id;
		this.key = key;
	}

	public String getId() {
		
		return id;
	}
	
	public boolean isBetween(Token from, Token to) {
		
		return key.isBetween(from.key, to.key);
	}
	
	public int hashCode() {
		
		return key.hashCode();
	}
	
	public boolean equals(Object object) {
		
		if (!(object instanceof Token)) {
			
			return false;
		}
		
		Token token = (Token) object;
		return key.equals(token.key);
	}
	
	public String toString() {
		
		return new ToStringBuilder(this)
			.append("id", id)
			.append("key", key)
			.toString();
	}
}
