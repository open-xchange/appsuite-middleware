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
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.ListRequest;
import com.openexchange.ajax.attach.actions.ListResponse;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;

/**
 * {@link Bug24876Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug24876Test extends AbstractTestEnvironment {

    private AJAXClient client;
    private Contact contact;
    private TimeZone tz;
    private long timestamp;
    private int attachmentId;
    private TestContext testContext;

    @Before
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        client = testContext.acquireUser().getAjaxClient();
        tz = client.getValues().getTimeZone();
        int folderId = client.getValues().getPrivateContactFolder();
        contact = new Contact();
        contact.setDisplayName("Test for bug 24876");
        contact.setParentFolderID(folderId);
        client.execute(new InsertRequest(contact)).fillObject(contact);
        attachmentId = client.execute(new AttachRequest(contact, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        contact = client.execute(new GetRequest(contact, tz)).getContact();
        timestamp = contact.getLastModifiedOfNewestAttachment().getTime();
    }

    @After
    public void tearDown() throws Exception {
        TestContextPool.backContext(testContext);
    }

    @Test
    public void testList() throws Exception {
        ListResponse response = client.execute(new ListRequest(contact, new int[] { attachmentId }, new int[] { AttachmentField.CREATION_DATE }, TimeZones.UTC));
        assertEquals("attachment listing did not return the only created attachment", 1, response.getArray().length);
        long toTest = 0;
        for (Object[] values : response) {
            toTest = ((Long) values[response.getColumnPos(AttachmentField.CREATION_DATE)]).longValue();
        }
        assertEquals("time stamp for attachments is wrong in list request", timestamp, toTest);
    }
}
