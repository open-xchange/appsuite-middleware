package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.OXFolderTools;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;


public class AppointmentTest extends CommonTest {
	
	private static String url = "/ajax/appointment";
	
	private static int appointmentFolderId = -1;
	
	private static long startTime = 0;
	
	private static long endTime = 0;
	
	private static final long d7 = 604800000;
	
	private static int userParticipantId1 = 232;
	
	private static int userParticipantId2 = 263;
	
	private static int userParticipantId3 = 263;
	
	private static int groupParticipantId1 = 13;
	
	private static int resourceParticipantId1 = -1;
	
	private boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		init();
	}
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(login, password, "localhost");
				
		url = AbstractConfigWrapper.parseProperty(ajaxProps, "appointment_url", url);
		appointmentFolderId = OXFolderTools.getCalendarStandardFolder(sessionObj.getUserObject().getId(), sessionObj.getContext());
		
		String userParticipant1 = AbstractConfigWrapper.parseProperty(ajaxProps, "user_participant1", "");
		String userParticipant2 = AbstractConfigWrapper.parseProperty(ajaxProps, "user_participant2", "");
		String userParticipant3 = AbstractConfigWrapper.parseProperty(ajaxProps, "user_participant3", "");
		
		userParticipantId1 = sc.addSession(userParticipant1, password, "localhost").getUserObject().getId();
		userParticipantId2 = sc.addSession(userParticipant2, password, "localhost").getUserObject().getId();
		userParticipantId3 = sc.addSession(userParticipant3, password, "localhost").getUserObject().getId();
		
		groupParticipantId1 = AbstractConfigWrapper.parseProperty(ajaxProps, "group_participant_id", groupParticipantId1);
		resourceParticipantId1 = AbstractConfigWrapper.parseProperty(ajaxProps, "resource_participant_id", resourceParticipantId1);
		
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
		int objectId = actionNew(appointmentObj);
	}
	
	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId1);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new UserParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = actionNew(appointmentObj);
	}
	
	public void testUpdateAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = actionNew(appointmentObj);
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
		actionUpdate(appointmentObj);
	}
	
	public void testUpdateAppointmentWithParticipant() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		int objectId = actionNew(appointmentObj);
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId1);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId3);
		participants[2] = new UserParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new UserParticipant();
		participants[3].setIdentifier(resourceParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		actionUpdate(appointmentObj);
	}
	
	public void testAll() throws Exception {
		actionAll(appointmentFolderId, new Date(System.currentTimeMillis()-d7), new Date(System.currentTimeMillis()+d7));
	}
	
	public void testList() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testList");
		int id1 = actionNew(appointmentObj);
		int id2 = actionNew(appointmentObj);
		int id3 = actionNew(appointmentObj);
		
		actionList(new int[]{id1, id2, id3});
	}
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		int objectId = actionNew(appointmentObj);
		
		actionConfirm(objectId, AppointmentObject.ACCEPT);
	}
	
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int id1 = actionNew(appointmentObj);
		int id2 = actionNew(appointmentObj);
		
		actionDelete(new int[]{id1, id2, 1});
	}
	
	protected void actionList(int[] id) throws Exception{
		list(id, new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE } );
	}
	
	public void testGet() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testGet");
		int objectId = actionNew(appointmentObj);
		
		actionGet(objectId);
	}
	
	public void testGetWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testGetWithParticipants");
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId1);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId3);
		participants[2] = new UserParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new UserParticipant();
		participants[3].setIdentifier(resourceParticipantId1);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = actionNew(appointmentObj);
		
		actionGet(objectId);
	}
	
	protected int actionNew(AppointmentObject appointmentobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		return insert(b);
	}
	
	protected void actionUpdate(AppointmentObject appointmentobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		update(b, appointmentobject.getObjectID());
	}
	
	protected void actionDelete(int[] id) throws Exception{
		delete(id);
	}
	
	protected void actionConfirm(int object_id, int confirm) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=confirm");
		parameter.append("&id=" + object_id);
		parameter.append("&confirm=" + confirm);
		
		req = new PostMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionAll(int folderId, Date start, Date end) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=all");
		parameter.append("&folder=" + folderId);
		parameter.append("&start=" + start.getTime());;
		parameter.append("&end=" + end.getTime());
		parameter.append("&columns=");
		parameter.append(AppointmentObject.OBJECT_ID);
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + (String)jsonobject.get(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
		} else {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionGet(int objectId) throws Exception {
		WebResponse resp = getObject(objectId);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			
			AppointmentParser appointmentparser = new AppointmentParser(null);
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentparser.parse(appointmentobject, data);
			
			assertEquals("same folder id:", appointmentFolderId, appointmentobject.getParentFolderID());
		} else {
			fail("missing data in json object");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected String getURL() {
		return url;
	}
	
	private AppointmentObject createAppointmentObject(String title) {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
}

