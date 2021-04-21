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

package com.openexchange.ajax.contact;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.modules.ContactsApi;

/**
 * {@link ContactProviderTest} - Base class for tests in a different contact provider (i.e com.openexchange.contact.provider.test)
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public abstract class ContactProviderTest extends AbstractAPIClientSession {

    private static final String CONTACT_TEST_PROVIDER_NAME = "c.o.contact.provider.test";
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
        assertThat("The test privider's contact folder must be accessible", B(testProviderFolder.isPresent()), is(Boolean.TRUE));
        return (String) testProviderFolder.get().get(0);
    }

}
