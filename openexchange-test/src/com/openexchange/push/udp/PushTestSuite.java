package com.openexchange.push.udp;


import com.clarkware.junitperf.TimedTest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.TimeZone;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PushTestSuite extends TestSuite{
	
	protected static final String localAddress = "localhost";
	
	protected static final int localPort = 33890;
	
	public static Test suite() throws Exception {
		WebConversation webConversation = new WebConversation();
		int userId = GroupUserTest.getUserId(webConversation, "localhost", "offspring", "netline");
		int contextId = GroupUserTest.getContextId(webConversation, "localhost", "offspring", "netline");
		
		int appointmentFolderId = FolderTest.getAppointmentDefaultFolder(webConversation, "localhost", "offspring", "netline").getObjectID();
				
		long maxElapsedTime = 10000;
		
		DatagramSocket datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));
    
		Test registerTest = new RegisterTest("testRegister", datagramSocket, userId, contextId);
        Test registerTimedTest = new TimedTest(registerTest, maxElapsedTime, false);
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("pushTestSuite");
		appointmentObj.setStartDate(new Date());
		appointmentObj.setEndDate(new Date());
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);		
		
		AppointmentTest.insertAppointment(webConversation, appointmentObj, "localhost", "offspring", "netline");
		
		long maxResponseTime = 20000;
		
		Test pushTest = new PushResponseTest("testPushResponse", datagramSocket, appointmentFolderId, contextId);
        Test pushTimedTest = new TimedTest(pushTest, maxResponseTime, false);
		
		TestSuite testSuite = new TestSuite();
		testSuite.addTest(registerTimedTest);
		testSuite.addTest(pushTimedTest);		
		
		return testSuite;
	}
}
