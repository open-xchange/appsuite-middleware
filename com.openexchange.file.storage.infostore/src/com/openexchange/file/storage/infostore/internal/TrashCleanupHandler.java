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

package com.openexchange.file.storage.infostore.internal;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
;

/**
 * {@link TrashCleanupHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TrashCleanupHandler implements LoginHandlerService, NonTransient {

    /**
     * Initializes a new {@link TrashCleanupHandler}.
     */
    public TrashCleanupHandler() {
        super();
    }

    @Override
    public void handleLogin(LoginResult loginResult) throws OXException {
        Session session = loginResult.getSession();
        int retentionDays = getRetentionDays(session.getContextId(), session.getUserId());
        if (0 < retentionDays) {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session, loginResult.getContext(), loginResult.getUser());
            if (false == serverSession.getUserConfiguration().hasInfostore()) {
                return;
            }
            new TrashCleaner(serverSession, retentionDays).run();
        }
    }

    @Override
    public void handleLogout(LoginResult logout) throws OXException {
        // nothing to do
    }

    private static int getRetentionDays(int contextID, int userID) throws OXException {
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        if (null != configViewFactory) {
            ConfigView configView = configViewFactory.getView(userID, contextID);
            return configView.opt("com.openexchange.infostore.trash.retentionDays", Integer.class, Integer.valueOf(-1)).intValue();
        }
        return -1;
    }

}
