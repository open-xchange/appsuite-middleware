package com.openexchange.ajax;

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
	
	public void testInsertAppointment() throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle("Test Termin!");
		appointmentobject.setStartDate(new Date());
		appointmentobject.setEndDate(new Date());
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(-1);
		
		int object_id = insertAppointment(appointmentobject);
		
		compareAppointmentObjects(appointmentobject, getAppointment(object_id, -1));
	}
	
	public void testUpdateAppointment() throws Exception {
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
	}
	
	public void testListAppointmentsInFolderBetween() throws Exception {
		listAppointment(-1, new Date(), new Date());
	}
	
	public void testSetConfirm() throws Exception {
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
	}
	
	
	public void testDeleteAppointment() throws Exception {
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
	}
	
	protected int insertAppointment(AppointmentObject appointmentobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		return insert(b);
	}
	
	protected void updateAppointment(AppointmentObject appointmentobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw);
		appointmentwriter.writeAppointment(appointmentobject);
		
		byte b[] = baos.toByteArray();
	}
	
	protected void deleteAppointment(AppointmentObject appointmentobject) throws Exception{
		delete(appointmentobject);
	}
	
	protected void confirmAppointment(int object_id, int folder_id, int confirm) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=confirm");
		parameter.append("&object_id=" + object_id);
		parameter.append("&folder_id=" + folder_id);
		parameter.append("confirm=" + confirm);
		
		req = new PostMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		System.out.println(resp.getText());
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void listAppointment(int folder_id, Date from, Date to) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=list");
		parameter.append("&folder_id=" + folder_id);
		parameter.append("&from=" + from.getTime());;
		parameter.append("&to=" + to.getTime());
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected AppointmentObject getAppointment(int object_id, int folder_id) throws Exception {
		WebResponse resp = getObject(object_id, folder_id);
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
	
	protected String getURL() {
		return url;
	}
}

