package com.openexchange.ajax.contact;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;

public class UpdatesTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	public UpdatesTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUpdates() throws Exception {
		listModifiedAppointment(getWebConversation(), contactFolderId, new Date(0), PROTOCOL + getHostName(), getSessionId());
	}
}

