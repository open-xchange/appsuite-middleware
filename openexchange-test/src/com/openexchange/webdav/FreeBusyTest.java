package com.openexchange.webdav;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.tools.URLParameter;
import java.util.Date;
import junit.framework.TestCase;

public class FreeBusyTest extends AbstractWebdavTest {
	
	protected Date startTime = null;

	protected Date endTime = null;
	
	private static final String FREEBUSY_URL = "/servlet/webdav.freebusy";
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() {
		
	}
	
	public void testConnect() throws Exception {
		Date start = new Date();
		Date end = new Date();
		
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter("username", getLogin());
		parameter.setParameter("server", "localhost");
        
        WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + FREEBUSY_URL + parameter.getURLParameters());
        WebResponse resp = webCon.getResponse(req);
        
        assertEquals(200, resp.getResponseCode());
	}
	
	public void _notestStatusAbsent() throws Exception {
		
	}
	
	public void _notestStatusFree() throws Exception {
		
	}
	
	public void _notestResource() throws Exception {
		
	}
}

