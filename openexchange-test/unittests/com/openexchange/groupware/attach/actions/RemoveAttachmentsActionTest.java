
package com.openexchange.groupware.attach.actions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.groupware.attach.impl.DeleteAttachmentAction;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.tx.UndoableAction;

public class RemoveAttachmentsActionTest extends AbstractAttachmentActionTest {

    private final CreateAttachmentAction createAction = new CreateAttachmentAction();
    private int delCountStart;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        createAction.setAttachments(getAttachments());
        createAction.setQueryCatalog(getQueryCatalog());
        createAction.setProvider(getProvider());
        createAction.setContext(getContext());

        createAction.perform();

        delCountStart = countDel();

    }

    @After
    public void tearDown() throws Exception {
        createAction.undo();
        super.tearDown();
    }

    @Override
    protected UndoableAction getAction() throws Exception {
        final DeleteAttachmentAction deleteAction = new DeleteAttachmentAction();
        deleteAction.setAttachments(getAttachments());
        deleteAction.setQueryCatalog(getQueryCatalog());
        deleteAction.setProvider(getProvider());
        deleteAction.setContext(getContext());
        return deleteAction;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        checkDelTable();
        checkRemovedFromNormalTable();
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final AttachmentMetadata attachment : getAttachments()) {
            final AttachmentMetadata loaded = getAttachmentBase().getAttachment(getSession(), attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
            assertEquals(attachment, loaded);
        }
        checkRemovedFromDel();
    }

    private void checkRemovedFromNormalTable() throws Exception {
        for (final AttachmentMetadata attachment : getAttachments()) {
            try {
                getAttachmentBase().getAttachment(getSession(), attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
                fail("Found attachment");
            } catch (final OXException x) {
                assertTrue(true);
            }
        }
    }

    private int countDel() throws OXException, SQLException {
        final StringBuilder in = new StringBuilder();
        for (final AttachmentMetadata m : getAttachments()) {
            in.append(m.getId()).append(',');
        }
        in.setLength(in.length() - 1);

        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = getProvider().getReadConnection(getContext());
            stmt = readCon.prepareStatement("SELECT count(*) FROM del_attachment WHERE cid = ? and id in (" + in.toString() + ")");
            stmt.setInt(1, getContext().getContextId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return -1;
            }
            return rs.getInt(1);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
            if (readCon != null) {
                getProvider().releaseReadConnection(getContext(), readCon);
            }
        }
    }

    private void checkDelTable() throws OXException, SQLException {
        Assert.assertEquals(getAttachments().size(), countDel() - delCountStart);
    }

    private void checkRemovedFromDel() throws OXException, SQLException {
        Assert.assertEquals(delCountStart, countDel());
    }
}
