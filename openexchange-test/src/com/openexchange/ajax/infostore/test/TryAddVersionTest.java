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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link TryAddVersionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class TryAddVersionTest extends AbstractInfostoreTest {

    private List<String> ids;
    private final int[] COLUMNS = new int[] { 700, 702, 710, 711 };
    private final String filename = "bug.eml";

    public TryAddVersionTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ids = new ArrayList<>(2);
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f);
        NewInfostoreResponse resp = getClient().execute(req);
        ids.add(resp.getID());
    }

    @Test
    public void testAddVersion() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, true);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertEquals(2, uploaded.getNumberOfVersions());
    }

    @Test
    public void testFallback() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("tryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, false);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertFalse("tryAddVersion".equals(uploaded.getFileName()));
        assertTrue(uploaded.getFileName().endsWith("(1)"));
        ids.add(uploaded.getId());
    }

    @Test
    public void testBug55425() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("TryAddVersion");
        java.io.File f = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename);
        NewInfostoreRequest req = new NewInfostoreRequest(file, f, true);
        NewInfostoreResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        ids.add(resp.getID());
        GetInfostoreRequest getReq = new GetInfostoreRequest(resp.getID(), COLUMNS);
        GetInfostoreResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);
        File uploaded = getResp.getDocumentMetadata();
        assertEquals(2, uploaded.getNumberOfVersions());
    }

}
