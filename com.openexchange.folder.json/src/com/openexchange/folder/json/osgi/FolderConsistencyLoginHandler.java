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

package com.openexchange.folder.json.osgi;

import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.session.Session;

/**
 * {@link FolderConsistencyLoginHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderConsistencyLoginHandler implements LoginHandlerService, NonTransient {

    /**
     * Initializes a new {@link FolderConsistencyLoginHandler}.
     */
    public FolderConsistencyLoginHandler() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        final Session session = login.getSession();
        folderService.checkConsistency("1", session);
        folderService.checkConsistency("0", session);
        folderService.checkConsistency("20", session);
    }

    @Override
    public void handleLogout(final LoginResult logout) throws OXException {
        // Nothing to do
    }

}
