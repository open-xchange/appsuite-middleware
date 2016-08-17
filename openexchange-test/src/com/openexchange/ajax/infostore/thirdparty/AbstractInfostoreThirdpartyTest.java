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

package com.openexchange.ajax.infostore.thirdparty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.oauth.client.actions.OAuthService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;


/**
 * {@link AbstractInfostoreThirdpartyTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class AbstractInfostoreThirdpartyTest extends AbstractAJAXSession {
    /**
     * Initializes a new {@link AbstractInfostoreThirdpartyTest}.
     * @param name
     */
    public AbstractInfostoreThirdpartyTest(String name) {
        super(name);
    }

    public FolderTestManager fMgr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fMgr = new FolderTestManager(getClient());
    }

    @Override
    protected void tearDown() throws Exception {
        fMgr.cleanUp();
        super.tearDown();
    }

    /**
     * Gets all infostore ids of the connected thirdparty filestores
     *
     * @param authProvider the authentication provider
     * @return the folder id or an exception is thrown if no folder id could be returned
     * @throws Exception if folder id was not found
     */
    protected List<ProviderIdMapper> getConnectedInfostoreId() throws Exception {
        int[] columns = new int[] {  1, 20 };

        List<ProviderIdMapper> providerIds = new ArrayList<ProviderIdMapper>();

        String folderId = null;

        FolderObject[] fObjs = fMgr.listFoldersOnServer(1, columns);
        for(FolderObject fObj : fObjs) {
            if(fObj.getFullName() != null) {
                for(OAuthService authProvider : OAuthService.values()) {
                    if(fObj.getFullName().startsWith(authProvider.getFilestorageService())) {
                        folderId = fObj.getFullName();
                        ProviderIdMapper pidm = new ProviderIdMapper();
                        pidm.setAuthProvider(authProvider);
                        pidm.setInfostoreId(folderId);
                        providerIds.add(pidm);
                    }
                }
            }
        }

        if(providerIds == null || providerIds.isEmpty()) {
            throw new Exception("Could not find file storage for provider: ");
        }

        return providerIds;
    }

    /**
     * Gets a sequence of random byte data
     *
     * @param size the size of the file in bytes
     * @return the sequence of byte data
     */
    protected byte[] randomBytes(int size) {
        byte[] randomBytes = new byte[size];
        Random rand = new Random();
        rand.nextBytes(randomBytes);
        return randomBytes;
    }

    protected JSONObject setFolderId(String value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("folder_id", value);
        return json;
    }
}
