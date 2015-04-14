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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.rmi;

import java.rmi.RemoteException;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientData;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.rmi.OAuthClientRmi;


/**
 * {@link OAuthClientRmiImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class OAuthClientRmiImpl implements OAuthClientRmi {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OAuthClientRmiImpl.class);

    private final ClientManagement clientManagement;

    /**
     * Initializes a new {@link OAuthClientRmiImpl}.
     */
    public OAuthClientRmiImpl(ClientManagement clientManagement) {
        super();
        this.clientManagement = clientManagement;
    }

    private ClientManagement getClientManagement() {
        return clientManagement;
    }

    @Override
    public Client getClientById(String clientId) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().getClientById(clientId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public Client registerClient(String contextGroup, ClientData clientData) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().registerClient(contextGroup, clientData);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().updateClient(clientId, clientData);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public boolean unregisterClient(String clientId) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().unregisterClient(clientId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public Client revokeClientSecret(String clientId) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().revokeClientSecret(clientId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public boolean enableClient(String clientId) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().enableClient(clientId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public boolean disableClient(String clientId) throws RemoteException, ClientManagementException {
        try {
            return getClientManagement().disableClient(clientId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    @Override
    public List<Client> getClients(String groupId) throws ClientManagementException, RemoteException {
        try {
            return getClientManagement().getClients(groupId);
        } catch (ClientManagementException e) {
            throw serializableException(e);
        }
    }

    private static ClientManagementException serializableException(ClientManagementException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return e;
        }

        /*
         * Underlying exceptions are potentially not serializable, we have to strip them.
         * Additionally exceptions with a causes are most likely worth to be logged.
         */
        LOGGER.error("", e);
        ClientManagementException stripped = new ClientManagementException(e.getReason(), e.getMessage(), true);
        stripped.setStackTrace(e.getStackTrace());
        return stripped;
    }

}
