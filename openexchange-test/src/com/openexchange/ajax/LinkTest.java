package com.openexchange.ajax;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.tools.URLParameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

public class LinkTest extends AbstractAJAXTest {
		
	private static final String LINK_URL = "/ajax/link";
	
	private static final Log LOG = LogFactory.getLog(LinkTest.class);
	
	public int[] testNew() throws Exception {
		
		/*
		 *  BUILD 2 OBJECTS FOR THE FIRST TEST
		 *  1. Contact Object
		 *  2. Appointment Object
		 */
		
		FolderObject fo = FolderTest.getStandardContactFolder(getWebConversation(), getHostName(), getSessionId());
		int  fid1 = fo.getObjectID();
		ContactObject co = new ContactObject();
		co.setSurName("Meier");
		co.setGivenName("Herbert");
		co.setDisplayName("Meier, Herbert");
		co.setParentFolderID(fid1);
		int oid1 = ContactTest.insertContact(getWebConversation(),co, PROTOCOL+getHostName(), getSessionId());
		
		fo = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
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
		ao.setShownAs(AppointmentObject.ABSEND);
		ao.setParentFolderID(fid2);
		
		int oid2 =	AppointmentTest.insertAppointment(getWebConversation(), ao, PROTOCOL+getHostName(), getSessionId());
		
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
		
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName()+ LINK_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}

		/*
		JSONObject data = (JSONObject)response.getData();
		System.out.println("##########$$$$$$$$$$$$$$$ =]> "+data.toString());

		JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId = data.getInt(DataFields.ID);
		}
		*/
		return repo;

	}
	
	public void testAll() throws Exception {
		
		int[] go = testNew();
		
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
		
		//assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		JSONArray data = (JSONArray)response.getData();
		
		for (int i=0;i<data.length();i++){
			JSONObject jo = data.getJSONObject(i);
			//jo.getInt("id1");
			//jo.getInt("folder1");
			//jo.getInt("module1");
			
			if (jo.getInt("id2") == go[2] && jo.getInt("folder2") == go[3] && jo.getInt("module2") == com.openexchange.groupware.Types.APPOINTMENT){
				//doll
			}else{
				fail("json error: OBJECT MISSMATCH");
			}
		}
		
		System.out.println(data.toString());
		
		//return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
}

