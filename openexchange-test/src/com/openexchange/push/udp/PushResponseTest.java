package com.openexchange.push.udp;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import junit.framework.TestCase;

public class PushResponseTest extends TestCase {
	
	private static String host = "localhost";
	
	private static int port = 44335;
	
	private DatagramSocket datagramSocket = null;
	
	private int folderId = 0;
	
	private int contextId = 0;
	
	public PushResponseTest(String name) {
		super(name);
	}
	
	public PushResponseTest(String name, DatagramSocket datagramSocket, int folderId, int contextId) {
		super(name);
		this.datagramSocket = datagramSocket;
		this.folderId = folderId;
		this.contextId = contextId;
	}
	
	public void testPushResponse() throws Exception {
		if (datagramSocket == null) {
			throw new Exception("DatagramSocket is null");
		}

		getResponse(datagramSocket, folderId, contextId);
	}
	
	public static void getResponse(DatagramSocket datagramSocket, int folderId, int contextId) throws Exception {
		byte[] responseByte = new byte[1024];
		
		DatagramPacket datagramPacket = new DatagramPacket(responseByte, responseByte.length);
		datagramSocket.receive(datagramPacket);
		responseByte = datagramPacket.getData();
		byte[] responseData = new byte[datagramPacket.getLength()];
		System.arraycopy(responseByte, 0, responseData, 0, responseData.length);
		
		assertEquals("unexpected response", folderId + "\1", new String(responseData));
	}
}
