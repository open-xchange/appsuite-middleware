package org.quickcached;

import org.quickserver.net.server.*;

import java.io.*;
import java.util.Date;
import org.quickcached.binary.BinaryPacket;
import org.quickcached.binary.RequestHeader;


public class Data implements ClientData {
	//private static final Logger logger = Logger.getLogger(Data.class.getName());

	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	private long dataRequiredLength;
	private String cmd;
	private String key;
	private String flags;
	private int exptime = -1;//sec
	private String casunique;
	private boolean noreplay;
	
	private int client_mode = -1;//0-text,1=binary

	public String getCommand() throws UnsupportedEncodingException {
		byte input[] = baos.toByteArray();
		String data = new String(input, HexUtil.getCharset());
		int index = data.indexOf("\r\n");
		if(index!=-1) {
			data = data.substring(0, index);
			baos.reset();
			index = index+2;
			baos.write(input, index, input.length-index);
		} else {
			data = null;
		}
		return data;
	}

	public byte[] getDataByte() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte data[] =  baos.toByteArray();
		out.write(data, 0, (int)dataRequiredLength);
		baos.reset();
		int consumed = (int) dataRequiredLength+2;
		baos.write(data, consumed, data.length-consumed);
		return out.toByteArray();
	}

	public boolean isBinaryCommand() throws UnsupportedEncodingException {
		if(client_mode == 2) {
			return true;
		} else if(client_mode == 1) {
			return false;
		}
		
		byte data[] =  baos.toByteArray();
		if(HexUtil.encode(data[0]).equals("80")) {
			client_mode = 2;
			return true;
		} else {
			byte input[] = baos.toByteArray();
			String indata = new String(input, HexUtil.getCharset());
			int index = indata.indexOf("\r\n");
			if(index!=-1) {
				client_mode = 1;
			}
			return false;
		}
	}

	public BinaryPacket getBinaryCommandHeader() throws Exception {
		if(baos.size()<24) return null;

		byte input[] = baos.toByteArray();

		byte headerData[] = new byte[24];
		System.arraycopy(input, 0, headerData, 0, 24);
		RequestHeader header = RequestHeader.parse(headerData);

		int lenToRead = header.getTotalBodyLength();
		if(baos.size()<(24+lenToRead)) {
			//todo may be it not good idea to descard parsed header
			return null;
		}

		byte bodyData[] = new byte[lenToRead];
		System.arraycopy(input, 24, bodyData, 0, lenToRead);

		BinaryPacket packet = BinaryPacket.parseRequest(header, bodyData);
			
		baos.reset();
		int index = lenToRead+24;
		baos.write(input, index, input.length-index);
		
		return packet;
	}

	public boolean isMoreCommandToProcess() {
		if(dataRequiredLength==0 && baos.size()>0) 
			return true;
		else
			return false;
	}

	public boolean isAllDataIn() {
		int size = baos.size();
		if(size>=dataRequiredLength+2) {
			return true;
		}
		return false;
	}

	public void clear() {
		cmd = null;
		key = null;
		flags = null;
		exptime = -1;
		noreplay = false;
		dataRequiredLength = 0;
	}

	public void addBytes(byte data[]) {
		baos.write(data, 0, data.length);
	}


	

	/**
	 * @return the dataRequiredLength
	 */
	public long getDataRequiredLength() {
		return dataRequiredLength;
	}

	/**
	 * @param dataRequiredLength the dataRequiredLength to set
	 */
	public void setDataRequiredLength(long dataRequiredLength) {
		this.dataRequiredLength = dataRequiredLength;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public int getExptime() {
		return exptime;
	}

	public void setExptime(int exptimeInSec) {
		this.exptime = exptimeInSec;
		//todo check 1970 thing
		//60*60*24*30 = 30 days in sec
		if(exptimeInSec>2592000) {
			Date time = new Date(exptimeInSec*1000);//in mili
			this.exptime = (int) (time.getTime() - System.currentTimeMillis())/1000;//in sec
		}
	}

	public boolean isNoreplay() {
		return noreplay;
	}

	public void setNoreplay(boolean noreplay) {
		this.noreplay = noreplay;
	}

	public void setCasUnique(String casunique) {
		this.casunique = casunique;
	}

	public String getCasUnique() {
		return casunique;
	}
}


