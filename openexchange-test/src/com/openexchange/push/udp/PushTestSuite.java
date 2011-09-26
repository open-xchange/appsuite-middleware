package com.openexchange.push.udp;


import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.clarkware.junitperf.TimedTest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class PushTestSuite extends TestSuite{

	protected static final String localAddress = "localhost";

	protected static final int localPort = 33890;

	public static Test suite() throws Exception {
		final WebConversation webConversation = new WebConversation();
		final int userId = GroupUserTest.getUserId(webConversation, "localhost", "offspring", "netline", "defaultcontext");
		final int contextId = GroupUserTest.getContextId(webConversation, "localhost", "offspring", "netline", "defaultcontext");

		final int appointmentFolderId = FolderTest.getAppointmentDefaultFolder(webConversation, "localhost", "offspring", "netline", "defaultcontext").getObjectID();

		final long maxElapsedTime = 10000;

		final DatagramSocket datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));

		final Test registerTest = new RegisterTest("testRegister", datagramSocket, userId, contextId);
        final Test registerTimedTest = new TimedTest(registerTest, maxElapsedTime, false);

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("pushTestSuite");
		appointmentObj.setStartDate(new Date());
		appointmentObj.setEndDate(new Date());
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(Appointment.ABSENT);

		AppointmentTest.insertAppointment(webConversation, appointmentObj, "localhost", "offspring", "netline", "defaultcontext");

		final long maxResponseTime = 20000;

		final Test pushTest = new PushResponseTest("testPushResponse", datagramSocket, appointmentFolderId, contextId);
        final Test pushTimedTest = new TimedTest(pushTest, maxResponseTime, false);

		final TestSuite testSuite = new TestSuite();
		testSuite.addTest(registerTimedTest);
		testSuite.addTest(pushTimedTest);

		return testSuite;
	}
}
