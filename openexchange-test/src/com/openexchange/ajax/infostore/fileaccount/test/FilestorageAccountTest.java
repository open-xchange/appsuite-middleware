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
 *    trademarks of the OX Software GmbH group of companies.
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
        FileAccountResponse response = api.getFileAccount(getApiClient().getSession(), "com.openexchange.infostore", "infostore");
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
        FileAccountsResponse response = api.getAllFileAccounts(getApiClient().getSession(), null);
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

}
