package com.openexchange.push.udp;


import com.clarkware.junitperf.TimedTest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.webdav.xml.GroupUserTest;
import java.net.DatagramSocket;
import java.net.InetAddress;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PushRegisterTestSuite extends TestSuite{
	
	protected static final String localAddress = "localhost";
	
	protected static final int localPort = 33890;
	
	public static Test suite() throws Exception {
		WebConversation webConversation = new WebConversation();
		int userId = GroupUserTest.getUserId(webConversation, "localhost", "offspring", "netline");
		int contextId = GroupUserTest.getContextId(webConversation, "localhost", "offspring", "netline");

		long maxElapsedTime = 10000;
		
		DatagramSocket datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));
    
		Test testCase = new RegisterTest("testRegister", datagramSocket, userId, contextId);
        Test timedTest = new TimedTest(testCase, maxElapsedTime, false);
		
		return timedTest;
	}
}
