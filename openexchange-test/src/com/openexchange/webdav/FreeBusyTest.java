package com.openexchange.webdav;

import java.util.Date;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.tools.URLParameter;
import com.openexchange.webdav.xml.GroupUserTest;

public class FreeBusyTest extends AbstractWebdavTest {

	protected Date startTime = null;

	protected Date endTime = null;

	protected static final long dayInMillis = 86400000;

	private static final String FREEBUSY_URL = "/servlet/webdav.freebusy";

	public FreeBusyTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() {

	}

	public void testConnect() throws Exception {
		final int contextId = GroupUserTest.getContextId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);

		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*7));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*7));

        final URLParameter parameter = new URLParameter();
		parameter.setParameter("contextid", contextId);
        parameter.setParameter(AJAXServlet.PARAMETER_START, start);
        parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter("username", getLogin());
		parameter.setParameter("server", "open-xchange.tux");

        final WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + FREEBUSY_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());
	}
}

