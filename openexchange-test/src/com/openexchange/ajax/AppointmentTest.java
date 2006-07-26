package com.openexchange.ajax;

import com.openexchange.groupware.container.UserParticipant;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.ldap.User;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class AppointmentTest extends CommonTest {
	
	private String url = "/ajax/appointment";
	
	private int appointmentFolderId = 955;
	
	private long startTime = 1153850400000L;
	
	private long endTime = 1153850400000L;
	
	private long d7 = 604800000;
	
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
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
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
		
		if (jsonobject.has("error")) {
			fail("server error: " + (String)jsonobject.get("error"));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
		} else {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionGet(int objectId) throws Exception {
		WebResponse resp = getObject(objectId);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			
			AppointmentParser appointmentparser = new AppointmentParser(null);
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentparser.parse(appointmentobject, data);

			assertEquals("same folder id:", appointmentFolderId, appointmentobject.getParentFolderID());
		} else {
			fail("missing data in json object");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
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

