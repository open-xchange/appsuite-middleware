
package com.openexchange.groupware.attach.actions;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.After;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.groupware.attach.impl.UpdateAttachmentAction;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.tx.UndoableAction;

public class UpdateAttachmentsActionTest extends AbstractAttachmentActionTest {

    private final CreateAttachmentAction createAction = new CreateAttachmentAction();
    private AttachmentMetadata update;
    private AttachmentMetadata original;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        createAction.setAttachments(getAttachments());
        createAction.setQueryCatalog(getQueryCatalog());
        createAction.setProvider(getProvider());
        createAction.setContext(getContext());

        createAction.perform();

        original = getAttachments().get(0);
        update = new AttachmentImpl(original);
        update.setFilename("otherfile.txt");
    }

    @After
    public void tearDown() throws Exception {
        createAction.undo();
        super.tearDown();
    }

    @Override
    protected UndoableAction getAction() throws Exception {
        final UpdateAttachmentAction updateAction = new UpdateAttachmentAction();
        updateAction.setAttachments(Arrays.asList(update));
        updateAction.setOldAttachments(Arrays.asList(original));
        updateAction.setQueryCatalog(getQueryCatalog());
        updateAction.setProvider(getProvider());
        updateAction.setContext(getContext());

        return updateAction;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        final AttachmentMetadata loaded = getAttachmentBase().getAttachment(getSession(), update.getFolderId(), update.getAttachedId(), update.getModuleId(), update.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
        assertEquals(update, loaded);
    }

    @Override
    protected void verifyUndone() throws Exception {
        final AttachmentMetadata loaded = getAttachmentBase().getAttachment(getSession(), update.getFolderId(), update.getAttachedId(), update.getModuleId(), update.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
        assertEquals(original, loaded);
    }
}
