/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.find.drive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.infostore.actions.ListInfostoreResponse;
import com.openexchange.ajax.infostore.actions.SearchInfostoreRequest;
import com.openexchange.ajax.infostore.actions.SearchInfostoreResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.drive.Constants;
import com.openexchange.find.basic.drive.FileSize;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link BasicDriveTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class BasicDriveTest extends AbstractFindTest {

    private static final ActiveFacet ACCOUNT_FACET = new ActiveFacet(CommonFacetType.ACCOUNT, "com.openexchange.infostore://infostore", Filter.NO_FILTER);

    private File metadata;
    private List<File> files;

    private FolderObject testFolder;

    private InfostoreTestManager manager;

    private static final String SEARCH = "BasicDriveTest";
    private static final String SUBFOLDER_SEARCH = "jpg";

    /**
     * Initializes a new {@link BasicDriveTest}.
     *
     * @param name
     */
    public BasicDriveTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        MailConfig.init();
        String testDataDir = MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR);
        java.io.File file = new java.io.File(testDataDir, "BasicDriveTest.tmp");

        String folderName = "findApiDriveTestFolder_" + System.currentTimeMillis();
        testFolder = folderManager.generatePrivateFolder(
            folderName,
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            client.getValues().getUserId());
        testFolder = folderManager.insertFolderOnServer(testFolder);

        manager = new InfostoreTestManager(client);
        metadata = new DefaultFile();
        metadata.setFileName(file.getName());
        metadata.setTitle(file.getName());
        metadata.setDescription("Test file for testing new find api");
        metadata.setFolderId(String.valueOf(testFolder.getObjectID()));
        metadata.setMeta(Collections.singletonMap("key", (Object) "value"));
        manager.newAction(metadata, file);

    }

    @Override
    public void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    public void testSearch() throws Exception {
        verifyDocumentExists();
        ActiveFacet fileNameFacet = new ActiveFacet(CommonFacetType.GLOBAL, "global", new Filter(
            Collections.singletonList(Constants.FIELD_FILE_NAME),
            SEARCH));
        QueryRequest request = new QueryRequest(0, 10, Arrays.asList(ACCOUNT_FACET, fileNameFacet), Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertTrue("Nothing found in BasicDriveTest", result.getSize() > 0);
    }

    public void testSearchInSubFolders() throws Exception {

        // Generate subfolders and files
        int parentId = testFolder.getObjectID();
        int fileCounter = 0;
        final int num_of_subfolders = 3;
        files = new LinkedList<File>();
        for (int x = 0; x < num_of_subfolders; x++) {
            FolderObject subfolder = folderManager.generatePrivateFolder(
                "findApiDriveTestFolder_" + System.currentTimeMillis(),
                FolderObject.INFOSTORE,
                parentId,
                client.getValues().getUserId());
            subfolder = folderManager.insertFolderOnServer(subfolder);
            parentId = subfolder.getObjectID();

            File f = new DefaultFile();
            String name = (fileCounter++) + ".jpg";
            f.setFileName(name);
            f.setTitle(name);
            f.setDescription("No desc");
            f.setFolderId(String.valueOf(parentId));
            f.setMeta(Collections.singletonMap("key", (Object) "value"));
            manager.newAction(f);
            files.add(f);
            f = new DefaultFile();
            name = (fileCounter++) + ".jpg";
            f.setFileName(name);
            f.setTitle(name);
            f.setDescription("No desc");
            f.setFolderId(String.valueOf(parentId));
            f.setMeta(Collections.singletonMap("key", (Object) "value"));
            manager.newAction(f);
            files.add(f);
        }

        ActiveFacet fileNameFacet = new ActiveFacet(CommonFacetType.GLOBAL, "global", new Filter(
            Collections.singletonList(Constants.FIELD_FILE_NAME),
            SUBFOLDER_SEARCH));
        ActiveFacet fileNameFacet2 = new ActiveFacet(CommonFacetType.FOLDER, String.valueOf(testFolder.getObjectID()), new Filter(
            Collections.singletonList(Constants.FIELD_FILE_NAME),
            SUBFOLDER_SEARCH));
        List<ActiveFacet> facetList = new LinkedList<ActiveFacet>();
        facetList.add(ACCOUNT_FACET);
        facetList.add(fileNameFacet);
        facetList.add(fileNameFacet2);
        QueryRequest request = new QueryRequest(0, 10, facetList, Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertTrue("Found " + result.getSize() + " instead of " + files.size() + " files.", result.getSize() == files.size());
    }

    public void testSizeFacet() throws Exception {
        verifyDocumentExists();
        ActiveFacet fileSizeFacet = new ActiveFacet(DriveFacetType.FILE_SIZE, FileSize.MB1.getSize(), new Filter(
            Collections.singletonList(Constants.FIELD_FILE_SIZE),
            FileSize.MB1.getSize()));
        QueryRequest request = new QueryRequest(0, 10, Arrays.asList(ACCOUNT_FACET, fileSizeFacet), Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertNotNull("No search result", result);
        assertTrue("Nothing found in file size test", result.getSize() > 0);
        for (Document d : result.getDocuments()) {
            PropDocument file = (PropDocument) d;
            assertTrue("File is too small", (Integer) file.getProps().get("file_size") >= 1024 * 1024);
        }
    }

    private void verifyDocumentExists() throws Exception {
        int[] columns = new int[] {1, 20, 700}; // id, folder, title
        SearchInfostoreResponse verificationResponse = client.execute(new SearchInfostoreRequest(testFolder.getObjectID(), metadata.getTitle(), columns));
        JSONArray foundFiles = (JSONArray) verificationResponse.getData();
        assertEquals("Wrong number of documents found via conventional search", 1, foundFiles.length());
        JSONArray desiredFile = foundFiles.getJSONArray(0);
        assertEquals("Wrong id for file found via conventional search", metadata.getId(), desiredFile.get(0).toString());
        assertEquals("Wrong folder for file found via conventional search", testFolder.getObjectID(), Integer.parseInt(desiredFile.get(1).toString()));
        assertEquals("Wrong title for file found via conventional search", metadata.getTitle(), desiredFile.get(2).toString());
    }

    public void testExclusiveFacetValues() throws Exception {
        List<Facet> facets = autocomplete("", Collections.singletonList(ACCOUNT_FACET));
        Facet folderTypeFacet = findByType(CommonFacetType.FOLDER_TYPE, facets);
        assertNotNull("Missing folder type facet", folderTypeFacet);
        assertEquals("Expected all 3 folder types", 3, ((ExclusiveFacet) folderTypeFacet).getValues().size());

        FacetValue chosenType = ((ExclusiveFacet) folderTypeFacet).getValues().get(0);
        facets = autocomplete("", Arrays.asList(ACCOUNT_FACET, createFolderTypeFacet(FolderType.getByIdentifier(chosenType.getId()))));
        assertNull("Folder type facet was returned", findByType(CommonFacetType.FOLDER_TYPE, facets));
    }

    public void testExclusiveFacets() throws Exception {
        List<Facet> facets = autocomplete("", Collections.singletonList(ACCOUNT_FACET));
        Facet folderTypeFacet = findByType(CommonFacetType.FOLDER_TYPE, facets);
        assertNotNull("Missing folder type facet", folderTypeFacet);
        assertEquals("Expected all 3 folder types", 3, ((ExclusiveFacet) folderTypeFacet).getValues().size());
        facets = autocomplete("", Arrays.asList(ACCOUNT_FACET, createActiveFacet(CommonFacetType.FOLDER, testFolder.getObjectID(), Filter.NO_FILTER)));
        assertNull("Folder type facet was returned", findByType(CommonFacetType.FOLDER_TYPE, facets));
    }

    public void testConflictsFolderFlag() throws Exception {
        List<Facet> facets = autocomplete("", Collections.singletonList(ACCOUNT_FACET));
        Facet folderTypeFacet = findByType(CommonFacetType.FOLDER_TYPE, facets);
        assertNotNull("Missing folder type facet", folderTypeFacet);
        boolean found = false;
        String prefix = "conflicts:";
        for (String flag : folderTypeFacet.getFlags()) {
            int index = flag.indexOf(prefix);
            if (index > -1) {
                if (CommonFacetType.FOLDER.getId().equals(flag.substring(index + prefix.length()))) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue("Flag not found", found);
    }

    public void testDefaultColumnsAreEquivalentToListRequest() throws Exception {
        // 20,23,1,5,700,702,703,704,707,3 from api.js
        Field[] fields = new Field[] {Field.FOLDER_ID, Field.META, Field.ID, Field.LAST_MODIFIED,
        Field.TITLE, Field.FILENAME, Field.FILE_MIMETYPE, Field.FILE_SIZE,
        Field.LOCKED_UNTIL, Field.MODIFIED_BY};
        testWithFields(fields, false);
    }

    public void testWithExplicitColumns1() throws Exception {
        // 20,23,1,5,700,702,703,704,707,3 from api.js
        Field[] fields = new Field[] {Field.FOLDER_ID, Field.META, Field.ID, Field.LAST_MODIFIED,
        Field.TITLE, Field.FILENAME, Field.FILE_MIMETYPE, Field.FILE_SIZE,
        Field.LOCKED_UNTIL, Field.MODIFIED_BY};
        testWithFields(fields, true);
    }

    public void testWithExplicitColumns2() throws Exception {
        Field[] fields = new Field[] {Field.FOLDER_ID, Field.ID, Field.META, Field.LAST_MODIFIED,
        Field.TITLE, Field.FILENAME };
        testWithFields(fields, true);
    }

    private void testWithFields(Field fields[], boolean withColumns) throws Exception {
        String columns[] = new String[fields.length];
        int intColumns[] = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            intColumns[i] = fields[i].getNumber();
            columns[i] = Integer.toString(fields[i].getNumber());
        }

        ListInfostoreRequest listRequest = new ListInfostoreRequest(intColumns);
        listRequest.addItem(new ListItem(metadata));
        ListInfostoreResponse listResponse = client.execute(listRequest);
        Object[] listDocument = listResponse.getArray()[0];

        // Search the same item and compare fields
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(ACCOUNT_FACET);
        facets.add(createActiveFieldFacet(DriveFacetType.FILE_NAME, Constants.FIELD_FILE_NAME, SEARCH));
        facets.add(createActiveFacet(CommonFacetType.FOLDER, testFolder.getObjectID(), Filter.NO_FILTER));
        QueryRequest queryRequest;
        if (withColumns) {
            queryRequest = new QueryRequest(true, 0, 10, facets, null, Module.DRIVE.getIdentifier(), columns);
        } else {
            queryRequest = new QueryRequest(0, 10, facets, Module.DRIVE.getIdentifier());
        }
        QueryResponse queryResponse = client.execute(queryRequest);
        PropDocument queryDocument = (PropDocument) queryResponse.getSearchResult().getDocuments().get(0);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Object listValue = listDocument[i];
            if (field == Field.META) {
                Map<String, Object> asMap = (Map<String, Object>) listValue;
                assertTrue(asMap.equals(queryDocument.getProps().get(field.getName())));
            } else {
                assertEquals("Unexpected value for field " + field.getName(), listValue, queryDocument.getProps().get(field.getName()));
            }
        }
    }

    public void testConflictingFacetsCauseException() throws Exception {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(ACCOUNT_FACET);
        facets.add(createActiveFacet(CommonFacetType.FOLDER, testFolder.getObjectID(), Filter.NO_FILTER));
        facets.add(createFolderTypeFacet(FolderType.PRIVATE));
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest("", Module.DRIVE.getIdentifier(), facets, (Map<String, String>) null, false);
        AutocompleteResponse resp = client.execute(autocompleteRequest);
        assertTrue("Wrong exception", FindExceptionCode.FACET_CONFLICT.equals(resp.getException()));
    }

    public void testTokenizedQuery() throws Exception {
        // description: "Test file for testing new find api"
        SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.DRIVE, "Test" + " " + "api", Collections.singletonList(ACCOUNT_FACET)));
        List<PropDocument> documents = query(Module.DRIVE, Arrays.asList(ACCOUNT_FACET, createActiveFacet(globalFacet)));
        assertTrue("no document found", 0 < documents.size());
        assertNotNull("document not found", findByProperty(documents, "id", metadata.getId()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.DRIVE, "\"Test file for\"", Collections.singletonList(ACCOUNT_FACET)));
        documents = query(Module.DRIVE, Arrays.asList(ACCOUNT_FACET, createActiveFacet(globalFacet)));
        assertTrue("no document found", 0 < documents.size());
        assertNotNull("document not found", findByProperty(documents, "id", metadata.getId()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.DRIVE, "\"Test file murks\"", Collections.singletonList(ACCOUNT_FACET)));
        documents = query(Module.DRIVE, Arrays.asList(ACCOUNT_FACET, createActiveFacet(globalFacet)));
        assertTrue("document found", 0 == documents.size());
    }

    public void testFolderTypeFacet() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        try {
            FolderType[] typesInOrder = new FolderType[] { FolderType.PRIVATE, FolderType.PUBLIC, FolderType.SHARED };
            AJAXClient[] clients = new AJAXClient[] { client, client, client2 };
            FolderObject[] folders = new FolderObject[3];
            folders[0] = folderManager.insertFolderOnServer(folderManager.generatePrivateFolder(
                randomUID(),
                FolderObject.INFOSTORE,
                client.getValues().getPrivateInfostoreFolder(),
                client.getValues().getUserId()));
            folders[1] = folderManager.insertFolderOnServer(folderManager.generatePublicFolder(
                randomUID(),
                FolderObject.INFOSTORE,
                FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
                client.getValues().getUserId()));
            folders[2] = folderManager.insertFolderOnServer(folderManager.generateSharedFolder(
                randomUID(),
                FolderObject.INFOSTORE,
                client.getValues().getPrivateInfostoreFolder(),
                client.getValues().getUserId(),
                client2.getValues().getUserId()));

            File[] documents = new File[3];
            documents[0] = new DefaultFile(metadata);
            documents[0].setTitle(randomUID());
            documents[0].setFolderId(String.valueOf(folders[0].getObjectID()));
            documents[1] = new DefaultFile(metadata);
            documents[1].setTitle(randomUID());
            documents[1].setFolderId(String.valueOf(folders[1].getObjectID()));
            documents[2] = new DefaultFile(metadata);
            documents[2].setTitle(randomUID());
            documents[2].setFolderId(String.valueOf(folders[2].getObjectID()));
            manager.newAction(documents[0]);
            manager.newAction(documents[1]);
            manager.newAction(documents[2]);

            for (int i = 0; i < 3; i++) {
                FolderType folderType = typesInOrder[i];
                List<Facet> facets = autocomplete(clients[i], "", Collections.singletonList(ACCOUNT_FACET));
                ExclusiveFacet folderTypeFacet = (ExclusiveFacet) findByType(CommonFacetType.FOLDER_TYPE, facets);
                FacetValue typeValue = findByValueId(folderType.getIdentifier(), folderTypeFacet);
                List<PropDocument> docs = query(clients[i], Arrays.asList(ACCOUNT_FACET, createActiveFacet(folderTypeFacet, typeValue)));
                PropDocument[] foundDocs = new PropDocument[3];
                for (PropDocument doc : docs) {
                    Map<String, Object> props = doc.getProps();
                    if (documents[0].getTitle().equals(props.get("title"))) {
                        foundDocs[0] = doc;
                        continue;
                    } else if (documents[1].getTitle().equals(props.get("title"))) {
                        foundDocs[1] = doc;
                        continue;
                    } else if (documents[2].getTitle().equals(props.get("title"))) {
                        foundDocs[2] = doc;
                        continue;
                    }
                }

                switch (folderType) {
                    case PRIVATE:
                        assertNotNull("Private document not found", foundDocs[0]);
                        assertNull("Public document found but should not", foundDocs[1]);
                        assertNotNull("Shared document not found", foundDocs[2]);
                        break;

                    case PUBLIC:
                        assertNull("Private document found but should not", foundDocs[0]);
                        assertNotNull("Public document not found", foundDocs[1]);
                        assertNull("Shared document found but should not", foundDocs[2]);
                        break;

                    case SHARED:
                        assertNull("Private document found but should not", foundDocs[0]);
                        assertNull("Public document found but should not", foundDocs[1]);
                        assertNotNull("Shared document not found", foundDocs[2]);
                        break;
                }
            }
        } finally {
            client2.logout();
        }
    }

    public void testDeletedFilesAreIgnored() throws Exception {
        FolderObject deletedFolder = folderManager.insertFolderOnServer(folderManager.generatePrivateFolder(
            randomUID(),
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            client.getValues().getUserId()));
        File deletedDocument = new DefaultFile(metadata);
        deletedDocument.setTitle(randomUID());
        deletedDocument.setFolderId(String.valueOf(deletedFolder.getObjectID()));
        manager.newAction(deletedDocument);
        folderManager.deleteFolderOnServer(deletedFolder);
        Folder reloadedFolder = client.execute(new GetRequest(EnumAPI.OX_NEW, deletedFolder.getObjectID())).getStorageFolder();
        FolderObject trashFolder = client.execute(new GetRequest(EnumAPI.OX_NEW, reloadedFolder.getParentID())).getFolder();
        assertEquals("Wrong type", FolderObject.TRASH, trashFolder.getType());

        List<Facet> autocompleteResponse = autocomplete(deletedDocument.getTitle(), Collections.singletonList(ACCOUNT_FACET));
        DefaultFacet folderTypeFacet = (DefaultFacet) findByType(CommonFacetType.FOLDER_TYPE, autocompleteResponse);
        ActiveFacet[] folderTypeFacets = new ActiveFacet[3];
        folderTypeFacets[0] = createActiveFacet(folderTypeFacet, findByValueId(FolderType.PRIVATE.getIdentifier(), folderTypeFacet));
        folderTypeFacets[1] = createActiveFacet(folderTypeFacet, findByValueId(FolderType.PUBLIC.getIdentifier(), folderTypeFacet));
        folderTypeFacets[2] = createActiveFacet(folderTypeFacet, findByValueId(FolderType.SHARED.getIdentifier(), folderTypeFacet));

        ActiveFacet fileNameFacet = createActiveFacet((SimpleFacet) findByType(DriveFacetType.FILE_NAME, autocompleteResponse));
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(ACCOUNT_FACET);
        facets.add(fileNameFacet);
        facets.add(createActiveFacet(CommonFacetType.FOLDER, reloadedFolder.getID(), Filter.NO_FILTER));
        List<PropDocument> documents = query(client, facets);
        assertEquals("Wrong number of documents", 1, documents.size());
        assertEquals("Wrong document", deletedDocument.getTitle(), (String) documents.get(0).getProps().get("title"));

        facets.clear();
        facets.add(ACCOUNT_FACET);
        facets.add(fileNameFacet);
        documents = query(client, facets);
        assertEquals("Wrong number of documents", 0, documents.size());

        for (int i = 0; i < 3; i++) {
            facets.clear();
            facets.add(ACCOUNT_FACET);
            facets.add(fileNameFacet);
            facets.add(folderTypeFacets[i]);
            documents = query(client, facets);
            assertEquals("Wrong number of documents. Document found in " + folderTypeFacets[i].getValueId() + " folder.", 0, documents.size());
        }

    }

//    Takes half an hour do create and delete all those folders...
//    public void testFolderChunking() throws Exception {
//        FolderObject first = null;
//        FolderObject middle = null;
//        FolderObject last = null;
//        for (int i = 0; i < 2002; i++) {
//            FolderObject folder = folderManager.insertFolderOnServer(folderManager.generatePrivateFolder(
//                randomUID(),
//                FolderObject.INFOSTORE,
//                client.getValues().getPrivateInfostoreFolder(),
//                client.getValues().getUserId()));
//            if (i == 0) {
//                first = folder;
//            } else if (i == 1000) {
//                middle = folder;
//            } else if (i == 2001) {
//                last = folder;
//            }
//        }
//
//        DocumentMetadata firstDoc = new DocumentMetadataImpl(metadata);
//        firstDoc.setTitle("zzz" + randomUID());
//        firstDoc.setFolderId(first.getObjectID());
//
//        DocumentMetadata middleDoc = new DocumentMetadataImpl(metadata);
//        middleDoc.setTitle("aaa" + randomUID());
//        middleDoc.setFolderId(middle.getObjectID());
//
//        DocumentMetadata lastDoc = new DocumentMetadataImpl(metadata);
//        lastDoc.setTitle("012" + randomUID());
//        lastDoc.setFolderId(last.getObjectID());
//        manager.newAction(firstDoc);
//        manager.newAction(middleDoc);
//        manager.newAction(lastDoc);
//
//        List<Facet> facets = autocomplete(client, "");
//        ExclusiveFacet folderTypeFacet = (ExclusiveFacet) findByType(CommonFacetType.FOLDER_TYPE, facets);
//        FacetValue typeValue = findByValueId(FolderType.PRIVATE.getIdentifier(), folderTypeFacet);
//        List<PropDocument> docs = query(client, Collections.singletonList(createActiveFacet(folderTypeFacet, typeValue)));
//
//        List<String> found = new ArrayList<String>(3);
//        for (PropDocument doc : docs) {
//            String title = (String) doc.getProps().get("title");
//            if (title.equals(firstDoc.getTitle()) || title.equals(middleDoc.getTitle()) || title.equals(lastDoc.getTitle())) {
//                found.add(title);
//            }
//        }
//
//        assertEquals("Did not find all documents", 3, found.size());
//        assertEquals("Wrong order", lastDoc.getTitle(), found.get(0));
//        assertEquals("Wrong order", middleDoc.getTitle(), found.get(1));
//        assertEquals("Wrong order", firstDoc.getTitle(), found.get(2));
//    }

    protected List<Facet> autocomplete(AJAXClient client, String prefix, List<ActiveFacet> facets) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.DRIVE.getIdentifier(), facets);
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

    protected List<PropDocument> query(AJAXClient client, List<ActiveFacet> facets) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.DRIVE.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    protected List<Facet> autocomplete(String prefix) throws Exception {
        return autocomplete(Module.DRIVE, prefix);
    }

    protected List<Facet> autocomplete(String prefix, List<ActiveFacet> facets) throws Exception {
        return autocomplete(Module.DRIVE, prefix, facets);
    }

}
