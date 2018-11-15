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

package com.openexchange.ajax.importexport;

import java.io.File;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactsResponse;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.ImportApi;

/**
 * {@link JPCSVImportTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class JPCSVImportTest extends AbstractConfigAwareAPIClientSession {

    private ImportApi   importApi;
    private ContactsApi contactsApi;
    private String folderId;
    private Set<String> folderToDelete;
    private FoldersApi  foldersApi;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.importApi = new ImportApi(getApiClient());
        foldersApi = new FoldersApi(getApiClient());
        String defaultFolder = getDefaultFolder(getSessionId(), foldersApi);
        folderId = createAndRememberNewFolder(foldersApi, defaultFolder, getApiClient().getUserId());
        contactsApi = new ContactsApi(getApiClient());
    }

    @Override
    public void tearDown() throws Exception {
        if (folderToDelete != null) {
            foldersApi.deleteFolders(getSessionId(), new ArrayList<>(folderToDelete), "0", Long.valueOf(System.currentTimeMillis()), "contacts", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
        }
        super.tearDown();
    }

    /**
     * Creates a new folder and remembers it.
     *
     * @param api The {@link FoldersApi}
     * @param session The user's session
     * @param parent The parent folder
     * @param entity The user id
     * @return The result of the operation
     * @throws ApiException if an API error is occurred
     */
    protected String createAndRememberNewFolder(FoldersApi api, String parent, int entity) throws ApiException {
        FolderPermission perm = new FolderPermission();
        perm.setEntity(entity);
        perm.setGroup(false);
        perm.setBits(403710016);

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        NewFolderBodyFolder folderData = new NewFolderBodyFolder();
        folderData.setModule("contacts");
        folderData.setSubscribed(true);
        folderData.setTitle("chronos_test_" + new UID().toString());
        folderData.setPermissions(permissions);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse createFolder = api.createFolder(parent, getSessionId(), body, "0", null);
        checkResponse(createFolder.getError(), createFolder.getErrorDesc(), createFolder.getData());

        String result = createFolder.getData();
        rememberFolder(result);

        return result;
    }

    /**
     * Keeps track of the specified folder
     *
     * @param folder The folder
     */
    protected void rememberFolder(String folder) {
        if (folderToDelete == null) {
            folderToDelete = new HashSet<>();
        }
        folderToDelete.add(folder);
    }

    /**
     * Retrieves the default contact folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultFolder(String session, FoldersApi foldersApi) throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, "contacts", "1,308", "0", null);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            if ((Boolean) folder.get(1)) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default contact folder!");
    }

    private static final String COLUMNS = "501,502,506,507,508,510,511";

    @Test
    public void testJPImport() throws ApiException {
        File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "jpcontact.csv");
        String importCSV = importApi.importCSV(getSessionId(), folderId, file, null);
        ContactsResponse allContacts = contactsApi.getAllContacts(getSessionId(), folderId, COLUMNS, null, null, null, null);
        Object data = checkResponse(allContacts.getError(), allContacts.getErrorDesc(), allContacts.getData());
        Assert.assertTrue("Wrong response type. Expected ArrayList but received: " + data.getClass().getSimpleName(), data instanceof ArrayList<?>);
        ArrayList<?> contacts = (ArrayList<?>) data;
        Assert.assertFalse("No contacts found.", contacts.isEmpty());
        Assert.assertEquals(1, contacts.size());
        Object con = contacts.get(0);
        Assert.assertTrue("Wrong contact data type. Expected ArrayList but was: " + con.getClass().getSimpleName(), con instanceof ArrayList<?>);
        ArrayList<?> conData = (ArrayList<?>) con;
        Assert.assertEquals(7, conData.size());
        Assert.assertEquals("Wrong contact data:", "Max", conData.get(0).toString());
        Assert.assertEquals("Wrong contact data:", "Mustermann", conData.get(1).toString());
        Assert.assertEquals("Wrong contact data:", "Hauptweg", conData.get(2).toString());
        Assert.assertEquals("Wrong contact data:", "12345", conData.get(3).toString());
        Assert.assertEquals("Wrong contact data:", "Olpe", conData.get(4).toString());
        Assert.assertEquals("Wrong contact data:", "Deutschland", conData.get(5).toString());

        System.out.println(data.toString());
        // TODO check data
    }
}
