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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.infostore;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.infostore.apiclient.InfostoreApiClientTest;
import com.openexchange.file.storage.File;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;

/**
 * {@link AdvancedSearchTest}
 *
 * @author <a href="mailto:alexander.schulze-ardey@open-xchange.com">Alexander Schulze-Ardey</a>
 * @since v7.10.5
 */
public class AdvancedSearchTest extends InfostoreApiClientTest {

    private final List<InfoItemData> createdEntities = new ArrayList<>();

    private final String columns = "" + File.Field.ID.getNumber() + "," + File.Field.TITLE.getNumber() + "," + File.Field.FILENAME.getNumber();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createFiles(10, folderId);
    }

    /**
     * Tests finding the right file using a complex filter.
     *
     * @throws ApiException
     */
    @Test
    public void testSearch() throws ApiException {
        String filename = createdEntities.get(0).getFilename();
        final String filter = "{'filter':[ 'or', [ 'and',  [ '=' , { 'field' : 'filename' }, '" + filename + "'], [ '<' , { 'field' : 'file_size' }, '100']],[ '=' , { 'field' : 'filename' }, 'changelog']]}";

        boolean includeSubfolders = false;
        boolean pregeneratePreviews = false;

        // @formatter:off
        InfoItemsResponse response = infostoreApi.infostoreAdvancedSearch(
            getApiClient().getSession(), 
            columns, 
            filter, 
            this.folderId, 
            File.Field.TITLE.getName(), 
            "asc", 
            I(0), 
            I(100), 
            B(includeSubfolders), 
            B(pregeneratePreviews));
        // @formatter:on

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof ArrayList<?>);
        ArrayList<?> arrayData = (ArrayList<?>) response.getData();
        assertEquals(1, arrayData.size());
        assertEquals(((ArrayList<?>) arrayData.get(0)).get(1), filename);
    }

    /**
     * Tests the result limiting.
     *
     * @throws ApiException
     */
    @Test
    public void testLimit() throws ApiException {
        final String filter = "{'filter': [ '<' , { 'field' : 'file_size' }, '100']}";

        boolean includeSubfolders = false;
        boolean pregeneratePreviews = false;

        // @formatter:off
        InfoItemsResponse response = infostoreApi.infostoreAdvancedSearch(
            getApiClient().getSession(), 
            columns, 
            filter, 
            this.folderId, 
            File.Field.TITLE.getName(), 
            "asc", 
            I(5), 
            I(9), 
            B(includeSubfolders),
            B(pregeneratePreviews));
        // @formatter:off
        
        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof ArrayList<?>);
        ArrayList<?> arrayData = (ArrayList<?>) response.getData();
        assertEquals(5, arrayData.size());
    }

    /**
     * Helper to create a bunch of files used a search base.
     *
     * @param n
     * @param folderId
     * @throws ApiException
     */
    private void createFiles(int n, String folderId) throws ApiException {
        for (int i = 0; i < n; i++) {
            String filename = UUID.randomUUID().toString();

            InfoItemData file = new InfoItemData();
            file.setTitle(filename);
            file.setFilename(filename);
            file.setDescription("more " + filename);
            file.setFileSize(L(filename.getBytes().length));
            file.setFolderId(folderId);

            String fileId = null;
            // @formatter:off
            fileId = uploadInfoItem(fileId, 
                file, 
                MIME_TEXT_PLAIN, 
                null, 
                filename.getBytes(), 
                null, 
                L(filename.getBytes().length), 
                filename);
            // @formatter:off
            file.setId(fileId);

            createdEntities.add(file);
        }
    }
}
