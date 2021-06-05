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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdatesResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;

/**
 * 
 * {@link MWB981Test}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v8.0.0
 */
public class MWB981Test extends AbstractAPIClientSession {

    private static final String COLUMNS = "1,2,3,5,20,23,51,52,108,700,702,703,704,705,707,711,7040";
    private FolderManager folderManager;
    private InfostoreApi infostoreApi;
    private String folderId;

    /**
     * Initializes a new {@link MWB981Test}.
     *
     * @param name
     */
    public MWB981Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderManager = new FolderManager(new FoldersApi(getApiClient()), "1");
        infostoreApi = new InfostoreApi(getApiClient());

        folderId = folderManager.createFolder(folderManager.findInfostoreRoot(), "FolderMWB981Test", FolderManager.INFOSTORE);
        createFile("FileMWB981Test", folderId);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != folderManager) {
                folderManager.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMWB981() throws Exception {
        InfoItemsResponse allResponse = infostoreApi.getAllInfoItems(folderId, COLUMNS, null, null, null, null, null, null);
        checkResponse(allResponse.getError(), allResponse.getErrorDesc());
        Long lastModified = allResponse.getTimestamp();

        InfoItemUpdatesResponse updatesResponse = infostoreApi.getInfoItemUpdates(folderId, COLUMNS, lastModified, null, null, null, null);
        List<Object> data = checkResponse(updatesResponse.getError(), updatesResponse.getErrorDesc(), updatesResponse.getData());
        assertThat("There should be no updated file data.", data.isEmpty());
    }

    private String createFile(String fileName, String parentFolderId) throws ApiException {
        InfoItemUpdateResponse uploadResponse = infostoreApi.uploadInfoItem(parentFolderId, fileName, new byte[] { 34, 45, 35, 23 }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertNotNull(uploadResponse);
        assertNull(uploadResponse.getErrorDesc(), uploadResponse.getError());
        String id = uploadResponse.getData();
        return id;
    }


}
