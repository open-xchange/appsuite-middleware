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

package com.openexchange.multifactor.listener;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link MultifactorDeleteDeviceListener} - A {@link MultifactorListener} which listens for device deletions and updates the session's multifactor state
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorDeleteDeviceListener implements MultifactorListener {

    private final SessiondService sessionService;

    /**
     * Initializes a new {@link MultifactorDeleteDeviceListener}.
     *
     * @param sessionService The {@link SessiondService}
     */
    public MultifactorDeleteDeviceListener(SessiondService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void onAfterAuthentication(int userId, int contextId, boolean success) throws OXException {
        /*no-op*/
    }

    @Override
    public void onAfterDelete(int userId, int contextId, int enabledDevices) throws OXException {
        //Remove the multifactor flag for the user's session if no enabled device is present anymore
        if (enabledDevices == 0) {
            final Collection<Session> sessions = sessionService.getSessions(userId, contextId);
            for (Session session : sessions) {
                session.setParameter(Session.MULTIFACTOR_PARAMETER, null);
            }
        }
    }

    @Override
    public void onAfterAdd(int userId, int contextId, int enabledDevices) throws OXException {
        /*no-op*/
    }
}
