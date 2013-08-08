package net.thoughtforge.ricochet.token;

public final class Key {

	byte[] value;

	protected Key(byte[] value) {
		
		this.value = value;
	}

	public boolean isBetween(Key from, Key to) {
		
		if (from.compareTo(to) < 0) {
			
			if (this.compareTo(from) > 0 && this.compareTo(to) < 0) {
				return true;
			}
			
		} else if (from.compareTo(to) > 0) {
			
			if (this.compareTo(to) < 0 || this.compareTo(from) > 0) {
				return true;
			}
			
		} else if (from.equals(to)) {
			
			return true;
			
		}
		
		return false;
	}

	public int compareTo(Key key) {

		for (int i = 0; i < value.length; i++) {
			
			int loperand = (this.value[i] & 0xff);
			int roperand = (key.value[i] & 0xff);
			
			if (loperand != roperand) {
				
				return (loperand - roperand);
			}
		}
		
		return 0;
	}

	public String toString() {
		
		StringBuilder stringBuilder = new StringBuilder();
		if (value.length > 4) {
			
			for (int i = 0; i < value.length; i++) {
				stringBuilder.append(Integer.toString(((int) value[i]) & 0xff) + ".");
			}
		} else {
			
			long n = 0;
			for (int i = value.length-1,j=0; i >= 0 ; i--, j++) {
				n |= ((value[i]<<(8*j)) & (0xffL<<(8*j)));
			}
			stringBuilder.append(Long.toString(n));
		}
		
		return stringBuilder.substring(0, stringBuilder.length() - 1).toString();
	}

	public int getLength() {
		
		return value.length * 8;
	}
	
	public int hashCode() {
		
		return toString().hashCode();
	}
	
	public boolean equals(Key key) {
		
		return compareTo(key) == 0;
	}
}
