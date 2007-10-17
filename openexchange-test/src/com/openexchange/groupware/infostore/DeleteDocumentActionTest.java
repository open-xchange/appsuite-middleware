package com.openexchange.groupware.infostore;

import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.DeleteDocumentAction;
import com.openexchange.groupware.tx.UndoableAction;

public class DeleteDocumentActionTest extends AbstractInfostoreActionTest {

	CreateDocumentAction create = new CreateDocumentAction();
	
	public void setUp() throws Exception {
		super.setUp();
		create.setProvider(getProvider());
		create.setContext(getContext());
		create.setDocuments(getDocuments());
		create.setQueryCatalog(getQueryCatalog());
		create.perform();
	}
	
	public void tearDown() throws Exception {
		create.undo();
		super.tearDown();
	}
	
	@Override
	protected UndoableAction getAction() throws Exception {
		DeleteDocumentAction deleteAction = new DeleteDocumentAction();
		deleteAction.setProvider(getProvider());
		deleteAction.setContext(getContext());
		deleteAction.setDocuments(getDocuments());
		deleteAction.setQueryCatalog(getQueryCatalog());
		return deleteAction;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		for(DocumentMetadata doc : getDocuments()) {
			assertNoResult("SELECT 1 FROM infostore WHERE cid = ? and id = ?", getContext().getContextId(), doc.getId());
		}
	}


	@Override
	protected void verifyUndone() throws Exception {
		for(DocumentMetadata doc : getDocuments()) {
			assertResult("SELECT 1 FROM infostore WHERE cid = ? and id = ?", getContext().getContextId(), doc.getId());
		}
	}
	
	// Bug 9061
	public void testPossibleToTryMoreThanOnce() throws Exception {
		UndoableAction action = getAction();
		action.perform();
		action.perform();
	}
}
