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

package com.openexchange.ajax.folder;

import java.rmi.Naming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.ajax.chronos.AbstractEnhancedApiClientSession;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsData;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsFiles;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link AbstractFolderCheckLimitTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class AbstractFolderCheckLimitTest extends AbstractEnhancedApiClientSession {

    private static final String FILE_NAME = "quotaCheck";
    private static final String FILE_ENDING = ".txt";

    protected Credentials credentials = null;
    protected OXUserInterface iface = null;

    protected int testUserId = 0;
    protected String quotaTestFolderId;

    protected TestUser quotaTestuser;
    private FoldersApi foldersApi;
    protected Context context;
    protected User user;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        this.credentials = new Credentials(admin.getUser(), admin.getPassword());
        this.user = createUserObj();
        this.quotaTestuser = new TestUser(user.getName(), Integer.toString(getClient().getValues().getContextId()), "secret");
        this.testUserId = createNewUserWithQuota(user);
        quotaApiClient = generateApiClient(quotaTestuser);
        rememberClient(quotaApiClient);
        foldersApi = new FoldersApi(quotaApiClient);
        quotaTestFolderId = getPrivateInfostoreFolder();
    }

    private String getPrivateInfostoreFolder() throws Exception {
        ConfigApi configApi = new ConfigApi(quotaApiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), quotaApiClient.getSession());
        return ((Integer) configNode.getData()).toString();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            deleteUser();
        } finally {
            super.tearDown();
        }
    }

    protected int createNewUserWithQuota(User user) throws Exception {
        UserModuleAccess userModuleAccess = new UserModuleAccess();
        context = new Context(getClient().getValues().getContextId());
        Integer userId = iface.create(context, user, userModuleAccess, credentials).getId();
        user.setId(userId);
        return userId;
    }

    protected void deleteUser() throws Exception {
        if (testUserId > 0) {
            User user = new User(testUserId);
            iface.delete(new Context(getClient().getValues().getContextId()), user, null, credentials);
        }
    }

    protected FolderCheckLimitsResponse checkLimits(FolderCheckLimitsData body, String folderId, String type) throws ApiException {
        return foldersApi.checkLimits(quotaApiClient.getSession(), folderId, type, body);
    }

    /**
     * Creates a data object for the quota check
     *
     * @param folder The filestore folder
     * @param files The file data object
     * @return data, the object to start the request with
     */
    protected FolderCheckLimitsData createQuotaCheckData(List<FolderCheckLimitsFiles> files) {
        FolderCheckLimitsData infoItemQuotaCheckData = new FolderCheckLimitsData();
        infoItemQuotaCheckData.setFiles(files);
        return infoItemQuotaCheckData;
    }

    /**
     * Creates a data object with file meta data
     *
     * @param fileSize The file size
     * @return data, the object which contains the file size and the file name
     */
    protected FolderCheckLimitsFiles createQuotaCheckFiles(Long fileSize) {
        return createQuotaCheckFiles(getFileName(), fileSize);
    }

    /**
     * Creates a data object with file meta data
     *
     * @param fileName The file name.
     * @param fileSize The file size.
     * @return data, the object which contains the file size and the file name
     */
    protected FolderCheckLimitsFiles createQuotaCheckFiles(String fileName, Long fileSize) {
        FolderCheckLimitsFiles infoItemQuotaCheckFile = new FolderCheckLimitsFiles();
        infoItemQuotaCheckFile.setName(fileName);
        infoItemQuotaCheckFile.setSize(fileSize);
        return infoItemQuotaCheckFile;
    }

    /**
     * Creates a file name
     *
     * @return String The file name
     */
    public String getFileName() {
        return FILE_NAME + UUID.randomUUID() + FILE_ENDING;
    }

    /**
     * Initializes a rmi user object with some random values and with 300 MB file quota
     *
     * @param oxuser The rmi user object.
     * @return User, a dummy user object
     */
    private User createUserObj() {
        com.openexchange.admin.rmi.dataobjects.User oxuser = new com.openexchange.admin.rmi.dataobjects.User();
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

    private static final Map<String, String> CONFIG = new HashMap<>();
    private ApiClient quotaApiClient;
    static {
        CONFIG.put("com.openexchange.quota.infostore", "2");
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }
}
