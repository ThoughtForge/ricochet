package net.thoughtforge.ricochet.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TokenFactory {

	private final MessageDigest DIGEST;
	
	public TokenFactory() {
		
		try {
			
			DIGEST = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			
			throw new TokenException(noSuchAlgorithmException);
		}
	}
	
	public Token createToken(String id) {
		
		Key key = new Key(DIGEST.digest(id.getBytes()));
		
		return new Token(id, key);
	}
}
