package com.openexchange.groupware.attach.actions;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.tx.UndoableAction;

public class CreateAttachmentsActionTest extends AbstractAttachmentActionTest{

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
		for(final AttachmentMetadata attachment : getAttachments()) {
			final AttachmentMetadata loaded = getAttachmentBase().getAttachment(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(),  attachment.getId(), getContext(), getUser(), null);
			assertEquals(attachment, loaded);
		}
		
	}
	
	@Override
	protected void verifyUndone() throws Exception {
		for(final AttachmentMetadata attachment : getAttachments()) {
			try {
				getAttachmentBase().getAttachment(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(),  attachment.getId(), getContext(), getUser(), null);
				fail("The attachment "+attachment.getId()+" was not removed on undo");
			} catch (final OXException x) {
				assertTrue(true);
			}
		}
	}
	

}
