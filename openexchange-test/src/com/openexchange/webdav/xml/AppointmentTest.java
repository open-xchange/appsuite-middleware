package com.openexchange.webdav.xml;

import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.OXFolderTools;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class AppointmentTest extends AbstractWebdavTest {
	
	private static String url = "/servlet/calendar";
	
	private static int appointmentFolderId = -1;
	
	private static long startTime = 0;
	
	private static long endTime = 0;
	
	private static final long d7 = 604800000;
	
	private static int userParticipantId2 = -1;
	
	private static int userParticipantId3 = -1;
	
	private static int groupParticipantId1 = 13;
	
	private static int resourceParticipantId1 = -1;
	
	private static boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		appointmentInit();
	}
	
	public void appointmentInit() throws Exception {
		if (isInit) {
			return ;
		}
		
		SessiondConnector sc = SessiondConnector.getInstance();
		
		url = AbstractConfigWrapper.parseProperty(webdavProps, "appointment_url", url);
		appointmentFolderId = OXFolderTools.getCalendarStandardFolder(userId, sessionObj.getContext());
		
		String userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
		String userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");
		
		userParticipantId2 = sc.addSession(userParticipant2, password, "localhost").getUserObject().getId();
		userParticipantId3 = sc.addSession(userParticipant3, password, "localhost").getUserObject().getId();
		
		groupParticipantId1 = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant_id", groupParticipantId1);
		resourceParticipantId1 = AbstractConfigWrapper.parseProperty(webdavProps, "resource_participant_id", resourceParticipantId1);
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		
		startTime = c.getTimeInMillis();
		endTime = startTime + 3600000;
		
		isInit = true;
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
		participants[2] = new UserParticipant();
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
		participants[2] = new UserParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new UserParticipant();
		participants[3].setIdentifier(resourceParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		saveAppointment(appointmentObj);
	}

	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int objectId = saveAppointment(appointmentObj);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setObjectID(objectId);
		deleteAppointment(appointmentObj);
	}
	
	public void testPropFind() throws Exception {
		byte[] b = listAppointments(appointmentFolderId, new Date(0), false);
		sendPropFind(b);
	}

	public void testPropFindWithDelete() throws Exception {
		byte[] b = listAppointments(appointmentFolderId, new Date(0), false);
		sendPropFind(b);
	}
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
				
		int objectId = saveAppointment(appointmentObj);
		
		confirmAppointment(objectId);
	}
	
	protected int saveAppointment(AppointmentObject appointmentObj) throws Exception {
		AppointmentWriter appointmentWriter = new AppointmentWriter(sessionObj);
		Element e_prop = new Element("prop", webdav);
		appointmentWriter.addContent2PropElement(e_prop, appointmentObj, false);
		byte[] b = writeRequest(e_prop);
		return sendPut(b);
	}
	
	protected void deleteAppointment(AppointmentObject appointmentObj) throws Exception {
		Element e_prop = new Element("prop", webdav);
		
		Element e_objectId = new Element("object_id", XmlServlet.NS);
		e_objectId.addContent(String.valueOf(appointmentObj.getObjectID()));
		e_prop.addContent(e_objectId);
		
		Element e_method = new Element("method", XmlServlet.NS);
		e_method.addContent("DELETE");
		e_prop.addContent(e_method);
		
		byte[] b = writeRequest(e_prop);
		sendPut(b, true);
	}
	
	protected byte[] listAppointments(int folderId, Date lastSync, boolean delete) throws Exception {
		Element e_propfind = new Element("propfind", webdav);
		Element e_prop = new Element("prop", webdav);
		
		Element e_folderId = new Element("folder_id", XmlServlet.NS);
		Element e_lastSync = new Element("lastsync", XmlServlet.NS);
		Element e_objectmode = new Element("objectmode", XmlServlet.NS);
		
		e_folderId.addContent(String.valueOf(folderId));
		e_lastSync.addContent(String.valueOf(lastSync.getTime()));
		
		e_propfind.addContent(e_prop);
		e_prop.addContent(e_folderId);
		e_prop.addContent(e_lastSync);
		
		if (delete) {
			e_objectmode.addContent("NEW_AND_MODIFIED,DELETED");
			e_prop.addContent(e_objectmode);
		} 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_propfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		return baos.toByteArray();
	} 
	
	protected void confirmAppointment(int objectId) throws Exception {
		Element e_prop = new Element("prop", webdav);
		
		Element e_objectId = new Element("object_id", XmlServlet.NS);
		e_objectId.addContent(String.valueOf(objectId));
		e_prop.addContent(e_objectId);
		
		Element e_method = new Element("method", XmlServlet.NS);
		e_method.addContent("CONFIRM");
		e_prop.addContent(e_method);
		
		byte[] b = writeRequest(e_prop);
		sendPut(b);
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
		return url;
	}
}

