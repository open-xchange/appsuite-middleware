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

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.ajax.chronos.EnhancedApiClient;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.InfoItemQuotaCheckData;
import com.openexchange.testing.httpclient.models.InfoItemQuotaCheckFiles;
import com.openexchange.testing.httpclient.models.InfoItemQuotaCheckResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;

/**
 * {@link AbstractInfostoreQuotaCheckTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class AbstractInfostoreQuotaCheckTest extends AbstractApiClientInfostoreTest {

    private static final String FILE_NAME = "quotaCheck";
    private static final String FILE_ENDING = ".txt";

    protected int testUserId;
    protected String testContext;
    protected String quotaTestFolderId;

    protected TestUser quotaTestuser;
    protected AJAXClient quotaClient;
    protected ApiClient quotaApiClient;

    protected FoldersApi quotaFoldersApi = null;
    protected UserApi quotaUserApi = null;
    protected InfostoreApi infostoreApi = null;

    private Credentials credentials = null;
    private OXUserInterface iface = null;
    private com.openexchange.admin.rmi.dataobjects.User user = null;

    private HashMap<String, String> config = new HashMap<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        credentials = new Credentials(admin.getUser(), admin.getPassword());
        user = createUser(user);
        quotaTestuser = initTestuser(user.getName(), Integer.toString(getClient().getValues().getContextId()), "secret");

        testUserId = createNewUserWithQuota();
        quotaClient = generateClient(null, quotaTestuser);

        quotaApiClient = generateApiClient(quotaTestuser);
        rememberClient(quotaApiClient);

        infostoreApi = new InfostoreApi(quotaApiClient);

        EnhancedApiClient enhancedClient = generateEnhancedClient(quotaTestuser);
        rememberClient(enhancedClient);
        quotaUserApi = new UserApi(quotaApiClient, enhancedClient, quotaTestuser, false);
        quotaFoldersApi = defaultUserApi.getFoldersApi();
        quotaTestFolderId = getDefaultFolder(quotaApiClient.getSession(), new FoldersApi(quotaApiClient));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        logoutClient(quotaClient, true);
    }

    /**
     * Creates a new user based on the rmi interface
     *
     * @return Integer The id of the TestUser
     * @throws Exception, if user creation fails
     */
    protected int createNewUserWithQuota() throws Exception {
        testUserId = iface.create(new Context(getClient().getValues().getContextId()), user, credentials).getId();
        return testUserId;
    }

    /**
     * Deletes a user based on the rmi interface
     *
     * @throws Exception, if the user deletion fails
     */
    protected void deleteUser() throws Exception {
        user.setId(testUserId);
        iface.delete(new Context(getClient().getValues().getContextId()), user, null, credentials);
    }

    /**
     * Initializes the specific configuration
     *
     * @throws Exception, if the configuration fails
     */
    protected void initConfiguration() throws Exception {
        setUpConfiguration();
    }

    /**
     * Sets the configuration to the default value
     *
     * @param map The map holding the key-value pairs for the configuration
     * @throws Exception, if the configuration fails
     */
    protected void setPropertiesToDefault(Map<String, String> map) throws Exception {
        if (!config.isEmpty()) {
            config = new HashMap<>();
        }
        addUserProperty(map);
        setUpConfiguration();
    }

    /**
     * Adds the configuration to the related map
     *
     * @param map The map which contains the configuration
     */
    protected void addUserProperty(Map<String, String> map) {
        config.putAll(map);
    }

    /**
     * Requests the quota check api
     *
     * @param body The data body for the request
     * @param expectException A boolean value if to expect an error from the server call
     * @throws Exception, if the quota check fails
     */
    protected void checkQuota(InfoItemQuotaCheckData body, boolean expectException) throws Exception {
        InfoItemQuotaCheckResponse checkQuota = infostoreApi.checkQuota(quotaApiClient.getSession(), body);
        assertNotNull(checkQuota);
        if (expectException) {
            assertNull(checkQuota.getData());
            assertNotNull(checkQuota.getError());
        } else {
            assertTrue(checkQuota.getData().getAccepted());
        }
    }

    /**
     * Creates a data object for the quota check
     *
     * @param folder The filestore folder
     * @param files The file data object
     * @return data, the object to start the request with
     */
    protected InfoItemQuotaCheckData createQuotaCheckData(String folder, List<InfoItemQuotaCheckFiles> files) {
        InfoItemQuotaCheckData infoItemQuotaCheckData = new InfoItemQuotaCheckData();
        infoItemQuotaCheckData.setFiles(files);
        infoItemQuotaCheckData.setFolder(folder);
        return infoItemQuotaCheckData;
    }

    /**
     * Creates a data object with file meta data
     *
     * @param fileName The file name.
     * @param fileSize The file size.
     * @return data, the object which contains the file size and the file name
     */
    protected InfoItemQuotaCheckFiles createQuotaCheckFiles(String fileName, Long fileSize) {
        InfoItemQuotaCheckFiles infoItemQuotaCheckFile = new InfoItemQuotaCheckFiles();
        infoItemQuotaCheckFile.setFilename(fileName);
        infoItemQuotaCheckFile.setFileSize(fileSize);
        return infoItemQuotaCheckFile;
    }

    /**
     * Creates a file name
     *
     * @return String The file name
     */
    public String getFileName() {
        return FILE_NAME+UUID.randomUUID()+FILE_ENDING;
    }

    /**
     * Creates a new TestUser
     *
     * @param user The user name.
     * @param context The user context.
     * @param password The password.
     * @return TestUser, a new TestUser.
     */
    private TestUser initTestuser(String user, String context, String password) {
        return new TestUser(user, context, password);
    }

    /**
     * Initializes a rmi user object with some random values and with 300 MB file quota
     *
     * @param oxuser The rmi user object.
     * @return User, a dummy user object
     */
    private User createUser(com.openexchange.admin.rmi.dataobjects.User oxuser) {
        oxuser = new com.openexchange.admin.rmi.dataobjects.User();
        UUID random = UUID.randomUUID();
        oxuser.setName(random.toString());
        oxuser.setDisplay_name("oxuser" + random);
        oxuser.setGiven_name("oxuser" + random);
        oxuser.setSur_name("oxuser" + random);
        oxuser.setPrimaryEmail("oxuser" + random + "@example.com");
        oxuser.setEmail1("oxuser" + random + "@example.com");
        oxuser.setPassword("secret");
        oxuser.setImapServer("dovecot.devel.open-xchange.com");
        oxuser.setImapLogin(random + "@" + random);
        oxuser.setMaxQuota(100L);
        return oxuser;
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return config;
    }

    @Override
    protected String getScope() {
        return "user";
    }

}
