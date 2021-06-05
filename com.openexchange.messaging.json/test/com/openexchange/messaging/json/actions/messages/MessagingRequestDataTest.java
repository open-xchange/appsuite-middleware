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

package com.openexchange.messaging.json.actions.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;

/**
 * {@link MessagingRequestDataTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingRequestDataTest {

    @Test
    public void testParsesLongFolderForm() throws OXException {
        final String messagingService = "com.openexchange.test1";
        final String account = "735";
        final String folder = "some/folder";

        final String folderLong = messagingService + "://" + account + "/" + folder;

        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("folder", folderLong);

        final MessagingRequestData messagingRequest = new MessagingRequestData(req, null, TestRegistryBuilder.buildTestRegistry(), null);

        final MessagingFolderAddress addr = messagingRequest.getLongFolder();

        assertNotNull(addr);

        assertEquals(messagingService, addr.getMessagingService());
        assertEquals(735, addr.getAccount());
        assertEquals(folder, addr.getFolder());
    }

    @Test
    public void testNumberFormatExceptionInAccount() {
        final String messagingService = "com.openexchange.test1";
        final String account = "735abc";
        final String folder = "some/folder";

        final String folderLong = messagingService + "://" + account + "/" + folder;

        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("folder", folderLong);

        final MessagingRequestData messagingRequest = new MessagingRequestData(req, null, TestRegistryBuilder.buildTestRegistry(), null);

        try {
            messagingRequest.getLongFolder();
            fail("Should have failed parsing account number");
        } catch (OXException e) {
            // SUCCESS
        }

    }

    @Test
    public void testFallsBackToFolderForMissingMessagingServiceAndAccountIDAndFolderAndID() throws OXException {
        final String messagingService = "com.openexchange.test1";
        final String account = "735";
        final String folder = "some/folder";

        final String folderLong = messagingService + "://" + account + "/" + folder;

        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("folder", folderLong);

        final MessagingRequestData messagingRequest = new MessagingRequestData(req, null, TestRegistryBuilder.buildTestRegistry(), null);

        assertEquals(messagingService, messagingRequest.getMessagingServiceId());
        assertEquals(735, messagingRequest.getAccountID());
        assertEquals(folder, messagingRequest.getFolderId());

    }

    @Test
    public void testAssemblesLongFolder() throws OXException {
        final String messagingService = "com.openexchange.test1";
        final String account = "735";
        final String folder = "some/folder";

        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", messagingService);
        req.putParameter("account", account);
        req.putParameter("folder", folder);

        final MessagingRequestData messagingRequest = new MessagingRequestData(req, null, TestRegistryBuilder.buildTestRegistry(), null);

        final MessagingFolderAddress addr = messagingRequest.getLongFolder();

        assertNotNull(addr);

        assertEquals(messagingService, addr.getMessagingService());
        assertEquals(735, addr.getAccount());
        assertEquals(folder, addr.getFolder());

    }

}
