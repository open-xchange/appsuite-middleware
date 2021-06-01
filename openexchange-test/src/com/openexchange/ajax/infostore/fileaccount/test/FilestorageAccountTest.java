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

package com.openexchange.ajax.infostore.fileaccount.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FileAccountResponse;
import com.openexchange.testing.httpclient.models.FileAccountsResponse;
import com.openexchange.testing.httpclient.modules.FilestorageApi;

/**
 * {@link FilestorageAccountTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public final class FilestorageAccountTest extends AbstractAPIClientSession {

    private static final Boolean CONNECTION_CHECK = Boolean.TRUE;
    private static final Boolean NO_CONNECTION_CHECK = Boolean.FALSE;
    private static final String[] POSSIBLE_CAPABILITIES;
    static {
        FileStorageCapability[] allCapabilities = FileStorageCapability.values();
        POSSIBLE_CAPABILITIES = new String[allCapabilities.length];
        for (int i = 0; i < allCapabilities.length; i++) {
            POSSIBLE_CAPABILITIES[i] = allCapabilities[i].name();
        }
    }

    private FilestorageApi api;

    /**
     * Initializes a new {@link FilestorageAccountTest}.
     *
     * @param name
     */
    public FilestorageAccountTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        api = new FilestorageApi(getApiClient());
    }

    @Test
    public void testGetFilestorageAccountCapabilities() throws Throwable {
        FileAccountResponse response = api.getFileAccount("com.openexchange.infostore", "infostore");
        assertNull(response.getError());
        assertNotNull("Response is empty!", response.getData());
        FileAccountData account = response.getData();
        List<String> caps = account.getCapabilities();
        assertNotNull("Response contains no capabilities field!", caps);
        for (String str : caps) {
            boolean contains = false;
            for (String cap : POSSIBLE_CAPABILITIES) {
                if (cap.equals(str)) {
                    contains = true;
                }
            }
            assertTrue("Returns unknown capability " + str, contains);
        }
    }

    @Test
    public void testGetAllFilestorageAccountCapabilities() throws Throwable {
        FileAccountsResponse response = api.getAllFileAccounts(null, NO_CONNECTION_CHECK);
        assertNull(response.getError());
        assertNotNull("Response is empty!", response.getData());
        List<FileAccountData> accounts = response.getData();
        assertFalse(accounts.isEmpty());
        FileAccountData account = accounts.get(0);
        List<String> caps = account.getCapabilities();
        assertNotNull("Response contains no capabilities field!", caps);
        for (String str : caps) {
            boolean contains = false;
            for (String cap : POSSIBLE_CAPABILITIES) {
                if (cap.equals(str)) {
                    contains = true;
                }
            }
            assertTrue("Returns unknown capability " + str, contains);
        }
    }

    @Test
    public void testGetAllFilestorageAccountsWithConnectionCheck() throws Throwable {
        FileAccountsResponse response = api.getAllFileAccounts(null, CONNECTION_CHECK);
        assertNull(response.getError());
        assertNotNull("Response is empty!", response.getData());
        List<FileAccountData> accounts = response.getData();
        assertFalse(accounts.isEmpty());
        for(FileAccountData account : accounts) {
            assertNull(account.getHasError()); //Error flag is not present if no error occurred
        }
    }
}
