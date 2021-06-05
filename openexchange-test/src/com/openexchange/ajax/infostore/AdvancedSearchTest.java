/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
