
package com.openexchange.push.udp;

import static org.junit.Assert.assertEquals;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

public class PushResponseTest extends AbstractAJAXSession {

    private DatagramSocket datagramSocket = null;

    private int folderId = 0;

    private int contextId = 0;

    protected static final String localAddress = "localhost";

    protected static final int localPort = 33890;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.datagramSocket = new DatagramSocket(localPort, InetAddress.getByName(localAddress));
        this.folderId = this.getClient().getValues().getPrivateAppointmentFolder();
        this.contextId = this.getClient().getValues().getUserId();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("pushTestSuite");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setParentFolderID(this.folderId);
        appointmentObj.setShownAs(Appointment.ABSENT);

        catm.insert(appointmentObj);
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
