package com.openexchange.ajax;

import com.meterware.httpunit.PutMethodWebRequest;
import com.openexchange.groupware.container.UserParticipant;
import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.ldap.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
import org.json.JSONObject;

public class AppointmentTest extends TestCase {
	
	private static String sessionId = null;
	
	private WebConversation wc = null;
	
	private WebRequest req = null;
	
	private WebResponse resp = null;
	
	private static String login = null;
	
	private static String password = null;
	
	private static String host = null;
	
	private static String url = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		Properties prop = Init.getAJAXProperties();
		login = prop.get("login").toString();
		password = prop.get("password").toString();
		host = prop.get("host").toString();
		url = prop.get("appointment_url").toString();
		
		wc = new WebConversation();
		
		sessionId = LoginTest.getLogin(wc, host, login, password);
		System.out.println("Obtained Session Id: " + sessionId);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInsertAppointment() {
		try {
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentobject.setTitle("Test Termin!");
			appointmentobject.setStartDate(new Date());
			appointmentobject.setEndDate(new Date());
			appointmentobject.setLocation("Location");
			appointmentobject.setShownAs(AppointmentObject.ABSEND);
			appointmentobject.setParentFolderID(-1);
			
			int object_id = insertAppointment(appointmentobject);
			
			compareAppointmentObjects(appointmentobject, getAppointment(object_id, -1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void testUpdateAppointment() {
		try {
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentobject.setTitle("Test Termin!");
			appointmentobject.setStartDate(new Date());
			appointmentobject.setEndDate(new Date());
			appointmentobject.setLocation("Location");
			appointmentobject.setShownAs(AppointmentObject.ABSEND);
			appointmentobject.setParentFolderID(-1);
			
			int object_id = insertAppointment(appointmentobject);
			
			appointmentobject.setShownAs(AppointmentObject.RESERVED);
			appointmentobject.setFullTime(true);
			appointmentobject.setLocation(null);
			
			updateAppointment(appointmentobject);
			
			compareAppointmentObjects(appointmentobject, getAppointment(object_id, -1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void testListAppointmentsInFolderBetween() {
		try {
			listAppointment(-1, new Date(), new Date());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void testSetConfirm() {
		try {
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentobject.setTitle("Test Termin!");
			appointmentobject.setStartDate(new Date());
			appointmentobject.setEndDate(new Date());
			appointmentobject.setLocation("Location");
			appointmentobject.setShownAs(AppointmentObject.ABSEND);
			appointmentobject.setParentFolderID(-1);
			
			UserParticipant p = new UserParticipant(new User());
			p.setIdentifier(12);
			p.setConfirm(AppointmentObject.DECLINE);
			
			int object_id = insertAppointment(appointmentobject);
			
			confirmAppointment(object_id, -1, AppointmentObject.ACCEPT);
			
			compareAppointmentObjects(appointmentobject, getAppointment(object_id, -1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void testDeleteAppointment() {
		try {
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentobject.setTitle("Test Termin!");
			appointmentobject.setStartDate(new Date());
			appointmentobject.setEndDate(new Date());
			appointmentobject.setLocation("Location");
			appointmentobject.setShownAs(AppointmentObject.ABSEND);
			appointmentobject.setParentFolderID(-1);
			
			int object_id = insertAppointment(appointmentobject);
			
			appointmentobject.setObjectID(object_id);
			
			deleteAppointment(appointmentobject);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected int insertAppointment(AppointmentObject appointmentobject) throws Exception {
		int object_id = 0;
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=new");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(host + url + parameter.toString(), bais, "text/javascript");
		resp = wc.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail(jsonobject.getString("error"));
		}
		
		if (jsonobject.has(OXObject.OBJECT_ID)) {
			object_id = jsonobject.getInt(OXObject.OBJECT_ID);
			assertTrue("object_id > 0", (object_id > 0));
		} else {
			fail("no object_id in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
		
		return object_id;
	}
	
	protected void updateAppointment(AppointmentObject appointmentobject) throws Exception {
		int object_id = 0;
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=update");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(host + url + parameter.toString(), bais, "text/javascript");
		resp = wc.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail(jsonobject.getString("error"));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void deleteAppointment(AppointmentObject appointmentobject) throws Exception{
		long begins = System.currentTimeMillis();
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=delete");
		parameter.append("&object_id=" + appointmentobject.getObjectID());
		parameter.append("&folder_id=" + appointmentobject.getParentFolderID());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		req = new PostMethodWebRequest(host + url + parameter.toString(), bais, "text/javascript");
		resp = wc.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail(jsonobject.getString("error"));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void confirmAppointment(int object_id, int folder_id, int confirm) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=confirm");
		parameter.append("&object_id=" + object_id);
		parameter.append("&folder_id=" + folder_id);
		parameter.append("confirm=" + confirm);
		
		req = new PostMethodWebRequest(host + url + parameter.toString());
		resp = wc.getResponse(req);
		System.out.println(resp.getText());
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void listAppointment(int folder_id, Date from, Date to) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=list");
		
		req = new GetMethodWebRequest(host + url + parameter.toString());
		resp = wc.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected AppointmentObject getAppointment(int object_id, int folder_id) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("session=" + sessionId);
		parameter.append("&action=get");
		parameter.append("&object_id=" + object_id);
		parameter.append("&folder_id=" + folder_id);
		
		req = new GetMethodWebRequest(host + url + parameter.toString());
		resp = wc.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		AppointmentParser appointmentparser = new AppointmentParser(null);
		
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentparser.parse(appointmentobject, jsonobject);
		
		return appointmentobject;
	}
	
	protected void compareAppointmentObjects(AppointmentObject a1, AppointmentObject a2) throws Exception {
		assertEquals("compare title", a1.getTitle(), a2.getTitle());
		assertEquals("compare start_date", a1.getStartDate(), a2.getStartDate());
		assertEquals("compare end_date", a1.getEndDate(), a2.getEndDate());
		assertEquals("compare shown_as", a1.getShownAs(), a2.getShownAs());
		assertEquals("compare location", a1.getLocation(), a2.getLocation());
		assertEquals("compare full_time", a1.getFullTime(), a2.getFullTime());
	}
}
