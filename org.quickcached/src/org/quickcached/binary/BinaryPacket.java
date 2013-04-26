package org.quickcached.binary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.HexUtil;
import org.quickcached.QuickCached;
import org.quickcached.Util;

/**
 *
 * @author akshath
 */
public class BinaryPacket {
	private static final Logger logger = Logger.getLogger(BinaryPacket.class.getName());
	
	private Header header;
	private Extras extras;
	
	private String key;
	private String encodedKey;
	private byte[] value;

	/*
	 Byte/     0       |       1       |       2       |       3       |
         /              |               |               |               |
        |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
        +---------------+---------------+---------------+---------------+
       0/ HEADER                                                        /
        /                                                               /
        /                                                               /
        /                                                               /
        +---------------+---------------+---------------+---------------+
      24/ COMMAND-SPECIFIC EXTRAS (as needed)                           /
       +/  (note length in the extras length header field)              /
        +---------------+---------------+---------------+---------------+
       m/ Key (as needed)                                               /
       +/  (note length in key length header field)                     /
        +---------------+---------------+---------------+---------------+
       n/ Value (as needed)                                             /
       +/  (note length is total body length header field, minus        /
       +/   sum of the extras and key length body fields)               /
        +---------------+---------------+---------------+---------------+
        Total 24 bytes
	 */

	public byte[] toBinaryByte() throws IOException {
		try {
			StringBuilder sb = new StringBuilder();

			sb.append(getHeader().encodedString());
			if(getExtras()!=null) sb.append(getExtras().encodedString());
			if(getKey()!=null) sb.append(getEncodedKey());			
			if(getValue()!=null) {
				sb.append(HexUtil.encode(getValue()));
			}

			String encodedValue = sb.toString();
			if(QuickCached.DEBUG) logger.fine("encodedValue: "+encodedValue);
			return HexUtil.decodeToByte(encodedValue);			
		} catch(Exception e) {
			logger.warning("Error: "+e);
			e.printStackTrace();
			throw new IOException("Error: "+e);
		}
	}
	
	public static BinaryPacket parseRequest(RequestHeader header, byte[] bodyData) throws Exception {
		try {
			String hexData = HexUtil.encode(bodyData);
			if(hexData.length()!=header.getTotalBodyLength()*2)
				throw new IllegalArgumentException("Bad Body passed: "+hexData);
			
			BinaryPacket bp = new BinaryPacket();
			bp.setHeader(header);

			int pos = 0;
			if(header.getExtrasLength()>0) {
				Extras extras = new Extras();
				
				if(header.getOpcode().equals("05") || header.getOpcode().equals("06") ||
						header.getOpcode().equals("15") || header.getOpcode().equals("16")) {//incr/decr
					if(header.getExtrasLength()>=8) {//Delta
						extras.setDelta(hexData.substring(0, 16));
						pos = 16;
					}
					if(header.getExtrasLength()>=16) {//InitalValue
						extras.setInitalValue(hexData.substring(pos, pos+16));
						pos = pos + 16;
					}
					if(header.getExtrasLength()>=20) {//Expiration
						extras.setExpiration(hexData.substring(pos, pos+8));
						pos = pos + 8;
					}
				} else {
					if(header.getExtrasLength()>=4) {//flag
						extras.setFlags(hexData.substring(0, 8));
						pos = 8;
					}
					if(header.getExtrasLength()>=8) {//Expiration
						extras.setExpiration(hexData.substring(pos, pos + 8));
						pos = pos + 8;
					}
				}
				bp.setExtras(extras);
			}

			if(header.getKeyLength()>0) {
				int endPos = pos+(header.getKeyLength()*2);
				String hexKey = hexData.substring(pos, endPos);
				pos = endPos;
				bp.setEncodedKey(hexKey);
			}

			int len = header.getExtrasLength()+header.getKeyLength();
			if(header.getTotalBodyLength() > len) {
				String value = hexData.substring(pos);

				bp.setValue(HexUtil.decodeToByte(value));
			}

			return bp;
		} catch(Exception e) {
			logger.warning("Error: "+e);
			throw e;
		}
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Extras getExtras() {
		return extras;
	}

	public void setExtras(Extras extras) {
		this.extras = extras;
	}

	public String getKey() {
		return key;
	}

	public String getEncodedKey() throws UnsupportedEncodingException {
		return HexUtil.encode(key);
	}

	public void setEncodedKey(String encodedKey) throws UnsupportedEncodingException {
		this.encodedKey = encodedKey;
		if(encodedKey!=null) key = HexUtil.decodeToString(encodedKey);
	}

	public void setKey(String key) throws UnsupportedEncodingException {
		this.key = key;
		if(key!=null) encodedKey = HexUtil.encode(key);
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[BinaryPacket {");
		sb.append(getHeader());
		if(getExtras()!=null) sb.append(getExtras());
		sb.append(", Key:");
		if(getKey()!=null) sb.append(getKey());
		sb.append(", Value:");
		if(getValue()!=null) {
			try {
				sb.append(new String(getValue(), HexUtil.getCharset()));
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(BinaryPacket.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		sb.append("}]");
		return sb.toString();
	}

	
}
