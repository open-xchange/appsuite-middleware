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

	public PushResponseTest(final String name) {
		super(name);
	}

	public PushResponseTest(final String name, final DatagramSocket datagramSocket, final int folderId, final int contextId) {
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

	public static void getResponse(final DatagramSocket datagramSocket, final int folderId, final int contextId) throws Exception {
		byte[] responseByte = new byte[1024];

		final DatagramPacket datagramPacket = new DatagramPacket(responseByte, responseByte.length);
		datagramSocket.receive(datagramPacket);
		responseByte = datagramPacket.getData();
		final byte[] responseData = new byte[datagramPacket.getLength()];
		System.arraycopy(responseByte, 0, responseData, 0, responseData.length);

		assertEquals("unexpected response", folderId + "\1", new String(responseData));
	}
}
