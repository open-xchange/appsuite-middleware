
package com.openexchange.groupware.attach.actions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashSet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.tx.UndoableAction;

public class CreateAttachmentsActionTest extends AbstractAttachmentActionTest {

    @Override
    protected UndoableAction getAction() throws Exception {
        final CreateAttachmentAction action = new CreateAttachmentAction();
        action.setAttachments(getAttachments());
        action.setQueryCatalog(getQueryCatalog());
        action.setProvider(getProvider());
        action.setContext(getContext());
        return action;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        for (final AttachmentMetadata attachment : getAttachments()) {
            final AttachmentMetadata loaded = getAttachmentBase().getAttachment(getSession(), attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
            assertEquals(attachment, loaded);
        }
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final AttachmentMetadata attachment : getAttachments()) {
            try {
                getAttachmentBase().getAttachment(getSession(), attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), getContext(), getUser(), new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null));
                fail("The attachment " + attachment.getId() + " was not removed on undo");
            } catch (final OXException x) {
                assertTrue(true);
            }
        }
    }
}
