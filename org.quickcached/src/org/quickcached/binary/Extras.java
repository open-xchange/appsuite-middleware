package org.quickcached.binary;

/**
 *
 * @author akshath
 */
public class Extras {
	private String flags;
	private String expiration;

	private String delta;
	private String initalValue;

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getExpiration() {
		return expiration;
	}

	public int getExpirationInSec() {
		if(expiration==null) {
			return 0;
		} else {
			 return Integer.parseInt(expiration, 16);
		}
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Extras {");
		sb.append("Falg:");
		sb.append(getFlags());

		if(getDelta()!=null) {
			sb.append(",Delta:");
			sb.append(getDelta());
		}
		if(getInitalValue()!=null) {
			sb.append(",InitalValue:");
			sb.append(getInitalValue());
		}

		sb.append(", Expiration:");
		sb.append(getExpiration());
		sb.append("}]");
		return sb.toString();
	}

	public String encodedString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFlags());
		if(getExpiration()!=null) sb.append(getExpiration());
		return sb.toString();
	}

	public String getDelta() {
		return delta;
	}

	public long getDeltaInDec() {
		if(delta==null) {
			return 0;
		} else {
			 return Long.parseLong(delta, 16);
		}
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}

	public String getInitalValue() {
		return initalValue;
	}

	public long getInitalValueInDec() {
		if(initalValue==null) {
			return 0;
		} else {
			 return Long.parseLong(initalValue, 16);
		}
	}

	public void setInitalValue(String initalValue) {
		this.initalValue = initalValue;
	}
}
