
package com.openexchange.groupware.infostore;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.tx.UndoableAction;

public class CreateVersionActionTest extends AbstractInfostoreActionTest {

    CreateDocumentAction create = new CreateDocumentAction(null);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        create.setProvider(getProvider());
        create.setContext(getContext());
        create.setDocuments(getDocuments());
        create.setQueryCatalog(getQueryCatalog());
        create.perform();
    }

    @After
    public void tearDown() throws Exception {
        create.undo();
        super.tearDown();
    }

    @Override
    protected UndoableAction getAction() throws Exception {
        final CreateVersionAction createAction = new CreateVersionAction(null);
        createAction.setProvider(getProvider());
        createAction.setContext(getContext());
        createAction.setDocuments(getDocuments());
        createAction.setQueryCatalog(getQueryCatalog());
        return createAction;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            checkInVersionTable(doc);
        }
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            checkNotInVersionTable(doc);
        }
    }

    private void checkInVersionTable(final DocumentMetadata doc) throws OXException, SQLException {
        assertResult("SELECT 1 FROM infostore_document WHERE infostore_id = ? and cid = ?", doc.getId(), getContext().getContextId());
    }

    private void checkNotInVersionTable(final DocumentMetadata doc) throws OXException, SQLException {
        assertNoResult("SELECT 1 FROM infostore_document WHERE infostore_id = ? and cid = ?", doc.getId(), getContext().getContextId());
    }

}
