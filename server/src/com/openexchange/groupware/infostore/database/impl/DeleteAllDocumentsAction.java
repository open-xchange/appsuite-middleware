package com.openexchange.groupware.infostore.database.impl;

import java.sql.SQLException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_DELETEALLDOCUMENTSACTION,
		component = Component.INFOSTORE
)
public class DeleteAllDocumentsAction extends AbstractDocumentListAction {

private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(DeleteDocumentAction.class);
	
	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid SQL Query was sent to the server",
			exceptionId = 0,
			msg = "Invalid SQL Query : %s")
	@Override
	protected void undoAction() throws AbstractOXException {
		if(getDocuments().size() == 0) {
			return;
		}
		final UpdateBlock[] updates = new UpdateBlock[getDocuments().size()];
		int i = 0;
		for(final DocumentMetadata doc : getDocuments()) {
			updates[i++] = new Update(getQueryCatalog().getDocumentInsert()) {

				@Override
				public void fillStatement() throws SQLException {
					fillStmt(stmt,getQueryCatalog().getDocumentFields(),doc,Integer.valueOf(getContext().getContextId()));
				}
				
			};
		}
		
		try {
			doUpdates(updates);
		} catch (final UpdateException e) {
			throw EXCEPTIONS.create(0, e.getSQLException(), e.getStatement());
		}
	}

	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid SQL Query was sent to the server",
			exceptionId = 1,
			msg = "Invalid SQL Query : %s")
	public void perform() throws AbstractOXException {
		if(getDocuments().size() == 0) {
			return;
		}
		final UpdateBlock[] updates = new UpdateBlock[1];
		
		updates[0] = new Update("DELETE FROM infostore WHERE cid = ?"){ // REFACTOR!

			@Override
			public void fillStatement() throws SQLException {
				stmt.setInt(1, getContext().getContextId());
			}
			
		};
		
		
		try {
			doUpdates(updates);
		} catch (final UpdateException e) {
			throw EXCEPTIONS.create(1, e.getSQLException(), e.getStatement());
		}
		
	}

	@Override
	protected Object[] getAdditionals(final DocumentMetadata doc) {
		return null;
	}

}
