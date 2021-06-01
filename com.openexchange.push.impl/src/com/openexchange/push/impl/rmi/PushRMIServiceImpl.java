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

package com.openexchange.push.impl.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import com.openexchange.push.PushUserClient;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.impl.PushManagerRegistry;
import com.openexchange.push.rmi.PushRMIService;

/**
 * {@link PushRMIServiceImpl} - The MBean implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PushRMIServiceImpl implements PushRMIService {

    /**
     * Initializes a new {@link PushRMIServiceImpl}.
     *
     * @throws NotCompliantMBeanException If initialization fails
     */
    public PushRMIServiceImpl() {
        super();
    }

    @Override
    public List<List<String>> listPushUsers() throws RemoteException {
        try {
            List<PushUserInfo> pushUsers = PushManagerRegistry.getInstance().listPushUsers();
            Collections.sort(pushUsers);

            int size = pushUsers.size();
            List<List<String>> list = new ArrayList<List<String>>(size);
            for (int i = 0; i < size; i++) {
                PushUserInfo pushUser = pushUsers.get(i);
                if (null != pushUser) {
                    list.add(Arrays.asList(Integer.toString(pushUser.getContextId()), Integer.toString(pushUser.getUserId()), Boolean.toString(pushUser.isPermanent())));
                }
            }

            return list;
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(PushRMIServiceImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public List<List<String>> listClientRegistrations() throws RemoteException {
        try {
            List<PushUserClient> pushClients = PushManagerRegistry.getInstance().listRegisteredPushUsers();

            int size = pushClients.size();
            List<List<String>> list = new ArrayList<List<String>>(size);
            for (int i = 0; i < size; i++) {
                PushUserClient pushClient = pushClients.get(i);
                if (null != pushClient) {
                    list.add(Arrays.asList(Integer.toString(pushClient.getContextId()), Integer.toString(pushClient.getUserId()), pushClient.getClient()));
                }
            }

            return list;
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(PushRMIServiceImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public boolean unregisterPermanentListenerFor(int userId, int contextId, String clientId) throws RemoteException {
        try {
            return PushManagerRegistry.getInstance().unregisterPermanentListenerFor(userId, contextId, clientId);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(PushRMIServiceImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }
}
