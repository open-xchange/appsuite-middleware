
package com.openexchange.groupware.infostore;

import org.junit.After;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.groupware.infostore.database.impl.UpdateVersionAction;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tx.UndoableAction;

public class UpdateVersionActionTest extends AbstractInfostoreActionTest {

    CreateDocumentAction create = new CreateDocumentAction(null);
    CreateVersionAction create2 = new CreateVersionAction(null);

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

    @After
    public void tearDown() throws Exception {
        create2.undo();
        create.undo();
        super.tearDown();
    }

    @Override
    protected UndoableAction getAction() throws Exception {
        final UpdateVersionAction update = new UpdateVersionAction(null);
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
        for (final DocumentMetadata doc : getUpdatedDocuments()) {
            assertResult("SELECT 1 FROM infostore_document WHERE filename = ? and cid = ? and infostore_id = ?", doc.getFileName(), getContext().getContextId(), doc.getId());
        }
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            assertResult("SELECT 1 FROM infostore_document WHERE filename = ? and cid = ? and infostore_id = ?", doc.getFileName(), getContext().getContextId(), doc.getId());
        }
    }

}
