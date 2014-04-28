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
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.drive.Constants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.drive.FileSizeDisplayItem;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;
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
        metadata = new DocumentMetadataImpl();
        metadata.setFileName(file.getName());
        metadata.setDescription("Test file for testing new find api");
        metadata.setFolderId(client.getValues().getPrivateInfostoreFolder());
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
                client.getValues().getPrivateInfostoreFolder(),
                metadata.getLastModified());
            client.execute(request);
        }
        super.tearDown();
    }

    public void testSearch() throws Exception {
        ActiveFacet fileNameFacet = new ActiveFacet(CommonFacetType.GLOBAL, "global", new Filter(
            Collections.singletonList(Constants.FIELD_FILE_NAME),
            SEARCH));
        QueryRequest request = new QueryRequest(0, 10, Collections.singletonList(fileNameFacet), Module.DRIVE.getIdentifier());
        QueryResponse response = client.execute(request);
        SearchResult result = response.getSearchResult();
        assertTrue("Nothing found in BasicDriveTest", result.getSize() > 0);
    }

    public void testAutocompletion() throws Exception {
        AutocompleteRequest request = new AutocompleteRequest(SEARCH.substring(0, 3), Module.DRIVE.getIdentifier());
        AutocompleteResponse response = client.execute(request);
        assertNotNull("Autocompletion failed in BasicDriveTest", response.getFacets());
    }

    public void testSizeFacet() throws Exception {
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

}
