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

package com.openexchange.oauth.provider.internal.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.osgi.framework.BundleException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.ClientData;
import com.openexchange.oauth.provider.DefaultClient;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.Icon;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link RdbOAuthClientStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class RdbOAuthClientStorage extends AbstractOAuthClientStorage {

    private final Obfuscator obfuscator;

    /**
     * Initializes a new {@link RdbOAuthClientStorage}.
     *
     * @throws BundleException If configuration property is missing
     */
    public RdbOAuthClientStorage(ServiceLookup services) throws BundleException {
        super(services);
        ConfigurationService service = services.getService(ConfigurationService.class);
        String key = service.getProperty(OAuthProviderProperties.ENCRYPTION_KEY);
        if (Strings.isEmpty(key)) {
            throw new BundleException("Missing \"" + OAuthProviderProperties.ENCRYPTION_KEY + "\" property", BundleException.ACTIVATOR_ERROR);
        }
        obfuscator = new Obfuscator(key, services);
    }

    private DatabaseService getDbService() throws OXException {
        DatabaseService service = services.getOptionalService(DatabaseService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        return service;
    }

    @Override
    public void enableClient(String clientId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        try {
            enableClient(clientId, con);
        } finally {
            dbService.backWritable(con);
        }
    }

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @throws OXException If client could not be enabled
     */
    public void enableClient(String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauth_client SET enabled=1 WHERE id = ? AND enabled = 0");
            stmt.setString(1, clientId);
            int result = stmt.executeUpdate();
            if (result <= 0) {
                throw OAuthProviderExceptionCodes.FAILED_ENABLEMENT.create(clientId);
            }
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void disableClient(String clientId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        try {
            disableClient(clientId, con);
        } finally {
            dbService.backWritable(con);
        }
    }

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @throws OXException If client could not be disabled
     */
    public void disableClient(String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oauth_client SET enabled=0 WHERE id = ? AND enabled = 1");
            stmt.setString(1, clientId);
            int result = stmt.executeUpdate();
            if (result <= 0) {
                throw OAuthProviderExceptionCodes.FAILED_DISABLEMENT.create(clientId);
            }
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Client getClientById(String clientId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly();
        try {
            return getClientById(clientId, con);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    /**
     * Gets the client identified by the given identifier.
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @return The client or <code>null</code> if there is no such client
     * @throws OXException If operation fails
     */
    public Client getClientById(String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT name, description, secret, default_scope, contact_address, website, enabled, registration_date FROM oauth_client WHERE id = ?");
            stmt.setString(1, clientId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            DefaultClient client = new DefaultClient();
            client.setId(clientId);
            client.setName(rs.getString(1));

            String description = rs.getString(2);
            if (!rs.wasNull()) {
                client.setDescription(description);
            }

            client.setSecret(obfuscator.unobfuscate(rs.getString(3)));

            String defaultScope = rs.getString(4);
            if (!rs.wasNull()) {
                client.setDefaultScope(new DefaultScopes(defaultScope));
            }

            String contactAddress = rs.getString(5);
            if (!rs.wasNull()) {
                client.setContactAddress(contactAddress);
            }

            String website = rs.getString(6);
            if (!rs.wasNull()) {
                client.setWebsite(website);
            }

            client.setEnabled(rs.getBoolean(7));
            client.setRegistrationDate(new Date(rs.getLong(8)));

            Databases.closeSQLStuff(rs, stmt);
            stmt = con.prepareStatement("SELECT uri FROM oauth_client_uri WHERE client = ?");
            stmt.setString(1, clientId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String uri = rs.getString(1);
                client.addRedirectURI(uri);
            }

            client.setIcon(new LazyIcon(clientId));
            return client;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Client registerClient(ClientData clientData) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = registerClient(clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritable(con);
        }
    }

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param clientData The client data to create the client from
     * @param con The connection to use
     * @return The newly created client
     * @throws OXException If create operation fails
     */
    public Client registerClient(ClientData clientData, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            // TODO: move generation out of storage layer
            String clientId = UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID());
            String secret = UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID());

            stmt = con.prepareStatement("INSERT INTO oauth_client (id, secret, name, description, icon, icon_mime_type, default_scope, contact_address, website, enabled, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, clientId);
            stmt.setString(2, obfuscator.obfuscate(secret));
            stmt.setString(3, clientData.getName());
            stmt.setString(4, clientData.getDescription());

            Icon icon = clientData.getIcon();
            stmt.setBlob(5, icon.getInputStream());
            stmt.setString(6, icon.getMimeType());
            stmt.setString(7, clientData.getDefaultScope().scopeString());
            stmt.setString(8, clientData.getContactAddress());
            stmt.setString(9, clientData.getWebsite());
            stmt.setBoolean(10, true);
            long now = System.currentTimeMillis();
            stmt.setLong(11, now);

            stmt.executeUpdate();

            Databases.closeSQLStuff(stmt);
            stmt = con.prepareStatement("INSERT INTO oauth_client_uri (client, uri) VALUES (?, ?)");
            stmt.setString(1, clientId);
            for (String uri : clientData.getRedirectURIs()) {
                stmt.setString(2, uri);
                stmt.addBatch();
            }
            stmt.executeBatch();


            DefaultClient client = toClient(clientData);
            client.setRegistrationDate(new Date(now));
            client.setEnabled(true);
            client.setId(clientId);
            client.setSecret(secret);
            return client;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static DefaultClient toClient(ClientData clientData) {
        DefaultClient client = new DefaultClient();
        client.setName(clientData.getName());
        client.setDescription(clientData.getDescription());
        client.setDefaultScope(clientData.getDefaultScope());
        client.setContactAddress(clientData.getContactAddress());
        client.setWebsite(clientData.getWebsite());
        for (String uri : clientData.getRedirectURIs()) {
            client.addRedirectURI(uri);
        }

        return client;
    }


    @Override
    public Client updateClient(String clientId, ClientData clientData) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = updateClient(clientId, clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritable(con);
        }
    }

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @param con The connection to use
     * @return The updated client
     * @throws OXException If update operation fails
     */
    public Client updateClient(String clientId, ClientData clientData, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            class TypedObject {
                final int type;
                final Object object;

                TypedObject(Object object, int type) {
                    super();
                    this.object = object;
                    this.type = type;
                }
            }

            List<TypedObject> values = new LinkedList<TypedObject>();

            StringBuilder sql = new StringBuilder(128);
            sql.append("UPDATE oauth_client SET");

            if (clientData.containsContactAddress()) {
                sql.append(" contact_address = ?,");
                values.add(new TypedObject(clientData.getContactAddress(), Types.VARCHAR));
            }

            if (clientData.containsDescription()) {
                sql.append(" description = ?,");
                values.add(new TypedObject(clientData.getDescription(), Types.VARCHAR));
            }

            if (clientData.containsName()) {
                sql.append(" name = ?,");
                values.add(new TypedObject(clientData.getName(), Types.VARCHAR));
            }

            if (clientData.containsWebsite()) {
                sql.append(" website = ?,");
                values.add(new TypedObject(clientData.getWebsite(), Types.VARCHAR));
            }

            if (clientData.containsIcon()) {
                sql.append(" icon = ?, icon_mime_type = ?,");

                Icon icon = clientData.getIcon();
                values.add(new TypedObject(icon.getInputStream(), Types.BLOB));
                values.add(new TypedObject(icon.getMimeType(), Types.VARCHAR));
            }

            if (!values.isEmpty()) {
                sql.setLength(sql.length() - 1); // Delete last character
                sql.append(" WHERE id = ?");
                stmt = con.prepareStatement(sql.toString());
                int pos = 1;
                for (TypedObject value : values) {
                    Object obj = value.object;
                    if (null == obj) {
                        stmt.setNull(pos, value.type);
                    } else {
                        stmt.setObject(pos++, obj, value.type);
                    }
                }
                stmt.setString(pos, clientId);
                int result = stmt.executeUpdate();
                if (result <= 0) {
                    throw OAuthProviderExceptionCodes.CLIENT_NOT_FOUND.create(clientId);
                }
            }

            if (clientData.containsRedirectURIs()) {
                Databases.closeSQLStuff(stmt);
                stmt = con.prepareStatement("DELETE FROM oauth_client_uri WHERE client = ?");
                stmt.setString(1, clientId);
                stmt.executeUpdate();

                Set<String> redirectURIs = clientData.getRedirectURIs();
                if (!redirectURIs.isEmpty()) {
                    Databases.closeSQLStuff(stmt);
                    stmt = con.prepareStatement("INSERT INTO oauth_client_uri (client, uri) VALUES (?, ?)");
                    stmt.setString(1, clientId);
                    for (String uri : redirectURIs) {
                        stmt.setString(2, uri);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            Databases.closeSQLStuff(stmt);
            stmt = null;

            return getClientById(clientId, con);
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean unregisterClient(String clientId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean deleted = unregisterClient(clientId, con);

            con.commit();
            rollback = false;
            return deleted;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritable(con);
        }
    }

    /**
     * Unregisters an existing client
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean unregisterClient(String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_client WHERE id = ?");
            stmt.setString(1, clientId);
            int result = stmt.executeUpdate();
            if (result <= 0) {
                return false;
            }

            Databases.closeSQLStuff(stmt);
            stmt = con.prepareStatement("DELETE FROM oauth_client_uri WHERE client = ?");
            stmt.setString(1, clientId);
            stmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Client revokeClientSecret(String clientId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable();
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = revokeClientSecret(clientId, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritable(con);
        }
    }

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    public Client revokeClientSecret(String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT secret FROM oauth_client WHERE id = ?");
            stmt.setString(1, clientId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthProviderExceptionCodes.CLIENT_NOT_FOUND.create(clientId);
            }
            String obfusOldSecret = rs.getString(1);
            String plainNewSecret = UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID());

            Databases.closeSQLStuff(rs, stmt);
            stmt = con.prepareStatement("UPDATE oauth_client SET secret = ? WHERE id = ? AND secret = ?");
            stmt.setString(1, obfuscator.obfuscate(plainNewSecret));
            stmt.setString(2, clientId);
            stmt.setString(3, obfusOldSecret);
            int result = stmt.executeUpdate();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (result <= 0) {
                // Another thread revoked in the meantime
                throw OAuthProviderExceptionCodes.CONCURRENT_SECRET_REVOKE.create(clientId);
            }

            return getClientById(clientId, con);
        } catch (SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void invalidateClient(String clientId) {
        // Nothing to do
    }

}
