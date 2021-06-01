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

package com.openexchange.ajax.importexport;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;
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
        folderId = createAndRememberNewFolder(foldersApi, getDefaultFolder(), getApiClient().getUserId().intValue());
        contactsApi = new ContactsApi(getApiClient());
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
        perm.setEntity(I(entity));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));
        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        NewFolderBodyFolder folderData = new NewFolderBodyFolder();
        folderData.setModule("contacts");
        folderData.setSubscribed(Boolean.TRUE);
        folderData.setTitle(this.getClass().getSimpleName() + new UID().toString());
        folderData.setPermissions(permissions);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse createFolder = api.createFolder(parent, body, "0", null, null, null);
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
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultFolder() throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders("contacts", "1,308", "0", null, Boolean.TRUE);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            if (((Boolean) folder.get(1)).booleanValue()) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default contact folder!");
    }

    private static final String COLUMNS = "501,502,506,507,508,510,511";

    @Test
    public void testJPImport() throws ApiException {
        File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "jpcontact.csv");
        importApi.importCSV(folderId, file, Boolean.FALSE, null);
        ContactsResponse allContacts = contactsApi.getAllContacts(folderId, COLUMNS, null, null, null);
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
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(1990, 0, 1);
        Assert.assertEquals("Wrong contact data:", String.valueOf(cal.getTimeInMillis()), conData.get(6).toString());
    }
}
