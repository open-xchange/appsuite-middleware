package com.openexchange.groupware.update;


import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.update.tasks.ClearLeftoverAttachmentsUpdateTask;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClearLeftoverAttachmentsTest extends UpdateTest {
    private AttachmentBase attachmentBase;

    private static final int OFFSET = 3;

    private List<AttachmentImpl> attachments = new ArrayList<AttachmentImpl>();

    public void setUp() throws Exception {
        super.setUp();

        attachmentBase = new AttachmentBaseImpl(getProvider());
        attachmentBase.setTransactional(true);
        attachmentBase.startTransaction();

    }

    public void tearDown() throws Exception {
        for(AttachmentImpl attachment : attachments) {
            try {
                attachmentBase.detachFromObject(22,22,22,new int[]{attachment.getId()}, ctx, user, null);
            } catch (OXException x) {}
        }
        super.tearDown();
    }

    public void testFixSchema() throws AbstractOXException, SQLException {
        createAttachments();
        resetSequenceCounter();
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
        assertNoLeftoversInDatabase();
        assertRemovedFiles();
    }

    public void testRunMultipleTimesNonDestructively() throws AbstractOXException, SQLException {
        createAttachments();
        resetSequenceCounter();
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
    }

    public void testIgnoreMissingFiles() throws AbstractOXException, SQLException {
        createAttachments();
        resetSequenceCounter();
        removeSomeFiles();
        new ClearLeftoverAttachmentsUpdateTask().perform(schema, existing_ctx_id);
        assertNoLeftoversInDatabase();
        assertRemovedFiles();
    }

    private void createAttachments() throws OXException {
        AttachmentImpl original  = new AttachmentImpl();
        original.setAttachedId(22);
        original.setComment("");
        original.setCreatedBy(user_id);
        original.setCreationDate(new Date());
        original.setFolderId(22);
        original.setModuleId(22);
        original.setFileMIMEType("text/plain");
        original.setFilename("blupp.txt");

        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);
        createCopy(original);

        
    }

    private void createCopy(AttachmentImpl original) throws OXException {
        AttachmentImpl copy = new AttachmentImpl(original);
        attachmentBase.attachToObject(copy,new ByteArrayInputStream(new byte[10]),ctx,user,null);
        attachments.add(copy);
    }

    private void resetSequenceCounter() throws SQLException, DBPoolingException {
        exec("UPDATE sequence_attachment SET id = ? WHERE cid = ?",attachments.get(OFFSET).getId(), existing_ctx_id);
    }

    private void removeSomeFiles() throws FileStorageException, FilestoreException {
        FileStorage fs = FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,getProvider());
        fs.deleteFile(attachments.get(OFFSET+2).getFileId());
        fs.deleteFile(attachments.get(OFFSET+3).getFileId());
    }

    private void assertNoLeftoversInDatabase() throws SQLException, DBPoolingException {
        assertNoResults("SELECT 1 FROM prg_attachment JOIN sequence_attachment ON prg_attachment.cid = sequence_attachment.cid WHERE prg_attachment.id > sequence_attachment.id AND prg_attachment.cid = ?",existing_ctx_id);              
    }

    private void assertRemovedFiles() throws FileStorageException, FilestoreException {
        FileStorage fs = FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,getProvider());
        for(int i = OFFSET+1; i < attachments.size(); i++) {
            try {
                fs.getFile(attachments.get(i).getFileId());
                assertFalse("File of attachment "+i+" was not deleted",true);
            } catch (FileStorageException x) {
                assertTrue(true); // Specific enough?
            }
        }
    }
}
