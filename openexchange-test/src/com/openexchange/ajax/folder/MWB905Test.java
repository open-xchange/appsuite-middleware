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

package com.openexchange.ajax.folder;

import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractClientSession;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FoldersVisibilityData;
import com.openexchange.testing.httpclient.models.SessionManagementData;
import com.openexchange.testing.httpclient.modules.SessionmanagementApi;

/**
 * {@link MWB905Test} - One problem of multiple folder being inserted with the same name, was that
 * multiple client logins caused the "Collected addresses" folders to be created multiple times.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class MWB905Test extends AbstractClientSession {

    private final static int THREADS = 20;

    /**
     * Login with {@value #THREADS} times and check that the folder
     * <code>Collected addresses</code> is only created once
     *
     * @throws Exception In case test fails
     */
    @Test
    public void testMultipleLogins() throws Exception {
        Runnable login = new Runnable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
                try {
                    generateApiClient(testUser);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        };
        Thread[] insertThreads = new Thread[THREADS];
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i] = new Thread(login);
            insertThreads[i].start();
        }
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i].join();
        }
        ApiClient apiClient = generateApiClient(testUser);
        SessionmanagementApi sessMan = new SessionmanagementApi(apiClient);
        List<SessionManagementData> data = sessMan.all().getData();
        Assert.assertTrue("Failed to login all clients", data.size() >= THREADS + 1);

        int count = 0;
        FolderManager manager = new FolderManager(new FolderApi(apiClient, testUser), "1");
        FoldersVisibilityData contactFolders = manager.getAllFolders("contacts", "300", Boolean.TRUE);
        @SuppressWarnings("unchecked") ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) contactFolders.getPrivate();
        for (ArrayList<?> folder : privateList) {
            Object folderName = folder.get(0);
            if (String.class.isAssignableFrom(folderName.getClass()) && com.openexchange.groupware.i18n.FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME.equals(folderName)) {
                count++;
            }
        }

        Assert.assertTrue("There should be exactly one \"Collected Addresses\" folder.", count == 1);
    }

    /*
     * ============================== HELPERS ==============================
     * Copied from com.openexchange.ajax.framework.AbstractAPIClientSession
     * Use here to avoid premature logins
     */

    private ApiClient generateApiClient(TestUser user) throws ApiException {
        Assert.assertTrue("No test user give", null != testUser);
        ApiClient newClient = new ApiClient();
        setBasePath(newClient);
        newClient.setUserAgent("HTTP API Testing Agent");
        newClient.login(user.getLogin(), user.getPassword());
        return newClient;
    }

    protected void setBasePath(ApiClient newClient) {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        if (hostname == null) {
            hostname = "localhost";
        }
        String protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
        if (protocol == null) {
            protocol = "http";
        }
        newClient.setBasePath(protocol + "://" + hostname + "/ajax");
    }

}
