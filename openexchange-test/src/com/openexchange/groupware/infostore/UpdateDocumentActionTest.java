package com.openexchange.groupware.infostore;

import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.UpdateDocumentAction;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.tx.UndoableAction;

public class UpdateDocumentActionTest extends AbstractInfostoreActionTest {

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
		UpdateDocumentAction update = new UpdateDocumentAction();
		update.setProvider(getProvider());
		update.setContext(getContext());
		update.setDocuments(getUpdatedDocuments());
		update.setOldDocuments(getDocuments());
		update.setQueryCatalog(getQueryCatalog());
		update.setModified(Metadata.COLOR_LABEL_LITERAL, Metadata.FILENAME_LITERAL);
		update.setTimestamp(Long.MAX_VALUE);
		return update;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		for(DocumentMetadata doc : getUpdatedDocuments()) {
			assertResult("SELECT 1 FROM infostore WHERE color_label = ? and cid = ? and id = ?", doc.getColorLabel(), getContext().getContextId(), doc.getId());
		}
	}

	@Override
	protected void verifyUndone() throws Exception {
		for(DocumentMetadata doc : getDocuments()) {
			assertResult("SELECT 1 FROM infostore WHERE color_label = ? and cid = ? and id = ?", doc.getColorLabel(), getContext().getContextId(), doc.getId());
		}
	}

}
