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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.infostore.actions.ListInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.ajax.infostore.actions.SearchInfostoreRequest;
import com.openexchange.ajax.infostore.actions.SearchInfostoreResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.file.storage.File.Field;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.drive.Constants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.FileSizeDisplayItem;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;

/**
 * {@link BasicDriveTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class BasicDriveTest extends AbstractFindTest {

    private DocumentMetadata metadata;

    private FolderObject testFolder;

    private static final String SEARCH = "BasicDriveTest";

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
        File file = new File(testDataDir, "BasicDriveTest.tmp");

        String folderName = "findApiDriveTestFolder_" + System.currentTimeMillis();
        testFolder = folderManager.generatePrivateFolder(folderName,
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            client.getValues().getUserId());
        testFolder = folderManager.insertFolderOnServer(testFolder);

        metadata = new DocumentMetadataImpl();
        metadata.setFileName(file.getName());
        metadata.setTitle(file.getName());
        metadata.setDescription("Test file for testing new find api");
        metadata.setFolderId(testFolder.getObjectID());
        metadata.setMeta(Collections.singletonMap("key", (Object) "value"));
        NewInfostoreRequest request = new NewInfostoreRequest(metadata, file);
        NewInfostoreResponse response = client.execute(request);
        assertFalse("Could not create test file for BasicDriveTest", response.hasError());
        metadata.setId(response.getID());
        metadata.setLastModified(response.getTimestamp());
    }

    @Override
    public void tearDown() throws Exception {
        if (metadata.getId() != -1) {
            DeleteInfostoreRequest request = new DeleteInfostoreRequest(
                metadata.getId(),
                testFolder.getObjectID(),
                metadata.getLastModified());
            client.execute(request);
        }
        super.tearDown();
    }

    public void testSearch() throws Exception {
        verifyDocumentExists();
        ActiveFacet fileNameFacet = new ActiveFacet(CommonFacetType.GLOBAL, "global", new Filter(
            Collections.singletonList(Constants.FIELD_FILE_NAME),
            SEARCH));
        QueryRequest request = new QueryRequest(0, 10, Collections.singletonList(fileNameFacet), Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertTrue("Nothing found in BasicDriveTest", result.getSize() > 0);
    }

    public void testSizeFacet() throws Exception {
        verifyDocumentExists();
        ActiveFacet fileSizeFacet = new ActiveFacet(DriveFacetType.FILE_SIZE, FileSizeDisplayItem.Size.MB1.getSize(), new Filter(
            Collections.singletonList(Constants.FIELD_FILE_SIZE),
            FileSizeDisplayItem.Size.MB1.getSize()));
        QueryRequest request = new QueryRequest(0, 10, Collections.singletonList(fileSizeFacet), Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertNotNull("No search result", result);
        assertTrue("Nothing found in file size test", result.getSize() > 0);
        for (Document d : result.getDocuments()) {
            PropDocument file = (PropDocument) d;
            assertTrue("File is too small", (Integer)file.getProps().get("file_size")  >= 1024*1024);
        }
    }

    private void verifyDocumentExists() throws Exception {
        int[] columns = new int[] {1, 20, 700}; // id, folder, title
        SearchInfostoreResponse verificationResponse = client.execute(new SearchInfostoreRequest(testFolder.getObjectID(), metadata.getTitle(), columns));
        JSONArray foundFiles = (JSONArray) verificationResponse.getData();
        assertEquals("Wrong number of documents found via conventional search", 1, foundFiles.length());
        JSONArray desiredFile = foundFiles.getJSONArray(0);
        assertEquals("Wrong id for file found via conventional search", metadata.getId(), Integer.parseInt(desiredFile.get(0).toString()));
        assertEquals("Wrong folder for file found via conventional search", testFolder.getObjectID(), Integer.parseInt(desiredFile.get(1).toString()));
        assertEquals("Wrong title for file found via conventional search", metadata.getTitle(), desiredFile.get(2).toString());
    }

    public void testExclusiveFacetValues() throws Exception {
        List<Facet> facets = autocomplete("");
        Facet folderTypeFacet = findByType(CommonFacetType.FOLDER_TYPE, facets);
        assertNotNull("Missing folder type facet", folderTypeFacet);
        assertEquals("Expected all 3 folder types", 3, folderTypeFacet.getValues().size());

        FacetValue chosenType = folderTypeFacet.getValues().get(0);
        facets = autocomplete("", Collections.singletonList(createFolderTypeFacet(FolderTypeDisplayItem.Type.getByIdentifier(chosenType.getId()))));
        assertNull("Folder type facet was returned", findByType(CommonFacetType.FOLDER_TYPE, facets));
    }

    public void testExclusiveFacets() throws Exception {
        List<Facet> facets = autocomplete("");
        Facet folderTypeFacet = findByType(CommonFacetType.FOLDER_TYPE, facets);
        assertNotNull("Missing folder type facet", folderTypeFacet);
        assertEquals("Expected all 3 folder types", 3, folderTypeFacet.getValues().size());
        facets = autocomplete("", Collections.singletonList(createActiveFacet(CommonFacetType.FOLDER, testFolder.getObjectID(), Filter.NO_FILTER)));
        assertNull("Folder type facet was returned", findByType(CommonFacetType.FOLDER_TYPE, facets));
    }

    public void testConflictsFolderFlag() throws Exception {
        List<Facet> facets = autocomplete("");
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
        int columns[] = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columns[i] = fields[i].getNumber();
        }

        ListInfostoreRequest listRequest = new ListInfostoreRequest(columns);
        listRequest.addItem(new ListItem(metadata));
        ListInfostoreResponse listResponse = client.execute(listRequest);
        Object[] listDocument = listResponse.getArray()[0];

        // Search the same item and compare fields
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
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
                Map<String, Object> asMap = ((JSONObject)listValue).asMap();
                assertTrue(asMap.equals(queryDocument.getProps().get(field.getName())));
            } else {
                assertEquals("Unexpected value for field " + field.getName(), listValue, queryDocument.getProps().get(field.getName()));
            }
        }
    }

    public void testConflictingFacetsCauseException() throws Exception {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(createActiveFacet(CommonFacetType.FOLDER, testFolder.getObjectID(), Filter.NO_FILTER));
        facets.add(createFolderTypeFacet(FolderTypeDisplayItem.Type.PRIVATE));
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest("", Module.DRIVE.getIdentifier(), facets, (Map<String, String>) null, false);
        AutocompleteResponse resp = client.execute(autocompleteRequest);
        assertTrue("Wrong exception", FindExceptionCode.FACET_CONFLICT.equals(resp.getException()));
    }

    protected List<Facet> autocomplete(String prefix) throws Exception {
        return autocomplete(Module.DRIVE, prefix);
    }

    protected List<Facet> autocomplete(String prefix, List<ActiveFacet> facets) throws Exception {
        return autocomplete(Module.DRIVE, prefix, facets);
    }

}
