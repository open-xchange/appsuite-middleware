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

package com.openexchange.ajax.folder;

import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractClientSession;
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

    private ApiClient generateApiClient(TestUser user) throws ApiException {
        Assert.assertTrue("No test user give", null != user);
        return user.generateApiClient();
    }

}
