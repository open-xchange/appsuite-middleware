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

package com.openexchange.ajax.attach;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AllRequest;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;

/**
 * {@link Bug26544Test}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug26544Test extends AbstractTestEnvironment {

    private AJAXClient client;

    private Contact contactA;

    private TimeZone tz;

    private TestContext testContext;

    @Before
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        client = testContext.acquireUser().getAjaxClient();
        tz = client.getValues().getTimeZone();
        int folderId = client.getValues().getPrivateContactFolder();
        contactA = new Contact();
        contactA.setDisplayName("Test for bug 26544");
        contactA.setParentFolderID(folderId);
        client.execute(new InsertRequest(contactA)).fillObject(contactA);
        client.execute(new AttachRequest(contactA, "C.txt", new ByteArrayInputStream("Test C".getBytes()), "text/plain")).getId();
        client.execute(new AttachRequest(contactA, "A.txt", new ByteArrayInputStream("Test A".getBytes()), "text/plain")).getId();
        client.execute(new AttachRequest(contactA, "B.txt", new ByteArrayInputStream("Test B".getBytes()), "text/plain")).getId();
        contactA = client.execute(new GetRequest(contactA, tz)).getContact();
        contactA.getLastModifiedOfNewestAttachment().getTime();
    }

    @After
    public void tearDown() throws Exception {
        TestContextPool.backContext(testContext);
    }

    @Test
    public void testAllRequestWithSortOrder() throws OXException, IOException, JSONException {
        int cols[] = { 800, 801, 802, 803, 804, 805, 806 };

        // test sort by id
        AllRequest allRequest = new AllRequest(contactA, cols, 1, Order.ASCENDING);
        AllResponse allResponse = client.execute(allRequest);
        List<AttachmentMetadata> attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(2).getFilename());

        // test sort by filename
        allRequest = new AllRequest(contactA, cols, 803, Order.ASCENDING);
        allResponse = client.execute(allRequest);
        attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(2).getFilename());
    }

    @Test
    public void testAllRequestWithoutSortOrder() throws OXException, IOException, JSONException {
        int cols[] = { 800, 801, 802, 803, 804, 805, 806 };
        AllRequest allRequest = new AllRequest(contactA, cols);
        AllResponse allResponse = client.execute(allRequest);
        List<AttachmentMetadata> attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(2).getFilename());
    }
}
