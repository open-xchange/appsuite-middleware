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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.infostore.actions.GetDocumentRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.test.common.test.TestInit;

/**
 * {@link Bug44622Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class Bug44622Test extends AbstractInfostoreTest {

    private String fileID;

    /**
     * Initializes a new {@link Bug44622Test}.
     *
     * @param name
     */
    public Bug44622Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        DefaultFile file = new DefaultFile();
        file.setFileName("Bug 44622 Test");
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        NewInfostoreRequest req = new NewInfostoreRequest(file, upload);
        NewInfostoreResponse resp = getClient().execute(req);
        fileID = resp.getID();
    }

    @Test
    public void testBug44622() throws Exception {
        GetDocumentRequest req = new GetDocumentRequest(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()), fileID);
        req.setAdditionalParameters(new Parameter("content_disposition", ""));
        GetDocumentResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        HttpResponse httpResp = resp.getHttpResponse();
        Header[] headers = httpResp.getHeaders("Content-Disposition");
        for (Header header : headers) {
            assertNotNull(header.getValue());
            assertTrue(header.getValue().contains("attachment;"));
        }
    }

}
