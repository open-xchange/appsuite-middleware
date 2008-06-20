package com.openexchange.groupware.infostore;

import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.groupware.infostore.database.impl.DeleteVersionAction;
import com.openexchange.groupware.tx.UndoableAction;

public class DeleteVersionActionTest extends AbstractInfostoreActionTest {


	private final CreateDocumentAction create = new CreateDocumentAction();
	private final CreateVersionAction create2 = new CreateVersionAction();
	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		create.setProvider(getProvider());
		create.setContext(getContext());
		create.setDocuments(getDocuments());
		create.setQueryCatalog(getQueryCatalog());
		create.perform();
		
		create2.setProvider(getProvider());
		create2.setContext(getContext());
		create2.setDocuments(getDocuments());
		create2.setQueryCatalog(getQueryCatalog());
		create2.perform();
		
	
	}
	
	@Override
	public void tearDown() throws Exception {
		create2.undo();
		create.undo();
		super.tearDown();
	}
	
	@Override
	protected UndoableAction getAction() throws Exception {
		final DeleteVersionAction deleteAction = new DeleteVersionAction();
		deleteAction.setProvider(getProvider());
		deleteAction.setContext(getContext());
		deleteAction.setDocuments(getDocuments());
		deleteAction.setQueryCatalog(getQueryCatalog());
		return deleteAction;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		for(final DocumentMetadata doc : getDocuments()) {
			assertNoResult("SELECT 1 FROM infostore_document WHERE cid = ? and infostore_id = ? and version_number = ?", getContext().getContextId(), doc.getId(), doc.getVersion());
		}
	}


	@Override
	protected void verifyUndone() throws Exception {
		for(final DocumentMetadata doc : getDocuments()) {
			assertResult("SELECT 1 FROM infostore_document WHERE cid = ? and infostore_id = ? and version_number = ?", getContext().getContextId(), doc.getId(), doc.getVersion());
		}
	}
	
	// Bug 9061
	public void testPossibleToTryMoreThanOnce() throws Exception {
		final UndoableAction action = getAction();
		action.perform();
		action.perform();
	}


    public void testBatching() throws Exception {
        DeleteVersionAction action = (DeleteVersionAction) getAction();
        action.setBatchSize(3); // 2 batches one with 3 and one with 1 entry
        action.perform();
        verifyPerformed();

    }

}
