package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.container.AppointmentObject;
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
	
	protected void setUp() throws Exception {
		super.setUp();
		
		url = ajaxProps.getProperty("appointment_url");
		appointmentFolderId = Integer.parseInt(ajaxProps.getProperty("appointment_folder"));
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		
		startTime = c.getTimeInMillis();
		endTime = startTime + 3600000;
	}
	
	public void testNew() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
		int objectId = actionNew(appointmentObj);
	}
	
	public void testUpdate() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
		int objectId = actionNew(appointmentObj);
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
		actionUpdate(appointmentObj);
	}
	
	public void testAll() throws Exception {
		actionAll(appointmentFolderId, new Date(System.currentTimeMillis()-d7), new Date(System.currentTimeMillis()+d7));
	}
	
	public void testList() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
		int id1 = actionNew(appointmentObj);
		int id2 = actionNew(appointmentObj);
		int id3 = actionNew(appointmentObj);
		
		actionList(new int[]{id1, id2, id3});
	} 
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
		int objectId = actionNew(appointmentObj);
		
		actionConfirm(objectId, AppointmentObject.ACCEPT);
	}
	
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
		int id1 = actionNew(appointmentObj);
		int id2 = actionNew(appointmentObj);
		
		actionDelete(new int[]{id1, id2, 1});
	}
	
	protected void actionList(int[] id) throws Exception{
		list(id, new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.LAST_MODIFIED, AppointmentObject.TITLE } );
	}
	
	public void testGet() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject();
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
		parameter.append("&columns=1%2C200%2C201%2C202%2C203%2C220");
		
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
	
	private AppointmentObject createAppointmentObject() {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
}

