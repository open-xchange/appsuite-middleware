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

package com.openexchange.groupware.infostore.autodelete;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;


/**
 * {@link InfostoreAutodeleteFileVersionsLoginHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class InfostoreAutodeleteFileVersionsLoginHandler implements LoginHandlerService {

    private final InfostoreFacadeImpl infostoreFacade;

    /**
     * Initializes a new {@link InfostoreAutodeleteFileVersionsLoginHandler}.
     */
    public InfostoreAutodeleteFileVersionsLoginHandler(InfostoreFacadeImpl infostoreFacade) {
        super();
        this.infostoreFacade = infostoreFacade;
    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {
        User user = login.getUser();
        if (user.isGuest()) {
            // Not for guest users
            return;
        }

        Session session = login.getSession();
        if (InfostoreAutodeleteSettings.hasAutodeleteCapability(session)) {
            int retentionDays = InfostoreAutodeleteSettings.getNumberOfRetentionDays(session);
            if (retentionDays > 0) {
                new InfostoreAutodeletePerformer(infostoreFacade).removeVersionsByRetentionDays(retentionDays, ServerSessionAdapter.valueOf(session));
            }
        }
    }

    @Override
    public void handleLogout(LoginResult logout) throws OXException {
        // Ignore
    }

}
