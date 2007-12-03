package com.openexchange.groupware.infostore;

import java.sql.SQLException;

import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.tx.UndoableAction;

public class CreateDocumentActionTest extends AbstractInfostoreActionTest {

	@Override
	protected UndoableAction getAction() throws Exception {
		CreateDocumentAction createAction = new CreateDocumentAction();
		createAction.setProvider(getProvider());
		createAction.setContext(getContext());
		createAction.setDocuments(getDocuments());
		createAction.setQueryCatalog(getQueryCatalog());
		return createAction;
	}

	@Override
	protected void verifyPerformed() throws Exception {	
		for(DocumentMetadata doc : getDocuments()) {
			checkInDocTable(doc);
		}
	}

	@Override
	protected void verifyUndone() throws Exception {
		for(DocumentMetadata doc : getDocuments()) {
			checkNotInDocTable(doc);
		}
	}

	private void checkNotInDocTable(DocumentMetadata doc) throws TransactionException, SQLException {
		assertNoResult("SELECT 1 FROM infostore WHERE id = ? and cid = ?", doc.getId(), getContext().getContextId());	
	}
	
	private void checkInDocTable(DocumentMetadata doc) throws TransactionException, SQLException {
		assertResult("SELECT 1 FROM infostore WHERE id = ? and cid = ?", doc.getId(), getContext().getContextId());
	}

}
