
package com.openexchange.groupware.infostore;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.groupware.infostore.database.impl.DeleteVersionAction;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.tx.UndoableAction;

public class DeleteVersionActionTest extends AbstractInfostoreActionTest {

    private final CreateDocumentAction create = new CreateDocumentAction(null);
    private final CreateVersionAction create2 = new CreateVersionAction(null);

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
        final DeleteVersionAction deleteAction = new DeleteVersionAction(null);
        deleteAction.setProvider(getProvider());
        deleteAction.setContext(getContext());
        deleteAction.setDocuments(getDocuments());
        deleteAction.setQueryCatalog(getQueryCatalog());
        return deleteAction;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            assertNoResult("SELECT 1 FROM infostore_document WHERE cid = ? and infostore_id = ? and version_number = ?", getContext().getContextId(), doc.getId(), doc.getVersion());
        }
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            assertResult("SELECT 1 FROM infostore_document WHERE cid = ? and infostore_id = ? and version_number = ?", getContext().getContextId(), doc.getId(), doc.getVersion());
        }
    }

    // Bug 9061
    @Test
    public void testPossibleToTryMoreThanOnce() throws Exception {
        final UndoableAction action = getAction();
        action.perform();
        action.perform();
    }

    @Test
    public void testBatching() throws Exception {
        final DeleteVersionAction action = (DeleteVersionAction) getAction();
        action.setBatchSize(3); // 2 batches one with 3 and one with 1 entry
        action.perform();
        verifyPerformed();

        // Bug 11305

        List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
        for (int i = 0; i < 1001; i++) {
            DocumentMetadataImpl document = new DocumentMetadataImpl();
            document.setTitle("doc " + i);
            documents.add(document);
        }

        trySlicing(action, documents, 1000);
        trySlicing(action, documents, 1001);
        trySlicing(action, documents, 1002);
        trySlicing(action, documents, 10);
        trySlicing(action, documents, 11);

    }

    private void trySlicing(DeleteVersionAction action, List<DocumentMetadata> documents, int batchSize) {
        Set<String> titles = new HashSet<String>();
        for (DocumentMetadata document : documents) {
            titles.add(document.getTitle());
        }
        List<DocumentMetadata>[] slices = action.getSlices(batchSize, documents);
        for (List<DocumentMetadata> slice : slices) {
            assertTrue(slice.size() <= batchSize);
            for (DocumentMetadata documentMetadata : slice) {
                assertTrue(titles.remove(documentMetadata.getTitle()));
            }
        }
        assertTrue(titles.isEmpty());
    }

}
