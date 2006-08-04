package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;


public class AppointmentTest extends CommonTest {
	
	private static final String APPOINTMENT_URL = "/ajax/appointment";
	
	private static int appointmentFolderId = -1;
	
	private static long startTime = 0;
	
	private static long endTime = 0;
	
	private static final long d7 = 604800000;
	
	private static int userParticipantId2 = -1;
	
	private static int userParticipantId3 = -1;
	
	private static int groupParticipantId1 = -1;
	
	private static int resourceParticipantId1 = -1;
    
	private static boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		init();
	}
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}

		userId = ParticipantTest.searchUser(getWebConversation(), getLogin(), PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.USER)[0].getId();
		
		String userParticipant2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
		String userParticipant3 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
		
		userParticipantId2 = ParticipantTest.searchUser(getWebConversation(), userParticipant2, PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.USER)[0].getId();
		userParticipantId3 = ParticipantTest.searchUser(getWebConversation(), userParticipant3, PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.USER)[0].getId();
		
		String groupParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "group_participant", "");
		
		groupParticipantId1 = ParticipantTest.listGroup(getWebConversation(), PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		String resourceParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "resource_participant", "");
		
		resourceParticipantId1 = ParticipantTest.searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
        FolderObject appointmentFolder = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        appointmentFolderId = appointmentFolder.getObjectID();
		
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
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new GroupParticipant();
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
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new ResourceParticipant();
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
		list(id, appointmentFolderId, new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS } );
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
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId3);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		participants[3] = new ResourceParticipant();
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
		update(b, appointmentobject.getObjectID(), appointmentFolderId);
	}
	
	protected void actionDelete(int[] id) throws Exception{
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.PARAMETER_DELETE);
		parameter.append("&" + AJAXServlet.PARAMETER_TIMESTAMP + "=" + new Date(0).getTime());
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < id.length; a++) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, id[a]);
			jsonObj.put(AJAXServlet.PARAMETER_FOLDERID, appointmentFolderId);
			jsonArray.put(jsonObj);
		} 
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("server error: " + jsonobject.getString(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
			assertTrue("array length is 1", data.length() == 1);
			assertEquals("first entry in array is 1", 1, data.getInt(0));
		} else {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionConfirm(int object_id, int confirm) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_CONFIRM);
		parameter.append("&" + DataFields.ID + "=" + object_id);
		parameter.append("&" + AJAXServlet.PARAMETER_CONFIRM +"=" + confirm);
		
		req = new PostMethodWebRequest(PROTOCOL + getHostName() + APPOINTMENT_URL + parameter.toString());
		resp = getWebConversation().getResponse(req);
		
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
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_ALL);
		parameter.append("&" + AJAXServlet.PARAMETER_INFOLDER + "=" + folderId);
		parameter.append("&" + AJAXServlet.PARAMETER_START + "=" + start.getTime());;
		parameter.append("&" + AJAXServlet.PARAMETER_END + "=" + end.getTime());
		parameter.append("&" + AJAXServlet.PARAMETER_COLUMNS + "=");
		parameter.append(AppointmentObject.OBJECT_ID);
		
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + APPOINTMENT_URL + parameter.toString());
		resp = getWebConversation().getResponse(req);
		
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
		WebResponse resp = getObject(objectId, appointmentFolderId);
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
		return APPOINTMENT_URL;
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

