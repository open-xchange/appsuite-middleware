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

import java.util.ArrayList;
import com.openexchange.ajax.chronos.AbstractEnhancedApiClientSession;
import com.openexchange.ajax.chronos.EnhancedApiClient;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;


/**
 * {@link AbstractApiClientInfostoreTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class AbstractApiClientInfostoreTest extends AbstractEnhancedApiClientSession {

    private static final String MODULE = "infostore";

    protected FoldersApi foldersApi = null;
    protected UserApi defaultUserApi = null;

    protected String infostoreDefaultFolderId = "";

    protected int userId = -1;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userId = apiClient.getUserId();

        ApiClient client = getApiClient();
        rememberClient(client);
        EnhancedApiClient enhancedClient = getEnhancedApiClient();
        rememberClient(enhancedClient);
        defaultUserApi = new UserApi(client, enhancedClient, testUser, false);
        foldersApi = defaultUserApi.getFoldersApi();
        infostoreDefaultFolderId = getDefaultFolder(apiClient.getSession(), new FoldersApi(apiClient));
    }

    /**
     * Retrieves the default infostore folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default contact folder of the user
     * @throws Exception if the default infostore folder cannot be found
     */
    @SuppressWarnings("unchecked")
    protected String getDefaultFolder(String session, FoldersApi foldersApi) throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, MODULE, "1,308", "1");
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPublic();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        } else {
            for (ArrayList<?> folder : privateList) {
                if ((Boolean) folder.get(1)) {
                    return (String) folder.get(0);
                }
            }
        }
        throw new Exception("Unable to find default infostore folder!");
    }

}
