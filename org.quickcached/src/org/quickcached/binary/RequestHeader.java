/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quickcached.binary;

import java.util.logging.Logger;
import org.quickcached.HexUtil;
import org.quickcached.Util;

/**
 *
 * @author akshath
 */
public class RequestHeader extends Header {
	private static final Logger logger = Logger.getLogger(RequestHeader.class.getName());

	private String vbucketId;

	public String getVbucketId() {
		return vbucketId;
	}

	public void setVbucketId(String vbucketId) {
		this.vbucketId = vbucketId;
	}

        /*
          Byte/     0       |       1       |       2       |       3       |
         /              |               |               |               |
        |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
        +---------------+---------------+---------------+---------------+
       0| Magic         | Opcode        | Key length                    |
        +---------------+---------------+---------------+---------------+
       4| Extras length | Data type     | vbucket id                    |
        +---------------+---------------+---------------+---------------+
       8| Total body length                                             |
        +---------------+---------------+---------------+---------------+
      12| Opaque                                                        |
        +---------------+---------------+---------------+---------------+
      16| CAS                                                           |
        |                                                               |
        +---------------+---------------+---------------+---------------+
         */
	public static RequestHeader parse(byte rawinput[]) throws Exception {
		try {
			String hexData = HexUtil.encode(rawinput);
			if(hexData.length()!=48) throw new IllegalArgumentException("Bad Header passed: "+hexData);
			
			RequestHeader header = new RequestHeader();
			
			header.setMagic(hexData.substring(0,2));//2
			header.setOpcode(hexData.substring(2,4));//2
			header.setKeyLength( Util.hex2decimal(hexData.substring(4,8)) );//4
			
			header.setExtrasLength( Util.hex2decimal(hexData.substring(8,10)) );//2
			header.setDataType(hexData.substring(10,12));//2
			header.setVbucketId(hexData.substring(12,16));//4

			header.setTotalBodyLength( Util.hex2decimal(hexData.substring(16,24)) );//8
			header.setOpaque(hexData.substring(24,32));//8
			header.setCas(hexData.substring(32));//16

			return header;
		} catch(Exception e) {
			logger.warning("Error: "+e);
			throw e;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[RequestHeader {");
		
		sb.append("Magic:");
		sb.append(getMagic());
		sb.append(", Opcode:");
		sb.append(getOpcode());
		sb.append(", KeyLength:");
		sb.append(getKeyLength());

		sb.append(", ExtrasLength:");
		sb.append(getExtrasLength());
		sb.append(", DataType:");
		sb.append(getDataType());
		sb.append(", VbucketId:");
		sb.append(getVbucketId());

		sb.append(", TotalBodyLength:");
		sb.append(getTotalBodyLength());
		sb.append(", Opaque:");
		sb.append(getOpaque());
		sb.append(", Cas:");
		sb.append(getCas());

		sb.append("}]");
		return sb.toString();
	}
}
