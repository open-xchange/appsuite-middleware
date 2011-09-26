package com.openexchange.push.udp;


import java.net.DatagramSocket;
import java.net.InetAddress;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.clarkware.junitperf.TimedTest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.webdav.xml.GroupUserTest;

public class PushRegisterTestSuite extends TestSuite{

	protected static final String localAddress = "localhost";

	protected static final int localPort = 33890;

	public static Test suite() throws Exception {
		final WebConversation webConversation = new WebConversation();
		final int userId = GroupUserTest.getUserId(webConversation, "localhost", "offspring", "netline", "defaultcontext");
		final int contextId = GroupUserTest.getContextId(webConversation, "localhost", "offspring", "netline", "defaultcontext");

		final long maxElapsedTime = 10000;

		final DatagramSocket datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));

		final Test testCase = new RegisterTest("testRegister", datagramSocket, userId, contextId);
        final Test timedTest = new TimedTest(testCase, maxElapsedTime, false);

		return timedTest;
	}
}
