package com.openexchange.groupware.attach.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.CreateAttachmentAction;
import com.openexchange.groupware.attach.impl.DeleteAttachmentAction;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.tx.UndoableAction;

public class RemoveAttachmentsActionTest extends AbstractAttachmentActionTest {

	private CreateAttachmentAction createAction = new CreateAttachmentAction();
	private int delCountStart;
	
	public void setUp() throws Exception {
		super.setUp();
		
		createAction.setAttachments(getAttachments());
		createAction.setQueryCatalog(getQueryCatalog());
		createAction.setProvider(getProvider());
		createAction.setContext(getContext());
		
		createAction.perform();
		
		delCountStart = countDel();
		
	}
	
	public void tearDown() throws Exception {
		createAction.undo();
		super.tearDown();
	}
	

	
	@Override
	protected UndoableAction getAction() throws Exception {
		DeleteAttachmentAction deleteAction = new DeleteAttachmentAction();
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
		for(AttachmentMetadata attachment : getAttachments()) {
			AttachmentMetadata loaded = getAttachmentBase().getAttachment(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(),  attachment.getId(), getContext(), getUser(), null);
			assertEquals(attachment, loaded);
		}
		checkRemovedFromDel();
	}
	
	private void checkRemovedFromNormalTable() {
		for(AttachmentMetadata attachment : getAttachments()) {
			try {
				getAttachmentBase().getAttachment(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(),  attachment.getId(), getContext(), getUser(), null);
				fail("Found attachment");
			} catch (OXException x) {
				assertTrue(true);
			}
		}
	}
	
	private int countDel() throws TransactionException, SQLException{
		StringBuilder in = new StringBuilder();
		for(AttachmentMetadata m : getAttachments()) {
			in.append(m.getId()).append(",");
		}
		in.setLength(in.length()-1);
		
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = getProvider().getReadConnection(getContext());
			stmt = readCon.prepareStatement("SELECT count(*) FROM del_attachment WHERE cid = ? and id in ("+in.toString()+")");
			stmt.setInt(1, getContext().getContextId());
			rs = stmt.executeQuery();
			if(!rs.next())
				return -1;
			return rs.getInt(1);
		} finally {
			if(stmt != null)
				stmt.close();
			if(rs != null)
				rs.close();
			if(readCon != null)
				getProvider().releaseReadConnection(getContext(), readCon);
		}
	}

	private void checkDelTable() throws TransactionException, SQLException {
		assertEquals(getAttachments().size(), countDel()-delCountStart);
	}
	
	private void checkRemovedFromDel() throws TransactionException, SQLException {
		assertEquals(delCountStart, countDel());
	}


}
