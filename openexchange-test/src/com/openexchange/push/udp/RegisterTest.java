package com.openexchange.push.udp;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import junit.framework.TestCase;

public class RegisterTest extends TestCase {
	
	public static final int MAGIC = 1337;
	
	public static final int REGISTER = 1;
	
	public static final int REGISTER_SYNC = 2;
	
	public static final int PUSH_SYNC = 3;
	
	public static final int REMOTE_HOST_REGISTER = 4;
	
	private static String host = "localhost";
	
	private static int port = 44335;
	
	private DatagramSocket datagramSocket = null;
	
	private int userId = 0;
	
	private int contextId = 0;
	
	public RegisterTest(String name) {
		super(name);
	}
	
	public RegisterTest(String name, DatagramSocket datagramSocket, int userId, int contextId) {
		super(name);
		this.datagramSocket = datagramSocket;
		this.userId = userId;
		this.contextId = contextId;
	}
	
	public void testRegister() throws Exception {
		if (datagramSocket == null) {
			throw new Exception("DatagramSocket is null");
		}

		register(datagramSocket, userId, contextId, host, port);
	}
	
	public static void register(DatagramSocket datagramSocket, int userId, int contextId, String host, int port) throws Exception {
		StringBuffer body = new StringBuffer();
		body.append(REGISTER);
		body.append('\1');
		body.append(userId);
		body.append('\1');
		body.append(contextId);
		
		StringBuffer requestData = new StringBuffer();
		requestData.append(MAGIC);
		requestData.append('\1');
		requestData.append(body.length());
		requestData.append('\1');
		requestData.append(body);
		
		byte b[] = requestData.toString().getBytes();
		
		DatagramPacket datagramPacket = new DatagramPacket(b, b.length, InetAddress.getByName(host), port);
		
		datagramSocket.send(datagramPacket);
		
		byte[] responseByte = new byte[1024];
		datagramPacket = new DatagramPacket(responseByte, responseByte.length);
		datagramSocket.receive(datagramPacket);
		responseByte = datagramPacket.getData();
		byte[] responseData = new byte[datagramPacket.getLength()];
		System.arraycopy(responseByte, 0, responseData, 0, responseData.length);
		
		assertEquals("unexpected response", "OK\1", new String(responseData));
	}
}
