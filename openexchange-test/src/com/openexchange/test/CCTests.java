package com.openexchange.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CCTests {
	public static Test suite(){
			
		TestSuite tests = new TestSuite();
		
		tests.addTestSuite(com.openexchange.ajax.LoginTest.class);
		tests.addTestSuite(com.openexchange.ajax.FolderTest.class);
		tests.addTestSuite(com.openexchange.ajax.TasksTest.class);
		tests.addTestSuite(com.openexchange.ajax.ReminderTest.class);
		tests.addTest(com.openexchange.ajax.infostore.InfostoreAJAXSuite.suite());
		tests.addTestSuite(com.openexchange.ajax.ConfigMenuTest.class);
		tests.addTest(com.openexchange.ajax.appointment.AppointmentAJAXSuite.suite());
		tests.addTest(com.openexchange.ajax.contact.ContactAJAXSuite.suite());
		tests.addTestSuite(com.openexchange.ajax.GroupTest.class);
		tests.addTestSuite(com.openexchange.ajax.ResourceTest.class);
		tests.addTestSuite(com.openexchange.ajax.LinkTest.class);
		tests.addTestSuite(com.openexchange.ajax.MultipleTest.class);

		tests.addTestSuite(com.openexchange.ajax.attach.SimpleAttachmentTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.TaskAttachmentTest.class);

		tests.addTest(com.openexchange.webdav.xml.appointment.AppointmentWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.contact.ContactWebdavSuite.suite());	
		tests.addTest(com.openexchange.webdav.xml.folder.FolderWebdavSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.task.TaskWebdavSuite.suite());		
		tests.addTest(com.openexchange.webdav.xml.attachment.AttachmentWebdavSuite.suite());	
		tests.addTestSuite(com.openexchange.webdav.xml.GroupUserTest.class);
		tests.addTestSuite(com.openexchange.webdav.client.SmokeTest.class);
		
		tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreParserTest.class);
		tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreWriterTest.class);
		tests.addTestSuite(com.openexchange.ajax.infostore.JSONSimpleRequestTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.AttachmentParserTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.AttachmentWriterTest.class);
		tests.addTest(com.openexchange.webdav.protocol.ProtocolTestSuite.suite());
		tests.addTest(com.openexchange.webdav.action.ActionTestSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.writer.WriterSuite.suite());
		
		tests.addTestSuite(com.openexchange.groupware.IDGeneratorTest.class);
		tests.addTestSuite(com.openexchange.sessiond.SessiondTest.class);
		tests.addTestSuite(com.openexchange.groupware.CalendarTest.class);
	    tests.addTestSuite(com.openexchange.tools.file.FileStorageTest.class);
		tests.addTestSuite(com.openexchange.tools.file.QuotaFileStorageTest.class);
		tests.addTestSuite(com.openexchange.groupware.attach.AttachmentBaseTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.PropertyStoreTest.class);
		tests.addTestSuite(com.openexchange.i18n.CompiledLineParserTemplateTest.class);
		tests.addTestSuite(com.openexchange.groupware.notify.ParticipantNotifyTest.class);
		
		return tests;
	}
}
