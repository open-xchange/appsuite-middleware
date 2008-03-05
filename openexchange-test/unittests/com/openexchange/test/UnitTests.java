package com.openexchange.test;

import junit.framework.Test;
import junit.framework.TestSuite;


public class UnitTests {
    public static Test suite() {

		TestSuite tests = new TestSuite();

		tests.addTestSuite(com.openexchange.groupware.infostore.URLHelperTest.class);
		tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreParserTest.class);
		tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreWriterTest.class);
		tests.addTestSuite(com.openexchange.ajax.infostore.JSONSimpleRequestTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.AttachmentParserTest.class);
		tests.addTestSuite(com.openexchange.ajax.attach.AttachmentWriterTest.class);
		tests.addTest(com.openexchange.webdav.protocol.ProtocolTestSuite.suite());
        tests.addTestSuite(com.openexchange.webdav.protocol.WebdavPathTest.class);        
        tests.addTest(com.openexchange.webdav.action.ActionTestSuite.suite());
		tests.addTest(com.openexchange.webdav.xml.writer.WriterSuite.suite());
		tests.addTestSuite(com.openexchange.webdav.action.IfHeaderParserTest.class);
		tests.addTestSuite(com.openexchange.webdav.infostore.integration.DropBoxScenarioTest.class);

		tests.addTestSuite(com.openexchange.groupware.IDGeneratorTest.class);
		tests.addTestSuite(com.openexchange.sessiond.SessiondTest.class);
		// tests.addTestSuite(com.openexchange.groupware.CalendarTest.class);
	   // tests.addTestSuite(com.openexchange.tools.file.FileStorageTest.class);

		tests.addTestSuite(com.openexchange.tools.file.QuotaFileStorageTest.class);
		tests.addTestSuite(com.openexchange.tools.file.SaveFileActionTest.class);
		tests.addTestSuite(com.openexchange.tools.update.IndexTest.class);

		tests.addTestSuite(com.openexchange.tools.io.SizeAwareInputStreamTest.class);

		tests.addTestSuite(com.openexchange.groupware.attach.actions.CreateAttachmentsActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.attach.actions.UpdateAttachmentsActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.attach.actions.RemoveAttachmentsActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.attach.actions.FireAttachedEventActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.attach.actions.FireDetachedEventActionTest.class);

		tests.addTestSuite(com.openexchange.groupware.infostore.URLHelperTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.InfostoreDeleteTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.PropertyStoreTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.EntityLockManagerTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.InfostoreFacadeTest.class);
		tests.addTestSuite(com.openexchange.groupware.folder.FolderTreeUtilTest.class);
		tests.addTestSuite(com.openexchange.groupware.folder.FolderLockManagerTest.class);

		tests.addTestSuite(com.openexchange.groupware.infostore.CreateDocumentActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.CreateVersionActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.UpdateDocumentActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.UpdateVersionActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.DeleteDocumentActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.DeleteVersionActionTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.validation.ValidationChainTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.validation.InfostoreInvalidCharactersCheckTest.class);
		//tests.addTestSuite(com.openexchange.groupware.infostore.DelUserFolderDiscovererTest.class);


		tests.addTestSuite(com.openexchange.groupware.infostore.PathResolverTest.class);
		tests.addTestSuite(com.openexchange.groupware.infostore.webdav.FolderCollectionPermissionHandlingTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.webdav.PermissionTest.class);

        tests.addTestSuite(com.openexchange.i18n.CompiledLineParserTemplateTest.class);
		tests.addTestSuite(com.openexchange.groupware.notify.ParticipantNotifyTest.class);

		tests.addTestSuite(com.openexchange.groupware.attach.AttachmentBaseTest.class);

		tests.addTest(com.openexchange.groupware.importexport.ImportExportStandaloneSuite.suite());

		tests.addTestSuite(com.openexchange.webdav.action.behaviour.RequestSpecificBehaviourRegistryTest.class);
		tests.addTestSuite(com.openexchange.webdav.action.behaviour.UserAgentBehaviourTest.class);


        // Mail

        /*tests.addTestSuite(com.openexchange.mail.MailAccessTest.class);
        tests.addTestSuite(com.openexchange.mail.MailConverterTest.class);
        tests.addTestSuite(com.openexchange.mail.MailFolderTest.class);
        tests.addTestSuite(com.openexchange.mail.MailLogicToolsTest.class);
        tests.addTestSuite(com.openexchange.mail.MailMessageTest.class);
        tests.addTestSuite(com.openexchange.mail.MailParserWriterTest.class); */
        //TODO: Enable Mail Tests (TODO: Insert imapServer into DB in test setup.


        return tests;
	}
}
