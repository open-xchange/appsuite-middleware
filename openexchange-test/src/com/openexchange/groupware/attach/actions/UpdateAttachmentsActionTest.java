package com.openexchange.groupware.attach.actions;

import java.util.Arrays;

import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.groupware.attach.impl.UpdateAttachmentAction;
import com.openexchange.groupware.tx.UndoableAction;

public class UpdateAttachmentsActionTest  extends AbstractAttachmentActionTest{
	
	private CreateAttachmentAction createAction = new CreateAttachmentAction();
	private AttachmentMetadata update;
	private AttachmentMetadata original;
	
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
	
	public void tearDown() throws Exception {
		createAction.undo();
		super.tearDown();
	}
	
	@Override
	protected UndoableAction getAction() throws Exception {
		UpdateAttachmentAction updateAction = new UpdateAttachmentAction();
		updateAction.setAttachments(Arrays.asList(update));
		updateAction.setOldAttachments(Arrays.asList(original));
		updateAction.setQueryCatalog(getQueryCatalog());
		updateAction.setProvider(getProvider());
		updateAction.setContext(getContext());
		
		return updateAction;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		AttachmentMetadata loaded = getAttachmentBase().getAttachment(update.getFolderId(), update.getAttachedId(), update.getModuleId(), update.getId(), getContext(), getUser(), null);
		assertEquals(update,loaded);
	}

	@Override
	protected void verifyUndone() throws Exception {
		AttachmentMetadata loaded = getAttachmentBase().getAttachment(update.getFolderId(), update.getAttachedId(), update.getModuleId(), update.getId(), getContext(), getUser(), null);
		assertEquals(original,loaded);
	}

}
