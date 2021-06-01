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

package com.openexchange.ajax.appPassword;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.Naming;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AppPassword;
import com.openexchange.testing.httpclient.models.AppPasswordApplication;
import com.openexchange.testing.httpclient.models.AppPasswordGetAppsResponse;
import com.openexchange.testing.httpclient.models.AppPasswordListResponse;
import com.openexchange.testing.httpclient.models.AppPasswordRegistrationResponse;
import com.openexchange.testing.httpclient.models.AppPasswordRegistrationResponseData;
import com.openexchange.testing.httpclient.modules.AppPasswordApi;

/**
 * {@link AbstractAppPasswordTest}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AbstractAppPasswordTest extends AbstractConfigAwareAPIClientSession {

    private AppPasswordApi appSpecApi;

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.appPasswords.randomPasswordFormat", "xxxx-xxxx-xxxx");
        configuration.put("com.openexchange.appPasswords.appTypes", "mail, drive, calendar, contacts");
        configuration.put("com.openexchange.appPasswords.mail.scope", "read_contacts, read_mailaccounts, read_mail, write_mail, read_folder, write_folder");
        configuration.put("com.openexchange.appPasswords.mail.displayName", "Mail App");
        configuration.put("com.openexchange.appPasswords.drive.scope", "read_files, write_files");
        configuration.put("com.openexchange.appPasswords.drive.displayName", "Drive App");
        configuration.put("com.openexchange.appPasswords.calendar.scope", "read_calendar, write_calendar, read_folder, write_folder");
        configuration.put("com.openexchange.appPasswords.calendar.displayName", "Calendar App");
        configuration.put("com.openexchange.appPasswords.contacts.scope", "read_contacts, write_contacts");
        configuration.put("com.openexchange.appPasswords.contacts.displayName", "Contacts App");
        return configuration;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Add required capability
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(testUser.getUserId());
        Set<String> cap = new HashSet<String>(1);
        cap.add("app_spec_passwords");
        Credentials userCreds = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface usrInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + OXUserInterface.RMI_NAME);
        Set<String> emptySet = Collections.emptySet();
        usrInterface.changeCapabilities(new Context(I(testUser.getContextId())), user, cap, emptySet, emptySet, userCreds);
        appSpecApi = new AppPasswordApi(getApiClient());
        super.setUpConfiguration();
    }

    /**
     * Get the list of Application Specific Passwords for the user
     *
     * @return
     * @throws ApiException
     */
    protected List<AppPassword> getList() throws ApiException {
        AppPasswordListResponse response = appSpecApi.listApplicationPassword();
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    /**
     * Get list of applications
     * AppPasswordGetAppsResponsegetApps
     *
     * @return
     * @throws ApiException
     */
    protected List<AppPasswordApplication> getApps() throws ApiException {
        AppPasswordGetAppsResponse response = appSpecApi.getApplications();
        return response.getData();
    }

    /**
     * Add a new application specific password
     *
     * @param appType Application type
     * @return
     * @throws ApiException
     */
    protected AppPasswordRegistrationResponseData addPassword(String appType) throws ApiException {
        AppPasswordRegistrationResponse resp = appSpecApi.addApplicationPassword(appType, "Test");
        return checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());
    }

    /**
     * Remove password
     *
     * @param uuid The UUID of the password to remove
     * @throws ApiException
     */
    protected void removePassword(String uuid) throws ApiException {
        appSpecApi.removeApplicationPassword(uuid);
    }

    /**
     * Remove all application specific passwords for the account. Used in cleanup
     *
     * @throws ApiException
     */
    protected void removeAll() throws ApiException {
        List<AppPassword> passwords = appSpecApi.listApplicationPassword().getData();
        for (AppPassword password : passwords) {
            removePassword(password.getUUID());
        }
    }

    /**
     * Reset the Application Specific Api Client
     * resetApiClient
     * 
     * @throws ApiException In case client can't be created
     */
    protected void resetApiClient() throws ApiException {
        appSpecApi = new AppPasswordApi(getApiClient());
    }

}
