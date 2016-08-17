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

package com.openexchange.oauth.provider.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientData;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.client.DefaultIcon;
import com.openexchange.oauth.provider.authorizationserver.client.Icon;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;


/**
 * {@link RemoteClientManagementImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class RemoteClientManagementImpl implements RemoteClientManagement {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RemoteClientManagementImpl.class);

    private final ClientManagement clientManagement;

    /**
     * Initializes a new {@link RemoteClientManagementImpl}.
     */
    public RemoteClientManagementImpl(ClientManagement clientManagement) {
        super();
        this.clientManagement = clientManagement;
    }

    private ClientManagement getClientManagement() {
        return clientManagement;
    }

    @Override
    public List<ClientDto> getClients(String groupId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return clients2RMI(getClientManagement().getClients(groupId));
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public ClientDto getClientById(String clientId, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return client2RMI(getClientManagement().getClientById(clientId));
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public ClientDto registerClient(String contextGroup, ClientDataDto clientData, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return client2RMI(getClientManagement().registerClient(contextGroup, rmi2ClientData(clientData)));
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public ClientDto updateClient(String clientId, ClientDataDto clientData, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return client2RMI(getClientManagement().updateClient(clientId, rmi2ClientData(clientData)));
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public boolean unregisterClient(String clientId, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return getClientManagement().unregisterClient(clientId);
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public ClientDto revokeClientSecret(String clientId, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return client2RMI(getClientManagement().revokeClientSecret(clientId));
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public boolean enableClient(String clientId, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return getClientManagement().enableClient(clientId);
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    @Override
    public boolean disableClient(String clientId, Credentials credentials) throws RemoteException, RemoteClientManagementException, InvalidCredentialsException {
        try {
            authenticate(credentials);
            return getClientManagement().disableClient(clientId);
        } catch (ClientManagementException e) {
            throw convertException(e);
        }
    }

    private static RemoteClientManagementException convertException(ClientManagementException e) {
        LOGGER.error("Error during OAuth client provisioning call", e);
        RemoteClientManagementException cme = new RemoteClientManagementException(e.getMessage());
        cme.setStackTrace(e.getStackTrace());
        return cme;
    }

    private void authenticate(Credentials credentials) throws RemoteClientManagementException, InvalidCredentialsException {
        try {
            if (credentials == null) {
                credentials = new Credentials("", "");
            }

            new BasicAuthenticator().doAuthentication(credentials);
        } catch (StorageException e) {
            LOGGER.error("Error while authenticating master admin", e);
            throw new RemoteClientManagementException(e.getMessage());
        }
    }

    private static List<ClientDto> clients2RMI(List<Client> clients) throws RemoteClientManagementException {
        if (clients == null) {
            return null;
        }

        List<ClientDto> rmiClients = new ArrayList<>(clients.size());
        for (Client client : clients) {
            rmiClients.add(client2RMI(client));
        }
        return rmiClients;
    }

    private static ClientDto client2RMI(Client client) throws RemoteClientManagementException {
        if (client == null) {
            return null;
        }

        ClientDto rmiClient = new ClientDto();
        rmiClient.setId(client.getId());
        rmiClient.setSecret(client.getSecret());
        rmiClient.setName(client.getName());
        rmiClient.setDescription(client.getDescription());
        rmiClient.setContactAddress(client.getContactAddress());
        rmiClient.setWebsite(client.getWebsite());
        rmiClient.setDefaultScope(client.getDefaultScope().toString());
        rmiClient.setEnabled(client.isEnabled());
        rmiClient.setRegistrationDate(client.getRegistrationDate().getTime());
        rmiClient.setRedirectURIs(cloneRedirectURIs(client.getRedirectURIs()));
        rmiClient.setIcon(icon2RMI(client.getIcon()));
        return rmiClient;
    }

    private static List<String> cloneRedirectURIs(List<String> redirectURIs) {
        if (redirectURIs == null) {
            return null;
        }

        List<String> clone = new ArrayList<>(redirectURIs.size());
        clone.addAll(redirectURIs);
        return clone;
    }

    private static IconDto icon2RMI(Icon icon) throws RemoteClientManagementException {
        if (icon == null) {
            return null;
        }

        IconDto rmiIcon = new IconDto();
        rmiIcon.setMimeType(icon.getMimeType());
        rmiIcon.setData(icon.getData());
        return rmiIcon;
    }

    private static ClientData rmi2ClientData(ClientDataDto rmiClientData) {
        if (rmiClientData == null) {
            return null;
        }

        ClientData clientData = new ClientData();
        if (rmiClientData.containsName()) {
            clientData.setName(rmiClientData.getName());
        }
        if (rmiClientData.containsDescription()) {
            clientData.setDescription(rmiClientData.getDescription());
        }
        if (rmiClientData.containsContactAddress()) {
            clientData.setContactAddress(rmiClientData.getContactAddress());
        }
        if (rmiClientData.containsWebsite()) {
            clientData.setWebsite(rmiClientData.getWebsite());
        }
        if (rmiClientData.containsIcon()) {
            IconDto rmiIcon = rmiClientData.getIcon();
            DefaultIcon icon = new DefaultIcon();
            icon.setMimeType(rmiIcon.getMimeType());
            icon.setData(rmiIcon.getData());
            clientData.setIcon(icon);
        }
        if (rmiClientData.containsRedirectURIs()) {
            clientData.setRedirectURIs(new HashSet<>(rmiClientData.getRedirectURIs()));
        }
        if (rmiClientData.containsDefaultScope()) {
            clientData.setDefaultScope(rmiClientData.getDefaultScope());
        }
        return clientData;
    }

}
