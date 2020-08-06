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
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
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
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(getClient().getValues().getUserId());
        Set<String> cap = new HashSet<String>(1);
        cap.add("app_spec_passwords");
        Credentials userCreds = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface usrInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        Set<String> emptySet = Collections.emptySet();
        usrInterface.changeCapabilities(new Context(I(getClient().getValues().getContextId())), user, cap, emptySet, emptySet, userCreds);
        appSpecApi = new AppPasswordApi(getApiClient());
        super.setUpConfiguration();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            removeAll();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Get the list of Application Specific Passwords for the user
     *
     * @return
     * @throws ApiException
     */
    protected List<AppPassword> getList() throws ApiException {
        AppPasswordListResponse response = appSpecApi.listApplicationPassword(getSessionId());
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    /**
     * Get list of applications
     * AppPasswordGetAppsResponsegetApps
     *
     * @param session
     * @return
     * @throws ApiException
     */
    protected List<AppPasswordApplication> getApps(String session) throws ApiException {
        AppPasswordGetAppsResponse response = appSpecApi.getApplications(session);
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
        AppPasswordRegistrationResponse resp = appSpecApi.addApplicationPassword(getSessionId(), appType, "Test");
        return checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());
    }

    /**
     * Remove password
     *
     * @param uuid The UUID of the password to remove
     * @throws ApiException
     */
    protected void removePassword(String uuid) throws ApiException {
        appSpecApi.removeApplicationPassword(getSessionId(), uuid);
    }

    /**
     * Remove all application specific passwords for the account. Used in cleanup
     *
     * @throws ApiException
     */
    protected void removeAll() throws ApiException {
        List<AppPassword> passwords = appSpecApi.listApplicationPassword(getSessionId()).getData();
        for (AppPassword password : passwords) {
            removePassword(password.getUUID());
        }
    }

    /**
     * Reset the Application Specific Api Client
     * resetApiClient
     *
     */
    protected void resetApiClient() {
        appSpecApi = new AppPasswordApi(getApiClient());
    }

}
