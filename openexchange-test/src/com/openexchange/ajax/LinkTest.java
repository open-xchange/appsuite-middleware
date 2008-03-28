package com.openexchange.ajax;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.tools.URLParameter;

public class LinkTest extends AbstractAJAXTest {
		
	public LinkTest(String name) {
		super(name);
	}

	private static final String LINK_URL = "/ajax/link";

	public static int[] insertLink(WebConversation webCon, String host, String session) throws Exception {

		FolderObject fo = FolderTest.getStandardContactFolder(webCon, host, session);
		int  fid1 = fo.getObjectID();
		ContactObject co = new ContactObject();
		co.setSurName("Meier");
		co.setGivenName("Herbert");
		co.setDisplayName("Meier, Herbert");
		co.setParentFolderID(fid1);
		int oid1 = ContactTest.insertContact(webCon,co, PROTOCOL+host, session);
		
		fo = FolderTest.getStandardCalendarFolder(webCon, host, session);
		int fid2 = fo.getObjectID();
		AppointmentObject ao = new AppointmentObject();
		ao.setTitle("Nasenmann");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		long startTime = c.getTimeInMillis();
		long endTime = startTime + 3600000;
		
		ao.setStartDate(new Date(startTime));
		ao.setEndDate(new Date(endTime));
		ao.setLocation("Location");
		ao.setShownAs(AppointmentObject.ABSENT);
		ao.setParentFolderID(fid2);
		ao.setIgnoreConflicts(true);
		
		int oid2 =	AppointmentTest.insertAppointment(webCon, ao, TimeZone.getDefault(), PROTOCOL+host, session);
		
		int[] repo = {oid1,fid1,oid2,fid2};
		
		/*
		 *  Now Build The Link Object
		 * 
		 */
		
		LinkObject lo = new LinkObject();
		lo.setFirstFolder(fid1);
		lo.setFirstId(oid1);
		lo.setFirstType(com.openexchange.groupware.Types.CONTACT);
		lo.setSecondFolder(fid2);
		lo.setSecondId(oid2);
		lo.setSecondType(com.openexchange.groupware.Types.APPOINTMENT);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);

		JSONWriter jsonwriter = new JSONWriter(pw);
		jsonwriter.object();
		jsonwriter.key("id1").value(lo.getFirstId());
		jsonwriter.key("module1").value(lo.getFirstType());
		jsonwriter.key("folder1").value(lo.getFirstFolder());
		jsonwriter.key("id2").value(lo.getSecondId());
		jsonwriter.key("module2").value(lo.getSecondType());
		jsonwriter.key("folder2").value(lo.getSecondFolder());
		jsonwriter.endObject();
		
		pw.flush();
		
		JSONObject jResponse = null;
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(PROTOCOL + host+ LINK_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		jResponse = new JSONObject(resp.getText());
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(jResponse.toString());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}

		return repo;
	}
	
	public void testAll() throws Exception {
		
		int[] go = insertLink(getWebConversation(),getHostName(),getSessionId());
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, go[1]);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, go[0]);
		parameter.setParameter("module", com.openexchange.groupware.Types.CONTACT);
		
		WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + LINK_URL + parameter.getURLParameters());
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertEquals(200, resp.getResponseCode());
		
		JSONArray data = (JSONArray)response.getData();
		
		for (int i=0;i<data.length();i++){
			JSONObject jo = data.getJSONObject(i);
			
			if (jo.getInt("id2") == go[2] && jo.getInt("folder2") == go[3] && jo.getInt("module2") == com.openexchange.groupware.Types.APPOINTMENT){
				int iii  = 0;
			}else{
				fail("json error: OBJECT MISSMATCH");
			}
		}
	}
	
	public void testDelete() throws Exception {
		
		int[] go = insertLink(getWebConversation(),getHostName(),getSessionId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		JSONWriter jsonwriter = new JSONWriter(pw);

		jsonwriter.object();
		
		JSONArray jo1 = new JSONArray();
		JSONArray jo2 = new JSONArray();
		
		jo2.put(0,go[2]);
		jo2.put(1,com.openexchange.groupware.Types.APPOINTMENT);
		jo2.put(2,go[3]);

		jo1.put(0,jo2);

		jsonwriter.key("data").value(jo1);
		jsonwriter.endObject();
		
		pw.flush();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, go[1]);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, go[0]);
		parameter.setParameter("module", com.openexchange.groupware.Types.CONTACT);
		
		WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + LINK_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());

		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONArray data = (JSONArray)response.getData();
		JSONArray jo = data.optJSONArray(0);		
		
		if (jo != null){
			fail("json error: DATA MISSMATCH");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	public void testWrongDelete() throws Exception {
	
		int[] go = insertLink(getWebConversation(),getHostName(),getSessionId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		JSONWriter jsonwriter = new JSONWriter(pw);

		jsonwriter.object();
		
		JSONArray jo1 = new JSONArray();
		JSONArray jo2 = new JSONArray();
		
		jo2.put(0,go[2]);
		jo2.put(1,com.openexchange.groupware.Types.APPOINTMENT);
		jo2.put(2,go[3]);

		jo1.put(0,jo2);

		jsonwriter.key("data").value(jo1);
		jsonwriter.endObject();
		
		pw.flush();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, go[1]);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, go[0]);
		parameter.setParameter("module", com.openexchange.groupware.Types.CONTACT);
		
		WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + LINK_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());

		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONArray data = (JSONArray)response.getData();
		JSONArray jo = data.optJSONArray(0);		
		
		if (null != jo){
			fail("json error: DATA MISSMATCH");
		}
		
		//assertEquals(200, resp.getResponseCode());
		
		baos = new ByteArrayOutputStream();
		pw = new PrintWriter(baos);
		
		jsonwriter = new JSONWriter(pw);
		jsonwriter.object();
		jsonwriter.key("data").value(jo1);
		jsonwriter.endObject();
		
		pw.flush();

		bais = new ByteArrayInputStream(baos.toByteArray());
		
		final URLParameter parameter2 = new URLParameter();
		parameter2.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter2.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter2.setParameter(AJAXServlet.PARAMETER_FOLDERID, go[1]);
		parameter2.setParameter(AJAXServlet.PARAMETER_ID, go[0]);
		parameter2.setParameter("module", com.openexchange.groupware.Types.CONTACT);
		
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + LINK_URL + parameter2.getURLParameters(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());

		final Response response2 = Response.parse(resp.getText());
		
		data = (JSONArray)response2.getData();
		jo = data.getJSONArray(0);		
		
		if (jo.getInt(0) != go[2] || jo.getInt(1) != com.openexchange.groupware.Types.APPOINTMENT || jo.getInt(2) != go[3]){
			fail("json error: DATA MISSMATCH");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
}

