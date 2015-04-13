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

package com.openexchange.oauth.provider.internal.client.storage;

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
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientData;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.client.ClientManagementException.Reason;
import com.openexchange.oauth.provider.client.DefaultClient;
import com.openexchange.oauth.provider.client.Icon;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.oauth.provider.internal.client.LazyIcon;
import com.openexchange.oauth.provider.internal.client.Obfuscator;
import com.openexchange.oauth.provider.internal.tools.ClientId;
import com.openexchange.oauth.provider.internal.tools.OAuthClientIdHelper;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbOAuthClientStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
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

    private DatabaseService getDbService() throws ClientManagementException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (dbService == null) {
            throw new ClientManagementException(Reason.INTERNAL_ERROR, "DatabaseService not available");
        }

        return dbService;
    }

    private static Connection getReadCon(DatabaseService dbService, String groupId) throws ClientManagementException {
        try {
            return dbService.getReadOnlyForGlobal(groupId);
        } catch (OXException e) {
            throw new ClientManagementException(Reason.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    private static Connection getWriteCon(DatabaseService dbService, String groupId) throws ClientManagementException {
        try {
            return dbService.getWritableForGlobal(groupId);
        } catch (OXException e) {
            throw new ClientManagementException(Reason.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public boolean enableClient(String groupId, String clientId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        try {
            return enableClient(clientId, con);
        } finally {
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @return <code>true</code> if the client was enabled, <code>false</code> if it was not in disabled state before
     * @throws OXException If client could not be enabled
     * @throws ClientManagementException
     */
    private boolean enableClient(String clientId, Connection con) throws ClientManagementException {
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            Databases.startTransaction(con);
            sstmt = con.prepareStatement("SELECT enabled FROM oauth_client WHERE id = ? FOR UPDATE");
            rs = sstmt.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean(1)) {
                    con.commit();
                    return false;
                }
            } else {
                con.commit();
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            ustmt = con.prepareStatement("UPDATE oauth_client SET enabled=1 WHERE id = ?");
            ustmt.setString(1, clientId);
            ustmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            Databases.rollback(con);
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(sstmt, rs);
            Databases.closeSQLStuff(ustmt);
        }
    }

    @Override
    public boolean disableClient(String groupId, String clientId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        try {
            return disableClient(clientId, con);
        } finally {
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @param con The connection to use
     * @throws OXException If client could not be disabled
     * @throws ClientManagementException
     */
    public boolean disableClient(String clientId, Connection con) throws ClientManagementException {
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            Databases.startTransaction(con);
            sstmt = con.prepareStatement("SELECT enabled FROM oauth_client WHERE id = ? FOR UPDATE");
            rs = sstmt.executeQuery();
            if (rs.next()) {
                if (!rs.getBoolean(1)) {
                    con.commit();
                    return false;
                }
            } else {
                con.commit();
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            ustmt = con.prepareStatement("UPDATE oauth_client SET enabled=0 WHERE id = ?");
            ustmt.setString(1, clientId);
            ustmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            Databases.rollback(con);
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, sstmt);
            Databases.closeSQLStuff(ustmt);
        }
    }

    @Override
    public Client getClientById(String groupId, String clientId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getReadCon(dbService, groupId);
        try {
            return getClientById(groupId, clientId, con, false);
        } finally {
            dbService.backReadOnlyForGlobal(groupId, con);
        }
    }

    /**
     * Gets the client identified by the given identifier.
     *
     * @param groupId The context group identifier
     * @param clientToken The client identifier
     * @param con The connection to use
     * @param forUpdate <code>true</code> if the selected row shall be locked with FOR UPDATE
     * @return The client or <code>null</code> if there is no such client
     * @throws OXException If operation fails
     * @throws ClientManagementException
     */
    public DefaultClient getClientById(String groupId, String clientId, Connection con, boolean forUpdate) throws ClientManagementException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (forUpdate) {
                stmt = con.prepareStatement("SELECT name, description, secret, default_scope, contact_address, website, enabled, registration_date FROM oauth_client WHERE id = ? AND gid = ? FOR UPDATE");
            } else {
                stmt = con.prepareStatement("SELECT name, description, secret, default_scope, contact_address, website, enabled, registration_date FROM oauth_client WHERE id = ? AND gid = ?");
            }
            stmt.setString(1, clientId);
            stmt.setString(2, groupId);
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
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Client registerClient(ClientData clientData) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, clientData.getGroupId());
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = registerClient(clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(clientData.getGroupId(), con);
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
    public Client registerClient(ClientData clientData, Connection con) throws ClientManagementException {
        PreparedStatement stmt = null;
        try {
            String clientId = OAuthClientIdHelper.getInstance().generateClientId(clientData.getGroupId());
            String secret = UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID());

            stmt = con.prepareStatement("INSERT INTO oauth_client (id, gid, secret, name, description, icon, icon_mime_type, default_scope, contact_address, website, enabled, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, clientId);
            stmt.setString(2, clientData.getGroupId());
            stmt.setString(3, obfuscator.obfuscate(secret));
            stmt.setString(4, clientData.getName());
            stmt.setString(5, clientData.getDescription());

            Icon icon = clientData.getIcon();
            stmt.setBlob(6, icon.getInputStream());
            stmt.setString(7, icon.getMimeType());
            stmt.setString(8, clientData.getDefaultScope().scopeString());
            stmt.setString(9, clientData.getContactAddress());
            stmt.setString(10, clientData.getWebsite());
            stmt.setBoolean(11, true);
            long now = System.currentTimeMillis();
            stmt.setLong(12, now);

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
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
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

        client.setIcon(clientData.getIcon());
        return client;
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, clientData.getGroupId());
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = updateClient(clientId, clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(clientData.getGroupId(), con);
        }
    }

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @param con The connection to use
     * @return The updated client
     * @throws ClientManagementException
     */
    public Client updateClient(String clientId, ClientData clientData, Connection con) throws ClientManagementException {
        ClientId clientIdObj = ClientId.parse(clientId);
        if (clientIdObj == null) {
            throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
        }

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
                sql.append(" WHERE id = ? AND gid = ?");
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
                stmt.setString(pos++, clientId);
                stmt.setString(pos, clientData.getGroupId());
                int result = stmt.executeUpdate();
                if (result <= 0) {
                    throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
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

            String groupId = clientIdObj.getGroupId();
            Client reloaded = getClientById(groupId, clientId, con, false);
            if (reloaded == null) {
                // Client deleted concurrently
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            return reloaded;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean unregisterClient(String groupId, String clientId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean deleted = unregisterClient(groupId, clientId, con);

            con.commit();
            rollback = false;
            return deleted;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Unregisters an existing client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @param con The connection to use
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean unregisterClient(String groupId, String clientId, Connection con) throws ClientManagementException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauth_client WHERE id = ? AND gid = ?");
            stmt.setString(1, clientId);
            stmt.setString(2, groupId);
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
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Client revokeClientSecret(String groupId, String clientId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = revokeClientSecret(groupId, clientId, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param groupId id of the group the client is assigned to
     * @param clientId The client identifier
     * @param con The connection to use
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    public Client revokeClientSecret(String groupId, String clientId, Connection con) throws ClientManagementException {
        PreparedStatement stmt = null;
        try {
            DefaultClient client = getClientById(groupId, clientId, con, true);
            if (client == null) {
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            String newSecret = obfuscator.obfuscate(UUIDs.getUnformattedString(UUID.randomUUID()) + UUIDs.getUnformattedString(UUID.randomUUID()));
            stmt = con.prepareStatement("UPDATE oauth_client SET secret = ? WHERE id = ? AND gid = ?");
            stmt.setString(1, newSecret);
            stmt.setString(2, clientId);
            stmt.setString(3, groupId);
            stmt.executeUpdate();

            client.setSecret(newSecret);
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(Reason.STORAGE_ERROR, e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void invalidateClient(String groupId, String clientId) {
        // Nothing to do
    }
}
