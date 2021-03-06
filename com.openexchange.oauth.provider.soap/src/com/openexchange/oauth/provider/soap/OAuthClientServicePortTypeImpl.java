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

package com.openexchange.oauth.provider.soap;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;


/**
 * {@link OAuthClientServicePortTypeImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */

@javax.jws.WebService(
    serviceName = "OAuthClientService",
    portName = "OAuthClientServiceHttpSoap11Endpoint",
    targetNamespace = "http://soap.provider.oauth.openexchange.com",
    // wsdlLocation = "null",
    endpointInterface = "com.openexchange.oauth.provider.soap.OAuthClientServicePortType")
public class OAuthClientServicePortTypeImpl implements OAuthClientServicePortType {

    private final RemoteClientManagement clientManagement;

    public OAuthClientServicePortTypeImpl(RemoteClientManagement clientManagement) {
        super();
        this.clientManagement = clientManagement;
    }

    @Override
    public List<ClientListData> listClients(String contextGroup, Credentials credentials) throws OAuthClientServiceException {
        try {
            List<com.openexchange.oauth.provider.rmi.client.ClientDto> clients = clientManagement.getClients(contextGroup, soap2Credentials(credentials));
            List<ClientListData> listDatas = new ArrayList<>(clients.size());
            for (com.openexchange.oauth.provider.rmi.client.ClientDto client : clients) {
                ClientListData listData = new ClientListData();
                listData.setId(client.getId());
                listData.setName(client.getName());
                listDatas.add(listData);
            }
            return listDatas;
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client getClientById(String clientId, Credentials credentials) throws OAuthClientServiceException {
        try {
            com.openexchange.oauth.provider.rmi.client.ClientDto client = clientManagement.getClientById(clientId, soap2Credentials(credentials));
            if (client == null) {
                throw new OAuthClientServiceException("Invalid client identifier: " + clientId);
            }

            return client2Soap(client);
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client registerClient(String contextGroup, ClientData clientData, Credentials credentials) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.registerClient(contextGroup, soap2ClientData(clientData), soap2Credentials(credentials)));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData, Credentials credentials) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.updateClient(clientId, soap2ClientData(clientData), soap2Credentials(credentials)));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean unregisterClient(String clientId, Credentials credentials) throws OAuthClientServiceException {
        try {
            return clientManagement.unregisterClient(clientId, soap2Credentials(credentials));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client revokeClientSecret(String clientId, Credentials credentials) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.revokeClientSecret(clientId, soap2Credentials(credentials)));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean enableClient(String clientId, Credentials credentials) throws OAuthClientServiceException {
        try {
            return clientManagement.enableClient(clientId, soap2Credentials(credentials));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean disableClient(String clientId, Credentials credentials) throws OAuthClientServiceException {
        try {
            return clientManagement.disableClient(clientId, soap2Credentials(credentials));
        } catch (RemoteException | RemoteClientManagementException | InvalidCredentialsException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private static Client client2Soap(ClientDto client) {
        Client soapClient = new Client();

        {
            String contactAddress = client.getContactAddress();
            if (null != contactAddress) {
                soapClient.setContactAddress(contactAddress);
            }
        }

        {
            String defaultScope = client.getDefaultScope();
            if (null != defaultScope) {
                soapClient.setDefaultScope(defaultScope);
            }
        }

        {
            String description = client.getDescription();
            if (null != description) {
                soapClient.setDescription(description);
            }
        }

        {
            IconDto icon = client.getIcon();
            if (null != icon) {
                Icon soapIcon = new Icon();
                soapIcon.setData(Base64.encodeBase64String(icon.getData()));
                soapIcon.setMimeType(icon.getMimeType());
                soapClient.setIcon(soapIcon);
            }
        }

        {
            boolean enabled = client.isEnabled();
            soapClient.setEnabled(enabled);
        }

        {
            String id = client.getId();
            if (null != id) {
                soapClient.setId(id);
            }
        }

        {
            String name = client.getName();
            if (null != name) {
                soapClient.setName(name);
            }
        }

        {
            List<String> redirectURIs = client.getRedirectURIs();
            if (null != redirectURIs) {
                soapClient.getRedirectURIs().addAll(redirectURIs);
            }
        }

        {
            long registrationDate = client.getRegistrationDate();
            soapClient.setRegistrationDate(registrationDate);
        }

        {
            String secret = client.getSecret();
            if (null != secret) {
                soapClient.setSecret(secret);
            }
        }

        {
            String website = client.getWebsite();
            if (null != website) {
                soapClient.setWebsite(website);
            }
        }

        return soapClient;
    }

    private static ClientDataDto soap2ClientData(ClientData soapClientData) {
        ClientDataDto clientData = new ClientDataDto();

        {
            String contactAddress = soapClientData.getContactAddress();
            if (null != contactAddress) {
                clientData.setContactAddress(contactAddress);
            }
        }

        {
            String defaultScope = soapClientData.getDefaultScope();
            if (null != defaultScope) {
                clientData.setDefaultScope(defaultScope);
            }
        }

        {
            String description = soapClientData.getDescription();
            if (null != description) {
                clientData.setDescription(description);
            }
        }

        {
            Icon base64Icon = soapClientData.getIcon();
            if (null != base64Icon) {
                IconDto icon = new IconDto();
                icon.setMimeType(base64Icon.getMimeType());
                icon.setData(Base64.decodeBase64(base64Icon.getData()));
                clientData.setIcon(icon);
            }
        }

        {
            String name = soapClientData.getName();
            if (null != name) {
                clientData.setName(name);
            }
        }

        {
            List<String> redirectURIs = soapClientData.getRedirectURIs();
            if (null != redirectURIs && redirectURIs.size() > 0) {
                clientData.setRedirectURIs(new ArrayList<String>(redirectURIs));
            }
        }

        {
            String website = soapClientData.getWebsite();
            if (null != website) {
                clientData.setWebsite(website);
            }
        }

        return clientData;
    }

    private static com.openexchange.admin.rmi.dataobjects.Credentials soap2Credentials(Credentials soapCredentials) {
        if (soapCredentials == null) {
            return null;
        }

        com.openexchange.admin.rmi.dataobjects.Credentials credentials = new com.openexchange.admin.rmi.dataobjects.Credentials();
        credentials.setLogin(soapCredentials.getLogin());
        credentials.setPassword(soapCredentials.getPassword());
        return credentials;
    }

}
