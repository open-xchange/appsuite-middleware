package com.openexchange.test;

import com.openexchange.SmokeTestSuite;
import com.openexchange.ajax.reminder.ReminderAJAXSuite;
import com.openexchange.ajax.reminder.ReminderBugTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;


public class InterfaceTests {

    public static Test suite() {

		TestSuite tests = new TestSuite();

        // First of all the smoke tests.
        tests.addTest(SmokeTestSuite.suite());

        tests.addTestSuite(com.openexchange.ajax.session.LoginTest.class);
        tests.addTestSuite(com.openexchange.ajax.session.RedirectTest.class);
        tests.addTestSuite(com.openexchange.ajax.FolderTest.class);
        tests.addTest(ReminderAJAXSuite.suite());
		tests.addTest(ReminderBugTestSuite.suite());

        tests.addTest(com.openexchange.ajax.infostore.InfostoreAJAXSuite.suite());
        tests.addTest(com.openexchange.ajax.config.ConfigTestSuite.suite());
        tests.addTest(com.openexchange.ajax.appointment.AppointmentAJAXSuite.suite());

		tests.addTest(com.openexchange.ajax.contact.ContactAJAXSuite.suite());
		tests.addTestSuite(com.openexchange.ajax.UserTest.class);
		tests.addTestSuite(com.openexchange.ajax.GroupTest.class);
		tests.addTestSuite(com.openexchange.ajax.ResourceTest.class);
		tests.addTestSuite(com.openexchange.ajax.LinkTest.class);
		tests.addTestSuite(com.openexchange.ajax.MultipleTest.class);

		tests.addTest(com.openexchange.groupware.importexport.ImportExportServerSuite.suite());

		tests.addTestSuite(com.openexchange.ajax.attach.SimpleAttachmentTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.TaskAttachmentTest.class);
        tests.addTest(com.openexchange.ajax.task.TaskTestSuite.suite());

		tests.addTest(com.openexchange.webdav.xml.appointment.AppointmentWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.appointment.AppointmentBugTestSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.contact.ContactWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.folder.FolderWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.task.TaskWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.attachment.AttachmentWebdavSuite.suite());
		tests.addTestSuite(com.openexchange.webdav.xml.GroupUserTest.class);
//		tests.addTestSuite(com.openexchange.webdav.client.SmokeTest.class);
		tests.addTestSuite(com.openexchange.webdav.client.NaughtyClientTest.class);

	

		return tests;
	}

}
