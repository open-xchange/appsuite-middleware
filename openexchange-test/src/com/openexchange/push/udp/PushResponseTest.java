
package com.openexchange.push.udp;

import static org.junit.Assert.assertEquals;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class PushResponseTest {

    private static String host = "localhost";

    private static int port = 44335;

    private DatagramSocket datagramSocket = null;

    private int folderId = 0;

    private int contextId = 0;

    protected static final String localAddress = "localhost";

    protected static final int localPort = 33890;

    @Before
    public void setUp() throws Exception {
        final WebConversation webConversation = new WebConversation();
        final int contextId = GroupUserTest.getContextId(webConversation, "localhost", "offspring", "netline", "defaultcontext");

        final int appointmentFolderId = FolderTest.getAppointmentDefaultFolder(webConversation, "localhost", "offspring", "netline", "defaultcontext").getObjectID();

        this.datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("pushTestSuite");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setShownAs(Appointment.ABSENT);

        AppointmentTest.insertAppointment(webConversation, appointmentObj, "localhost", "offspring", "netline", "defaultcontext");

        this.folderId = appointmentFolderId;
        this.contextId = contextId;
    }

    @Test
    public void testPushResponse() throws Exception {
        if (datagramSocket == null) {
            throw new Exception("DatagramSocket is null");
        }

        getResponse(datagramSocket, folderId, contextId);
    }

    private static void getResponse(final DatagramSocket datagramSocket, final int folderId, final int contextId) throws Exception {
        byte[] responseByte = new byte[1024];

        final DatagramPacket datagramPacket = new DatagramPacket(responseByte, responseByte.length);
        datagramSocket.receive(datagramPacket);
        responseByte = datagramPacket.getData();
        final byte[] responseData = new byte[datagramPacket.getLength()];
        System.arraycopy(responseByte, 0, responseData, 0, responseData.length);

        assertEquals("unexpected response", folderId + "\1", new String(responseData));
    }
}
