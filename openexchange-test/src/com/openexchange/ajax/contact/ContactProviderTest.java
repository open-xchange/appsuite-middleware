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

package com.openexchange.ajax.contact;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.contact.provider.ContactsProviders;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CapabilityData;
import com.openexchange.testing.httpclient.modules.CapabilitiesApi;
import com.openexchange.testing.httpclient.modules.ContactsApi;

/**
 * {@link ContactProviderTest} - Base class for tests in a different contact provider (i.e com.openexchange.contact.provider.test)
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public abstract class ContactProviderTest extends AbstractAPIClientSession {

    private static final String CONTACT_TEST_PROVIDER_ID = com.openexchange.contact.provider.test.impl.TestContactsProvider.PROVIDER_ID;
    private static final String CONTACT_TEST_PROVIDER_NAME = com.openexchange.contact.provider.test.impl.TestContactsProvider.PROVIDER_DISPLAY_NAME;
    private static final String PARENT_FOLDER = "1";
    private static final String FOLDER_COLUMNS = "1,300";

    protected FolderManager folderManager;
    protected ContactsApi contactsApi;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderManager = new FolderManager(new FolderApi(getApiClient(), testUser), String.valueOf(EnumAPI.OX_NEW.getTreeId()));
        contactsApi = new ContactsApi(getApiClient());
    }

    /**
     * Internal method to get the ID of the test provider's contact folder
     *
     * @return The ID of the contact folder provided by the test provider
     * @throws ApiException
     */
    protected String getAccountFolderId() throws ApiException {
        ArrayList<ArrayList<Object>> folders = folderManager.listFolders(PARENT_FOLDER, FOLDER_COLUMNS, Boolean.FALSE);
        assertThat(I(folders.size()), greaterThan(I(1)));
        Optional<ArrayList<Object>> testProviderFolder = folders.stream().filter(folder -> folder.get(1).equals(CONTACT_TEST_PROVIDER_NAME)).findFirst();
        if (false == testProviderFolder.isPresent()) {
            String capability = ContactsProviders.getCapabilityName(CONTACT_TEST_PROVIDER_ID);
            CapabilitiesApi capabilitiesApi = new CapabilitiesApi(getApiClient());
            CapabilityData capabilityData = capabilitiesApi.getCapability(capability).getData();
            assertTrue("Capability " + capability + " not set", null != capabilityData && capability.equals(capabilityData.getId()));
        }
        assertThat("The test provider's contact folder must be accessible", B(testProviderFolder.isPresent()), is(Boolean.TRUE));
        return (String) testProviderFolder.get().get(0);
    }

}
