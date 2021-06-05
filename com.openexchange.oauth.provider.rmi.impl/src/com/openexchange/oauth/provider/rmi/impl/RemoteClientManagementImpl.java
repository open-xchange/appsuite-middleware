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
            Credentials auth = credentials;
            if (auth == null) {
                auth = new Credentials("", "");
            }

            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);
        } catch (StorageException e) {
            LOGGER.error("Error while authenticating master admin", e);
            throw new RemoteClientManagementException(e.getMessage());
        }
    }

    private static List<ClientDto> clients2RMI(List<Client> clients) {
        if (clients == null) {
            return null;
        }

        List<ClientDto> rmiClients = new ArrayList<>(clients.size());
        for (Client client : clients) {
            rmiClients.add(client2RMI(client));
        }
        return rmiClients;
    }

    private static ClientDto client2RMI(Client client) {
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

    private static IconDto icon2RMI(Icon icon) {
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
