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

package com.openexchange.oauth.provider.impl.client.storage;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.BundleException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientData;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.client.DefaultClient;
import com.openexchange.oauth.provider.authorizationserver.client.Icon;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.client.LazyIcon;
import com.openexchange.oauth.provider.impl.client.Obfuscator;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
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

    @Override
    public List<Client> getClients(String groupId) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getReadCon(dbService, groupId);
        try {
            return getClients(groupId, con);
        } finally {
            dbService.backReadOnlyForGlobal(groupId, con);
        }
    }

    private List<Client> getClients(String groupId, Connection con) throws ClientManagementException {
        List<Client> clients = new LinkedList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, name, description, secret, default_scope, contact_address, website, enabled, registration_date FROM oauth_client WHERE gid = ?");
            stmt.setString(1, groupId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String clientId = rs.getString("id");
                clients.add(fillClient(groupId, clientId, rs, con));
            }

            return clients;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
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
    private DefaultClient getClientById(String groupId, String clientId, Connection con, boolean forUpdate) throws ClientManagementException {
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
            if (rs.next()) {
                return fillClient(groupId, clientId, rs, con);
            }

            return null;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Client registerClient(String groupId, String clientId, String secret, ClientData clientData) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = registerClient(groupId, clientId, secret, clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param groupId The context group ID
     * @param clientId The ID for the client
     * @param secret The client secret
     * @param clientData The client data to create the client from
     * @param con The connection to use
     * @return The newly created client
     * @throws OXException If create operation fails
     */
    private Client registerClient(String groupId, String clientId, String secret, ClientData clientData, Connection con) throws ClientManagementException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO oauth_client (id, gid, secret, name, description, icon, icon_mime_type, default_scope, contact_address, website, enabled, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, clientId);
            stmt.setString(2, groupId);
            stmt.setString(3, obfuscator.obfuscate(secret));
            stmt.setString(4, clientData.getName());
            stmt.setString(5, clientData.getDescription());

            Icon icon = clientData.getIcon();
            stmt.setBlob(6, new ByteArrayInputStream(icon.getData()));
            stmt.setString(7, icon.getMimeType());
            stmt.setString(8, clientData.getDefaultScope());
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
            checkForDuplicateName(e, groupId, clientData);
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Client updateClient(String groupId, String clientId, ClientData clientData) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = updateClient(groupId, clientId, clientData, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            dbService.backWritableForGlobal(groupId, con);
        }
    }

    /**
     * Updates an existing client's attributes according to given client data.
     * @param groupId The context group ID
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @param con The connection to use
     * @return The updated client
     * @throws ClientManagementException
     */
    private Client updateClient(String groupId, String clientId, ClientData clientData, Connection con) throws ClientManagementException {
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
                values.add(new TypedObject(icon.getData(), Types.BLOB));
                values.add(new TypedObject(icon.getMimeType(), Types.VARCHAR));
            }

            if (clientData.containsDefaultScope()) {
                sql.append(" default_scope = ?,");
                values.add(new TypedObject(clientData.getDefaultScope(), Types.VARCHAR));
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
                stmt.setString(pos, groupId);
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

            Client reloaded = getClientById(groupId, clientId, con, false);
            if (reloaded == null) {
                // Client deleted concurrently
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            return reloaded;
        } catch (SQLException e) {
            checkForDuplicateName(e, groupId, clientData);
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
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
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
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
    private boolean unregisterClient(String groupId, String clientId, Connection con) throws ClientManagementException {
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
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
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
            sstmt.setString(1, clientId);
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
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.autocommit(con);
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
    private boolean disableClient(String clientId, Connection con) throws ClientManagementException {
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            Databases.startTransaction(con);
            sstmt = con.prepareStatement("SELECT enabled FROM oauth_client WHERE id = ? FOR UPDATE");
            sstmt.setString(1, clientId);
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
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.autocommit(con);
            Databases.closeSQLStuff(rs, sstmt);
            Databases.closeSQLStuff(ustmt);
        }
    }

    @Override
    public Client revokeClientSecret(String groupId, String clientId, String secret) throws ClientManagementException {
        DatabaseService dbService = getDbService();
        Connection con = getWriteCon(dbService, groupId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            Client client = revokeClientSecret(groupId, clientId, secret, con);

            con.commit();
            rollback = false;
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
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
     * @param secret The new client secret
     * @param con The connection to use
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    private Client revokeClientSecret(String groupId, String clientId, String secret, Connection con) throws ClientManagementException {
        PreparedStatement stmt = null;
        try {
            DefaultClient client = getClientById(groupId, clientId, con, true);
            if (client == null) {
                throw new ClientManagementException(Reason.INVALID_CLIENT_ID, clientId);
            }

            stmt = con.prepareStatement("UPDATE oauth_client SET secret = ? WHERE id = ? AND gid = ?");
            stmt.setString(1, obfuscator.obfuscate(secret));
            stmt.setString(2, clientId);
            stmt.setString(3, groupId);
            stmt.executeUpdate();

            client.setSecret(secret);
            return client;
        } catch (SQLException e) {
            throw new ClientManagementException(e, Reason.STORAGE_ERROR, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void invalidateClient(String groupId, String clientId) {
        // Nothing to do
    }

    private DefaultClient fillClient(String groupId, String clientId, ResultSet clientRS, Connection con) throws SQLException {
        DefaultClient client = new DefaultClient();
        client.setId(clientId);

        client.setName(clientRS.getString("name"));

        String description = clientRS.getString("description");
        if (!clientRS.wasNull()) {
            client.setDescription(description);
        }

        client.setSecret(obfuscator.unobfuscate(clientRS.getString("secret")));

        String defaultScope = clientRS.getString("default_scope");
        if (!clientRS.wasNull()) {
            client.setDefaultScope(Scope.parseScope(defaultScope));
        }

        String contactAddress = clientRS.getString("contact_address");
        if (!clientRS.wasNull()) {
            client.setContactAddress(contactAddress);
        }

        String website = clientRS.getString("website");
        if (!clientRS.wasNull()) {
            client.setWebsite(website);
        }

        client.setEnabled(clientRS.getBoolean("enabled"));
        client.setRegistrationDate(new Date(clientRS.getLong("registration_date")));

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uri FROM oauth_client_uri WHERE client = ?");
            stmt.setString(1, clientId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String uri = rs.getString(1);
                client.addRedirectURI(uri);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        client.setIcon(new LazyIcon(groupId, clientId));
        return client;
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
            throw new ClientManagementException(e, Reason.INTERNAL_ERROR, e.getMessage());
        }
    }

    private static Connection getWriteCon(DatabaseService dbService, String groupId) throws ClientManagementException {
        try {
            return dbService.getWritableForGlobal(groupId);
        } catch (OXException e) {
            throw new ClientManagementException(e, Reason.INTERNAL_ERROR, e.getMessage());
        }
    }

    private static final Pattern DUPLICATE_KEY = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'");

    private static void checkForDuplicateName(SQLException e, String groupId, ClientData clientData) throws ClientManagementException {
        /*
         * SQLState 23000: Integrity Constraint Violation
         * Error: 1586 SQLSTATE: 23000 (ER_DUP_ENTRY_WITH_KEY_NAME)
         * Error: 1062 SQLSTATE: 23000 (ER_DUP_ENTRY)
         * com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry 'Client Name' for key 'gid_name'
         * Message: Duplicate entry '%s' for key '%s'
         */
        if ("23000".equals(e.getSQLState()) && (e.getErrorCode() == 1062 || e.getErrorCode() == 1586)) {
            Matcher matcher = DUPLICATE_KEY.matcher(e.getMessage());
            if (matcher.matches() && "gid_name".equals(matcher.group(2))) {
                throw new ClientManagementException(Reason.DUPLICATE_NAME, clientData.getName(), groupId);
            }
        }
    }

    private static DefaultClient toClient(ClientData clientData) {
        DefaultClient client = new DefaultClient();
        client.setName(clientData.getName());
        client.setDescription(clientData.getDescription());
        client.setDefaultScope(Scope.parseScope(clientData.getDefaultScope()));
        client.setContactAddress(clientData.getContactAddress());
        client.setWebsite(clientData.getWebsite());
        for (String uri : clientData.getRedirectURIs()) {
            client.addRedirectURI(uri);
        }

        client.setIcon(clientData.getIcon());
        return client;
    }

}
