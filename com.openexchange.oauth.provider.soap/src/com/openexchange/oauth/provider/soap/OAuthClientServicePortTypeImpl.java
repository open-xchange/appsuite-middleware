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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.soap;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Streams;
import com.openexchange.oauth.provider.DefaultIcon;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.Scopes;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.rmi.OAuthClientRmi;


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

    private static final Logger LOG = LoggerFactory.getLogger(OAuthClientServicePortTypeImpl.class);

    private final OAuthClientRmi clientManagement;

    public OAuthClientServicePortTypeImpl(OAuthClientRmi clientManagement) {
        super();
        this.clientManagement = clientManagement;
    }

    @Override
    public List<ClientListData> listClients(String contextGroup) throws OAuthClientServiceException {
        try {
            List<com.openexchange.oauth.provider.client.Client> clients = clientManagement.getClients(contextGroup);
            List<ClientListData> listDatas = new ArrayList<>(clients.size());
            for (com.openexchange.oauth.provider.client.Client client : clients) {
                ClientListData listData = new ClientListData();
                listData.setId(client.getId());
                listData.setName(client.getName());
                listDatas.add(listData);
            }
            return listDatas;
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client getClientById(String clientId) throws OAuthClientServiceException {
        try {
            com.openexchange.oauth.provider.client.Client client = clientManagement.getClientById(clientId);
            if (client == null) {
                throw new OAuthClientServiceException("Invalid client identifier: " + clientId);
            }

            return client2Soap(client);
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client registerClient(String contextGroup, ClientData clientData) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.registerClient(contextGroup, soap2ClientData(clientData)));
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.updateClient(clientId, soap2ClientData(clientData)));
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean unregisterClient(String clientId) throws OAuthClientServiceException {
        try {
            return clientManagement.unregisterClient(clientId);
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Client revokeClientSecret(String clientId) throws OAuthClientServiceException {
        try {
            return client2Soap(clientManagement.revokeClientSecret(clientId));
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean enableClient(String clientId) throws OAuthClientServiceException {
        try {
            return clientManagement.enableClient(clientId);
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean disableClient(String clientId) throws OAuthClientServiceException {
        try {
            return clientManagement.disableClient(clientId);
        } catch (RemoteException | ClientManagementException e) {
            throw new OAuthClientServiceException(e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private com.openexchange.oauth.provider.soap.Client client2Soap(com.openexchange.oauth.provider.client.Client client) {
        com.openexchange.oauth.provider.soap.Client soapClient = new com.openexchange.oauth.provider.soap.Client();

        {
            String contactAddress = client.getContactAddress();
            if (null != contactAddress) {
                soapClient.setContactAddress(contactAddress);
            }
        }

        {
            Scopes defaultScope = client.getDefaultScope();
            if (null != defaultScope) {
                soapClient.setDefaultScope(defaultScope.scopeString());
            }
        }

        {
            String description = client.getDescription();
            if (null != description) {
                soapClient.setDescription(description);
            }
        }

        {
            com.openexchange.oauth.provider.client.Icon icon = client.getIcon();
            if (null != icon) {
                try {
                    Icon soapIcon = new Icon();
                    soapIcon.setData(Base64.encodeBase64String(Streams.stream2bytes(icon.getInputStream())));
                    soapIcon.setMimeType(icon.getMimeType());
                    soapClient.setIcon(soapIcon);
                } catch (IOException e) {
                    LOG.error("Failed to apply icon to client SOAP representation", e);
                }
            }
        }

        {
            boolean enabled = client.isEnabled();
            soapClient.setEnabled(Boolean.valueOf(enabled));
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
            Date registrationDate = client.getRegistrationDate();
            if (null != registrationDate) {
                soapClient.setRegistrationDate(registrationDate.getTime());
            }
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

    private com.openexchange.oauth.provider.client.ClientData soap2ClientData(com.openexchange.oauth.provider.soap.ClientData soapClientData) {
        com.openexchange.oauth.provider.client.ClientData clientData = new com.openexchange.oauth.provider.client.ClientData();

        {
            String contactAddress = soapClientData.getContactAddress();
            if (null != contactAddress) {
                clientData.setContactAddress(contactAddress);
            }
        }

        {
            String defaultScope = soapClientData.getDefaultScope();
            if (null != defaultScope) {
                clientData.setDefaultScope(DefaultScopes.parseScope(defaultScope));
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
                DefaultIcon icon = new DefaultIcon();
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
                clientData.setRedirectURIs(new LinkedHashSet<String>(redirectURIs));
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

}
