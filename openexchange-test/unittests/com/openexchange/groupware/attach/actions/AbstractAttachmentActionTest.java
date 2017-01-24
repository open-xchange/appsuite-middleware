
package com.openexchange.groupware.attach.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.attach.impl.AttachmentQueryCatalog;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;

public abstract class AbstractAttachmentActionTest extends AbstractActionTest {

    private User user;
    private Context ctx;
    private final List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
    private AttachmentBase attachmentBase = null;
    private AttachmentQueryCatalog queryCatalog = new AttachmentQueryCatalog();
    private DBProvider provider;

    @Before
    public void setUp() throws Exception {
        Init.startServer();
        provider = new DBPoolProvider();
        queryCatalog = new AttachmentQueryCatalog();

        final TestConfig config = new TestConfig();
        final String userName = config.getUser();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        user = UserStorage.getInstance().getUser(tools.resolveUser(userName, ctx), ctx);

        attachmentBase = new AttachmentBaseImpl(provider);

        initAttachments();
    }

    @After
    public void tearDown() throws Exception {
        Init.stopServer();
    }

    protected User getUser() {
        return user;
    }

    protected Context getContext() {
        return ctx;
    }

    protected Session getSession() throws Exception {
        return SessionObjectWrapper.createSessionObject(user.getId(), getContext().getContextId(), String.valueOf(System.currentTimeMillis()));
    }

    protected List<AttachmentMetadata> getAttachments() {
        return attachments;
    }

    protected AttachmentBase getAttachmentBase() {
        return attachmentBase;
    }

    protected AttachmentQueryCatalog getQueryCatalog() {
        return queryCatalog;
    }

    protected DBProvider getProvider() {
        return provider;
    }

    private void initAttachments() {
        // TODO: Get Real IDs
        AttachmentMetadata m = new AttachmentImpl();
        m.setFileMIMEType("text/plain");
        m.setFilesize(1024);
        m.setFilename("testfile.txt");
        m.setFileId("00/00/23");
        m.setAttachedId(22);
        m.setModuleId(22);
        m.setRtfFlag(true);
        m.setFolderId(22);
        m.setId(1024);
        m.setCreationDate(new Date());
        attachments.add(m);

        m = new AttachmentImpl();
        m.setFileMIMEType("text/plain");
        m.setFilesize(2048);
        m.setFilename("testfile2.txt");
        m.setFileId("00/00/24");
        m.setAttachedId(22);
        m.setModuleId(22);
        m.setRtfFlag(true);
        m.setFolderId(22);
        m.setId(2048);
        m.setCreationDate(new Date());
        attachments.add(m);

        m = new AttachmentImpl();
        m.setFileMIMEType("text/plain");
        m.setFilesize(4096);
        m.setFilename("testfile3.txt");
        m.setFileId("00/00/25");
        m.setAttachedId(22);
        m.setModuleId(22);
        m.setRtfFlag(true);
        m.setFolderId(22);
        m.setId(4096);
        m.setCreationDate(new Date());
        attachments.add(m);

    }

    public static final void assertEquals(final AttachmentMetadata m1, final AttachmentMetadata m2) {
        final GetSwitch get1 = new GetSwitch(m1);
        final GetSwitch get2 = new GetSwitch(m2);

        for (final AttachmentField field : AttachmentField.VALUES) {
            Assert.assertEquals(field.doSwitch(get1), field.doSwitch(get2));
        }
    }
}
