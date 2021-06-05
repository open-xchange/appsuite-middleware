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

package com.openexchange.report.internal;

import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.report.LoginCounterService;

/**
 * {@link LoginCounterRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class LoginCounterRMIServiceImpl implements LoginCounterRMIService {

    private LoginCounterService counterService;

    /**
     * Initialises a new {@link LoginCounterRMIServiceImpl}.
     */
    public LoginCounterRMIServiceImpl(LoginCounterService counterService) {
        super();
        this.counterService = counterService;
    }

    @Override
    public List<Object[]> getLastLoginTimeStamp(int userId, int contextId, String client) throws RemoteException {
        try {
            return counterService.getLastLoginTimeStamp(userId, contextId, client);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
