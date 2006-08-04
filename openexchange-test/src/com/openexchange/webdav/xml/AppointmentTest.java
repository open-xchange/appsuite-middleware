package com.openexchange.webdav.xml;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class AppointmentTest extends CalendarTest {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNewAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
		saveAppointment(appointmentObj);
	}
	
	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
				
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		saveAppointment(appointmentObj);
	}
	
	public void testUpdateAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = saveAppointment(appointmentObj);
		
		appointmentObj = createAppointmentObject("testUpdateAppointment");
		appointmentObj.setObjectID(objectId);
		
		saveAppointment(appointmentObj);
	}
	
	public void testUpdateAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = saveAppointment(appointmentObj);
		
		appointmentObj = createAppointmentObject("testUpdateAppointment");
		appointmentObj.setObjectID(objectId);
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		saveAppointment(appointmentObj);
	}

	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int objectId = saveAppointment(appointmentObj);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setObjectID(objectId);
		deleteObject(appointmentObj, appointmentFolderId);
	}
	/*
	public void testPropFind() throws Exception {
		listObjects(appointmentFolderId, new Date(0), false);
	}
	 */
	
	public void testPropFindWithModifiedAppointments() throws Exception {
		Date timestamp = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithModifiedAppointments01");
		int objectId01 = saveAppointment(appointmentObj);
		
		appointmentObj = createAppointmentObject("testPropFindWithModifiedAppointments02");
		int objectId02 = saveAppointment(appointmentObj);

		
		Element e_propfind = new Element("propfind", webdav);
		Element e_prop = new Element("prop", webdav);
		
		Element e_folderId = new Element("folder_id", XmlServlet.NS);
		Element e_lastSync = new Element("lastsync", XmlServlet.NS);
		Element e_objectmode = new Element("objectmode", XmlServlet.NS);
		
		e_folderId.addContent(String.valueOf(appointmentFolderId));
		e_lastSync.addContent(String.valueOf(timestamp.getTime()));
		
		e_propfind.addContent(e_prop);
		e_prop.addContent(e_folderId);
		e_prop.addContent(e_lastSync);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_propfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(PROTOCOL + hostName + getURL());
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		assertTrue("check body size", responseByte.length > 0);
		
		bais = new ByteArrayInputStream(responseByte);
		
		doc = new SAXBuilder().build(bais);

		Element rootElement = doc.getRootElement();
		List responseElements = rootElement.getChildren("response", webdav);
		
		boolean found01 = false;
		boolean found02 = false;
		
		for (int a = 0; a < responseElements.size(); a++) {
			Element responseElement = (Element)responseElements.get(a);
			Element href = responseElement.getChild("href", webdav);
			
			if (Integer.valueOf((href.getValue())) == objectId01) {
				found01 = true;
			} else if (Integer.valueOf((href.getValue())) == objectId02) {
				found02 = true;
			} 
		} 
		
		assertTrue("check first element", found01);
		assertTrue("check second element", found02);
	}
	
	/*
	public void testPropFindWithDelete() throws Exception {
		listObjects(appointmentFolderId, new Date(0), false);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithObjectId");
		int objectId = saveAppointment(appointmentObj);
		
		loadObject(objectId);
	}
	 
	 */
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
				
		int objectId = saveAppointment(appointmentObj);
		
		confirmObject(objectId);
	}
	
	protected int saveAppointment(AppointmentObject appointmentObj) throws Exception {
		AppointmentWriter appointmentWriter = new AppointmentWriter(sessionObj);
		Element e_prop = new Element("prop", webdav);
		appointmentWriter.addContent2PropElement(e_prop, appointmentObj, false);
		byte[] b = writeRequest(e_prop);
		return sendPut(b);
	}
	
	private AppointmentObject createAppointmentObject(String title) throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}

	protected String getURL() {
		return appointmentUrl;
	}
}

