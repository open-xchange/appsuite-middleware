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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.test.common.test.pool.TestUser;
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
        this.iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + OXUserInterface.RMI_NAME);
        this.credentials = new Credentials(admin.getUser(), admin.getPassword());
        this.user = createUserObj();
        this.quotaTestuser = new TestUser(user.getName(), Integer.toString(testUser.getContextId()), "secret");
        this.testUserId = createNewUserWithQuota(user); // TODO create default user with this quota instead
        quotaApiClient = quotaTestuser.getApiClient();
        foldersApi = new FoldersApi(quotaApiClient);
        quotaTestFolderId = getPrivateInfostoreFolder();
    }

    private String getPrivateInfostoreFolder() throws Exception {
        ConfigApi configApi = new ConfigApi(quotaApiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
        return (configNode.getData()).toString();
    }

    protected int createNewUserWithQuota(User user) throws Exception {
        UserModuleAccess userModuleAccess = new UserModuleAccess();
        context = new Context(I(testUser.getContextId()));
        Integer userId = iface.create(context, user, userModuleAccess, credentials).getId();
        user.setId(userId);
        return userId.intValue();
    }

    protected FolderCheckLimitsResponse checkLimits(FolderCheckLimitsData body, String folderId, String type) throws ApiException {
        return foldersApi.checkLimits(folderId, type, body);
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
        oxuser.setMaxQuota(L(100));
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
