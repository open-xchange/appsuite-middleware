package com.openexchange.ajax;

import com.openexchange.ajax.*;
import com.openexchange.groupware.container.UserParticipant;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.ldap.User;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.json.JSONObject;

public class AppointmentTest extends CommonTest {
	
	private String url = "/ajax/appointment";

	private int appointmentFolderId = 955;
	
	private long startTime = 1153850400000L;
	
	private long endTime = 1153850400000L;
	
	public void testNew() throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		int object_id = actionNew(appointmentobject);
		
		compareAppointmentObjects(appointmentobject, getAppointment(object_id));
	}
	
	public void testUpdate() throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		int object_id = actionNew(appointmentobject);
		
		appointmentobject.setShownAs(AppointmentObject.RESERVED);
		appointmentobject.setFullTime(true);
		appointmentobject.setLocation(null);
		
		actionUpdate(appointmentobject);
		
		compareAppointmentObjects(appointmentobject, getAppointment(object_id));
	}
	
	public void testAll() throws Exception {
		actionAll(-1, new Date(), new Date());
	}
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		UserParticipant p = new UserParticipant(new User());
		p.setIdentifier(12);
		p.setConfirm(AppointmentObject.DECLINE);
		
		int object_id = actionNew(appointmentobject);
		
		actionConfirm(object_id, AppointmentObject.ACCEPT);
		
		compareAppointmentObjects(appointmentobject, getAppointment(object_id));
	}
	
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		int object_id = actionNew(appointmentobject);
		
		appointmentobject.setObjectID(object_id);
		
		actionDelete(appointmentobject);
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
		update(b);
	}
	
	protected void actionDelete(AppointmentObject appointmentobject) throws Exception{
		delete(appointmentobject);
	}
	
	protected void actionConfirm(int object_id, int confirm) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=confirm");
		parameter.append("&object_id=" + object_id);
		parameter.append("&confirm=" + confirm);
		
		req = new PostMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		System.out.println(resp.getText());
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionAll(int folderId, Date from, Date to) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=list");
		parameter.append("&folder_id=" + folderId);
		parameter.append("&from=" + from.getTime());;
		parameter.append("&to=" + to.getTime());
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
	}
	protected AppointmentObject getAppointment(int object_id) throws Exception {
		WebResponse resp = getObject(object_id);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			
			AppointmentParser appointmentparser = new AppointmentParser(null);		
			AppointmentObject appointmentobject = new AppointmentObject();
			appointmentparser.parse(appointmentobject, data);
		
			return appointmentobject;
		} else {
			fail("missing data in json object");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		throw new Exception("something went wrong here");
	}

	protected void compareAppointmentObjects(AppointmentObject a1, AppointmentObject a2) throws Exception {
		assertTrue(a1.containsTitle() == a2.containsTitle());
		assertEquals("compare title", a1.getTitle(), a2.getTitle());
		assertEquals("compare start_date", a1.getStartDate().getTime(), a2.getStartDate().getTime());
		assertEquals("compare end_date", a1.getEndDate().getTime(), a2.getEndDate().getTime());
		assertEquals("compare shown_as", a1.getShownAs(), a2.getShownAs());
		assertEquals("compare location", a1.getLocation(), a2.getLocation());
		assertEquals("compare full_time", a1.getFullTime(), a2.getFullTime());
		assertEquals("compare folder_id", a1.getParentFolderID(), a2.getParentFolderID());
	}
	
	protected String getURL() {
		return url;
	}	
}

