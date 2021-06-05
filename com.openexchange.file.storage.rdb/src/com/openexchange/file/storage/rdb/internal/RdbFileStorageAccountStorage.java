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

package com.openexchange.file.storage.rdb.internal;

import static com.openexchange.java.Autoboxing.I;
import com.google.json.JsonSanitizer;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.file.storage.rdb.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * {@link RdbFileStorageAccountStorage} - The file storage account storage backed by database.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class RdbFileStorageAccountStorage implements FileStorageAccountStorage, SecretEncryptionStrategy<GenericProperty> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbFileStorageAccountStorage.class);

    /**
     * The {@link DatabaseService} class.
     */
    private static final Class<DatabaseService> CLAZZ_DB = DatabaseService.class;

    /**
     * The {@link GenericConfigurationStorageService} class.
     */
    private static final Class<GenericConfigurationStorageService> CLAZZ_GEN_CONF = GenericConfigurationStorageService.class;

    private static final RdbFileStorageAccountStorage INSTANCE = new RdbFileStorageAccountStorage();

    /**
     * Gets the database-backed instance of account storage manager.
     *
     * @return The database-backed instance of account storage manager
     */
    public static RdbFileStorageAccountStorage getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link RdbFileStorageAccountStorage}.
     */
    private RdbFileStorageAccountStorage() {
        super();
    }

    private static final String SQL_SELECT = "SELECT confId, displayName, metaData FROM filestorageAccount WHERE cid = ? AND user = ? AND serviceId = ? AND account = ?";

    private static final String SQL_SELECT_CONFIDS_FOR_USER = "SELECT confId, account FROM filestorageAccount WHERE cid = ? AND user = ? AND serviceId = ?";

    /**
     * Reads the JSON content of the designated column in the current row of specified <code>ResultSet</code> object.
     *
     * @param columnName The column index
     * @param resultSet The <code>ResultSet</code> object
     * @return The JSON content or <code>null</code>
     * @throws SQLException If JSON content cannot be read
     */
    private static JSONObject readJSON(int columnIndex, ResultSet resultSet) throws SQLException {
        JSONObject retval;
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnIndex);
            retval = deserialize(inputStream);
        } catch (SQLException e) {
            if (false == JSONException.isParseException(e)) {
                throw e;
            }

            // Try to sanitize corrupt input
            Streams.close(inputStream);
            inputStream = resultSet.getBinaryStream(columnIndex);
            retval = deserialize(inputStream, true);
        } finally {
            Streams.close(inputStream);
        }
        return retval;
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream) throws SQLException {
        return deserialize(inputStream, false);
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @param withSanitize <code>true</code> if JSON content provided by input stream is supposed to be sanitized; otherwise <code>false</code> to read as-is
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream, boolean withSanitize) throws SQLException {
        if (null == inputStream) {
            return null;
        }

        try {
            if (withSanitize) {
                String jsonish = JsonSanitizer.sanitize(Streams.reader2string(new AsciiReader(inputStream)));
                return new JSONObject(jsonish);
            }

            return new JSONObject(new AsciiReader(inputStream));
        } catch (JSONException e) {
            throw new SQLException(e);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Serializes a JSON object (as used in an account's configuration) to an input stream.
     *
     * @param data The JSON object serialize, or <code>null</code>
     * @return The serialized JSON object, or <code>null</code> if the passed object was <code>null</code> or an empty JSON-Object
     */
    private static InputStream serialize(JSONObject data) {
        if (null == data || data.isEmpty()) {
            return null;
        }
        return new JSONInputStream(data, Charsets.US_ASCII.name());
    }


    @Override
    public FileStorageAccount getAccount(final String serviceId, final int id, final Session session) throws OXException {
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement(SQL_SELECT);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setString(pos++, serviceId);
            stmt.setInt(pos, id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id), serviceId, Integer.valueOf(session.getUserId()), Integer.valueOf(contextId));
            }
            final FileStorageService messagingService = getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
            final DefaultFileStorageAccount account = new DefaultFileStorageAccount();
            account.setId(String.valueOf(id));
            account.setFileStorageService(messagingService);
            account.setDisplayName(rs.getString(2));
            JSONObject metaData = readJSON(3, rs);
            if (metaData != null) {
                account.setMetaData(metaData);
            }
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                final Map<String, Object> configuration = new HashMap<String, Object>();
                final int confId = rs.getInt(1);
                genericConfStorageService.fill(rc, getContext(session), confId, configuration);
                /*
                 * Decrypt password fields for clear-text representation in account's configuration
                 */
                final Set<String> secretPropNames = messagingService.getSecretProperties();
                if (!secretPropNames.isEmpty()) {
                    for (final String passwordElementName : secretPropNames) {
                        final String toDecrypt = (String) configuration.get(passwordElementName);
                        if (null != toDecrypt) {
                            try {
                                final String decrypted = decrypt(toDecrypt, serviceId, id, session, confId, passwordElementName);
                                configuration.put(passwordElementName, decrypted);
                            } catch (OXException x) {
                                // Must not be fatal
                                configuration.put(passwordElementName, "");
                                // Supply a (probably false) password anyway.
                            }
                        }
                    }
                }
                account.setConfiguration(configuration);
            }
            return account;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    /**
     * Gets the first account matching specified account identifier.
     *
     * @param accountId The account identifier
     * @param session The session
     * @return The matching account or <code>null</code>
     * @throws OXException If look-up fails
     */
    public FileStorageAccount getAccount(final int accountId, final Session session) throws OXException {
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement("SELECT confId, displayName, serviceId, metaData FROM filestorageAccount WHERE cid = ? AND user = ? AND account = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final String serviceId = rs.getString(3);
            final FileStorageServiceRegistry registry = getService(FileStorageServiceRegistry.class);
            if (!registry.containsFileStorageService(serviceId)) {
                // No such file storage service known
                LOG.warn("Unknown file storage service: {}", serviceId);
                return null;
            }
            final FileStorageService fsService = registry.getFileStorageService(serviceId);
            final DefaultFileStorageAccount account = new DefaultFileStorageAccount();
            account.setId(String.valueOf(accountId));
            account.setFileStorageService(fsService);
            account.setDisplayName(rs.getString(2));
            JSONObject metaData = readJSON(4,rs);
            if (metaData != null) {
                account.setMetaData(metaData);
            }
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                final Map<String, Object> configuration = new HashMap<String, Object>();
                final int confId = rs.getInt(1);
                genericConfStorageService.fill(rc, getContext(session), confId, configuration);
                /*
                 * Decrypt password fields for clear-text representation in account's configuration
                 */
                final Set<String> secretPropNames = fsService.getSecretProperties();
                if (!secretPropNames.isEmpty()) {
                    for (final String passwordElementName : secretPropNames) {
                        final String toDecrypt = (String) configuration.get(passwordElementName);
                        if (null != toDecrypt) {
                            try {
                                final String decrypted = decrypt(toDecrypt, serviceId, accountId, session, confId, passwordElementName);
                                configuration.put(passwordElementName, decrypted);
                            } catch (OXException x) {
                                // Must not be fatal
                                configuration.put(passwordElementName, "");
                                // Provide a (probably false) password anyway.
                            }
                        }
                    }
                }
                account.setConfiguration(configuration);
            }
            return account;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    private static final String SQL_SELECT_ACCOUNTS = "SELECT account, confId, displayName, metaData FROM filestorageAccount WHERE cid = ? AND user = ? AND serviceId = ?";

    @Override
    public List<FileStorageAccount> getAccounts(final String serviceId, final Session session) throws OXException {
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc = databaseService.getReadOnly(contextId);
        List<FileStorageAccount> accounts;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement(SQL_SELECT_ACCOUNTS);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setString(pos, serviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                accounts = new ArrayList<FileStorageAccount>(4);
                final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                final FileStorageService messagingService;
                {
                    final FileStorageServiceRegistry registry = getService(FileStorageServiceRegistry.class);
                    messagingService = registry.getFileStorageService(serviceId);
                }
                do {
                    final DefaultFileStorageAccount account = new DefaultFileStorageAccount();
                    account.setDisplayName(rs.getString(3));
                    final Map<String, Object> configuration = new HashMap<String, Object>();
                    genericConfStorageService.fill(rc, getContext(session), rs.getInt(2), configuration);
                    account.setConfiguration(configuration);
                    account.setId(String.valueOf(rs.getInt(1)));
                    account.setFileStorageService(messagingService);
                    JSONObject metaData = readJSON(4, rs);
                    if(metaData != null) {
                        account.setMetaData(metaData);
                    }
                    accounts.add(account);
                } while (rs.next());
            } else {
                accounts = Collections.emptyList();
            }
            return accounts;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    /**
     * Gets the identifiers of user-associated accounts of a certain service.
     *
     * @param serviceId The service identifier
     * @param session The session
     * @return The identifiers of user-associated accounts of a certain service
     * @throws OXException If identifiers cannot be returned
     */
    public TIntList getAccountIDs(final String serviceId, final Session session) throws OXException {
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc = databaseService.getReadOnly(contextId);
        TIntList accounts;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement("SELECT account FROM filestorageAccount WHERE cid = ? AND user = ? AND serviceId = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setString(pos, serviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                accounts = new TIntArrayList(4);
                do {
                    accounts.add(rs.getInt(1));
                } while (rs.next());
            } else {
                accounts = new TIntArrayList(0);
            }
            return accounts;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    private static final String SQL_INSERT = "INSERT INTO filestorageAccount (cid, user, account, confId, serviceId, displayName) VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * Performs some basic constraint checks on the given account.
     *
     * @param account The {@link FileStorageAccount} to check
     * @throws OXException in case the account is not valid
     */
    private void checkAccount(FileStorageAccount account) throws OXException {
        if (account.getDisplayName().length() > 128) {
            throw FileStorageExceptionCodes.ACCOUNT_NAME_TOO_LONG.create(I(128));
        }
    }

    @Override
    public int addAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        checkAccount(account);
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Writable connection
         */
        final int contextId = session.getContextId();
        final Connection wc = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        int rollback = 0;
        try {
            Databases.startTransaction(wc); // BEGIN
            rollback = 1;
            /*
             * Save account configuration using generic conf
             */
            final int genericConfId;
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                final Map<String, Object> configuration = null == account.getConfiguration() ? Collections.emptyMap() : new HashMap<String, Object>(account.getConfiguration());
                /*
                 * Encrypt password fields to not having clear-text representation in database
                 */
                final FileStorageService fsService = getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
                final Set<String> secretPropNames = fsService.getSecretProperties();
                if (!secretPropNames.isEmpty()) {
                    for (final String passwordElementName : secretPropNames) {
                        final String toCrypt = (String) configuration.get(passwordElementName);
                        if (null != toCrypt) {
                            final String encrypted = encrypt(toCrypt, session);
                            configuration.put(passwordElementName, encrypted);
                        }
                    }
                }
                genericConfId = genericConfStorageService.save(wc, getContext(session), configuration);
            }
            final int accountId;
            {
                final IDGeneratorService idGeneratorService = Services.getOptionalService(IDGeneratorService.class);
                if (null == idGeneratorService) {
                    accountId = genericConfId;
                } else {
                    int id = idGeneratorService.getId("com.openexchange.file.storage.account", contextId);
                    while (id <= 0) {
                        id = idGeneratorService.getId("com.openexchange.file.storage.account", contextId);
                    }
                    accountId = id;
                }
            }
            /*
             * Insert account data
             */
            stmt = wc.prepareStatement(SQL_INSERT);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos++, accountId);
            stmt.setInt(pos++, genericConfId);
            stmt.setString(pos++, serviceId);
            stmt.setString(pos, account.getDisplayName());
            stmt.executeUpdate();
            wc.commit(); // COMMIT
            rollback = 2;
            return accountId;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(wc);
                }
                Databases.autocommit(wc);
            }
            databaseService.backWritable(contextId, wc);
        }
    }

    @Override
    public void update(final String recrypted, final GenericProperty prop) throws OXException {
        final HashMap<String, Object> update = new HashMap<String, Object>();
        update.put(prop.propertyName, recrypted);
        final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
        final Session session = prop.session;
        genericConfStorageService.update(getContext(session), prop.confId, update);
        // Invalidate account
        try {
            CachingFileStorageAccountStorage.getInstance().invalidate(prop.serviceId, prop.id, session.getUserId(), session.getContextId());
        } catch (Exception e) {
            // Ignore
        }
    }

    private String encrypt(final String toCrypt, final Session session) throws OXException {
        final SecretEncryptionService<GenericProperty> encryptionService = getService(SecretEncryptionFactoryService.class).createService(this);
        return encryptionService.encrypt(session, toCrypt);
    }

    private String decrypt(final String toDecrypt, final String serviceId, final int id, final Session session, final int confId, final String propertyName) throws OXException {
        final SecretEncryptionService<GenericProperty> encryptionService = getService(SecretEncryptionFactoryService.class).createService(this);
        return encryptionService.decrypt(session, toDecrypt, new GenericProperty(confId, propertyName, serviceId, id, session));
    }

    private static final String SQL_DELETE = "DELETE FROM filestorageAccount WHERE cid = ? AND user = ? AND serviceId = ? AND account = ?";

    @Override
    public void deleteAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        deleteAccounts(serviceId, new FileStorageAccount[] { account }, new int[] { 0 }, session);
    }

    /**
     * Deletes specified accounts.
     *
     * @param serviceId The service identifier
     * @param accounts The accounts to delete
     * @param genericConfIds The associated identifiers of generic configuration
     * @param session The session
     * @throws OXException If delete operation fails
     */
    public void deleteAccounts(String serviceId, FileStorageAccount[] accounts, int[] genericConfIds, Session session) throws OXException {
        ConnectionProvider connectionProvider;
        {
            Connection con = (Connection) session.getParameter("__connection");
            if (null != con) {
                try {
                    if (Databases.isInTransaction(con)) {
                        // Given connection is already in transaction. Invoke & return immediately.
                        deleteAccounts(serviceId, accounts, genericConfIds, session, con);
                        return;
                    }
                } catch (SQLException e) {
                    throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                }

                // Use given connection
                connectionProvider = new InstanceConnectionProvider(con);
            } else {
                // Acquire a connection using DatabaseService
                DatabaseService databaseService = getService(CLAZZ_DB);
                int contextId = session.getContextId();
                connectionProvider = new DatabaseServiceConnectionProvider(contextId, databaseService);
            }
        }

        // Acquire connection & invoke
        Connection con = connectionProvider.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;

            deleteAccounts(serviceId, accounts, genericConfIds, session, con);

            con.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con); // ROLL-BACK
                }
                Databases.autocommit(con);
            }
            connectionProvider.ungetConnection(con);
        }
    }

    /**
     * Deletes specified accounts using given connection.
     *
     * @param serviceId The service identifier
     * @param accounts The accounts to delete
     * @param genericConfIds The associated identifiers of generic configuration
     * @param session The session
     * @param con the read-write connection to use
     * @throws OXException If delete operation fails
     */
    public void deleteAccounts(String serviceId, FileStorageAccount[] accounts, int[] genericConfIds, Session session, Connection con) throws OXException {
        if (null == con) {
            deleteAccounts(serviceId, accounts, genericConfIds, session);
            return;
        }

        try {
            int contextId = session.getContextId();
            int userId = session.getUserId();
            Context context = getContext(session);
            final DeleteListenerRegistry deleteListenerRegistry = DeleteListenerRegistry.getInstance();
            for (int i = 0; i < accounts.length; i++) {
                final FileStorageAccount account = accounts[i];
                final int accountId = Integer.parseInt(account.getId());
                Map<String, Object> properties = account.getConfiguration();
                deleteListenerRegistry.triggerOnBeforeDeletion(session, accountId, properties, con);
                /*
                 * Delete account configuration using generic conf
                 */
                {
                    GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                    int genericConfId = genericConfIds[i];
                    if (genericConfId <= 0) {
                        genericConfId = optGenericConfId(contextId, userId, serviceId, accountId, con);
                    }
                    if (genericConfId > 0) {
                        genericConfStorageService.delete(con, context, genericConfId);
                    }
                }
                /*
                 * Delete account data
                 */
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(SQL_DELETE);
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    stmt.setString(pos++, serviceId);
                    stmt.setInt(pos, accountId);
                    stmt.executeUpdate();
                    deleteListenerRegistry.triggerOnAfterDeletion(session, accountId, properties, con);
                } finally {
                    Databases.closeSQLStuff(stmt);
                }
            }
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final String SQL_UPDATE = "UPDATE filestorageAccount SET displayName = ?, metaData = ? WHERE cid = ? AND user = ? AND serviceId = ? AND account = ?";

    @Override
    public void updateAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        checkAccount(account);
        final DatabaseService databaseService = getService(CLAZZ_DB);
        /*
         * Writable connection
         */
        final int contextId = session.getContextId();
        final Connection wc = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        int rollback = 0;

        try(final InputStream metaDataStream = serialize(account.getMetadata())) {
            Databases.startTransaction(wc); // BEGIN
            rollback = 1;
            final int accountId = Integer.parseInt(account.getId());
            /*
             * Update account configuration using generic conf
             */
            {
                Map<String, Object> configuration = account.getConfiguration();
                if (null != configuration) {
                    configuration = new HashMap<String, Object>(configuration);
                    final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
                    final int genericConfId = getGenericConfId(contextId, session.getUserId(), serviceId, accountId, wc);
                    /*
                     * Encrypt password fields to not having clear-text representation in database
                     */
                    final FileStorageService messagingService = getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
                    final Set<String> secretPropNames = messagingService.getSecretProperties();
                    if (!secretPropNames.isEmpty()) {
                        for (final String passwordElementName : secretPropNames) {
                            final String toCrypt = (String) configuration.get(passwordElementName);
                            if (null != toCrypt) {
                                final String encrypted = encrypt(toCrypt, session);
                                configuration.put(passwordElementName, encrypted);
                            }
                        }
                    }
                    genericConfStorageService.update(wc, getContext(session), genericConfId, configuration);
                }
            }
            /*
             * Update account data
             */
            final String displayName = account.getDisplayName();
            if (null != displayName || null != metaDataStream) {
                stmt = wc.prepareStatement(SQL_UPDATE);
                int pos = 1;
                stmt.setString(pos++, displayName);
                if(metaDataStream == null) {
                    stmt.setNull(pos++, java.sql.Types.BLOB);
                }
                else {
                    stmt.setBinaryStream(pos++, metaDataStream);
                }
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, session.getUserId());
                stmt.setString(pos++, serviceId);
                stmt.setInt(pos, accountId);
                stmt.executeUpdate();
            }
            wc.commit(); // COMMIT
            rollback = 2;
        } catch (OXException e) {
            throw e;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(wc);
                }
                Databases.autocommit(wc);
            }
            databaseService.backWritable(contextId, wc);
        }
    }

    private static <S> S getService(final Class<? extends S> clazz) throws OXException {
        try {
            return Services.getService(clazz);
        } catch (IllegalStateException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return getService(ContextService.class).getContext(session.getContextId());
    }

    private static int getGenericConfId(final int contextId, final int userId, final String serviceId, final int accountId, final Connection con) throws OXException, SQLException {
        int confId = optGenericConfId(contextId, userId, serviceId, accountId, con);
        if (confId < 0) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(accountId), serviceId, Integer.valueOf(userId), Integer.valueOf(contextId));
        }
        return confId;
    }

    private static int optGenericConfId(final int contextId, final int userId, final String serviceId, final int accountId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, serviceId);
            stmt.setInt(pos, accountId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    public boolean checkSecretCanDecryptStrings(final FileStorageService parentService, final Session session, final String secret) throws OXException {
        final Set<String> secretProperties = parentService.getSecretProperties();
        if (secretProperties.isEmpty()) {
            return true;
        }
        final TIntList confIds = getConfIdsForUser(session.getContextId(), session.getUserId(), parentService.getId());
        final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
        final CryptoService cryptoService = getService(CryptoService.class);

        final Context ctx = getContext(session);
        final HashMap<String, Object> content = new HashMap<String, Object>();
        try {
            for (int i = 0, size = confIds.size(); i < size; i++) {
                final int confId = confIds.get(i);
                content.clear();
                genericConfStorageService.fill(ctx, confId, content);

                for (final String field : secretProperties) {
                    final String encrypted = (String) content.get(field);
                    if (encrypted != null) {
                        cryptoService.decrypt(encrypted, secret);
                    }
                }
            }
        } catch (OXException e) {
            throw e;
        }
        return true;
    }

    private TIntIntMap getConfIdToAccountMappingForUser(final int contextId, final int userId, final String serviceId) throws OXException {
        final DatabaseService databaseService = getService(CLAZZ_DB);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = databaseService.getReadOnly(contextId);
            stmt = con.prepareStatement(SQL_SELECT_CONFIDS_FOR_USER);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, serviceId);
            // Query
            rs = stmt.executeQuery();
            final TIntIntMap ret = new TIntIntHashMap(16);
            while (rs.next()) {
                ret.put(rs.getInt(1), rs.getInt(2));
            }
            return ret;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                databaseService.backReadOnly(contextId, con);
            }
        }
    }

    private TIntList getConfIdsForUser(final int contextId, final int userId, final String serviceId) throws OXException {
        final TIntList confIds = new TIntArrayList(20);
        final DatabaseService databaseService = getService(CLAZZ_DB);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = databaseService.getReadOnly(contextId);
            stmt = con.prepareStatement(SQL_SELECT_CONFIDS_FOR_USER);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, serviceId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                final int confId = rs.getInt(1);
                confIds.add(confId);
            }

        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                databaseService.backReadOnly(contextId, con);
            }
        }
        return confIds;
    }

    public void migrateToNewSecret(final FileStorageService parentService, final String oldSecret, final String newSecret, final Session session) throws OXException {
        final Set<String> secretProperties = parentService.getSecretProperties();
        if (secretProperties.isEmpty()) {
            return;
        }
        final TIntList confIds = getConfIdsForUser(session.getContextId(), session.getUserId(), parentService.getId());
        final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
        final CryptoService cryptoService = getService(CryptoService.class);

        final Context ctx = getContext(session);
        final Map<String, Object> content = new HashMap<String, Object>();
        final Map<String, Object> update = new HashMap<String, Object>();
        try {
            for (int i = 0, size = confIds.size(); i < size; i++) {
                final int confId = confIds.get(i);
                content.clear();
                genericConfStorageService.fill(ctx, confId, content);
                update.clear();
                for (final String field : secretProperties) {
                    final String encrypted = (String) content.get(field);
                    if (!com.openexchange.java.Strings.isEmpty(encrypted)) {
                        try {
                            // Try using the new secret. Maybe this account doesn't need the migration
                            cryptoService.decrypt(encrypted, newSecret);
                        } catch (OXException x) {
                            // Needs migration
                            final String transcripted = cryptoService.encrypt(cryptoService.decrypt(encrypted, oldSecret), newSecret);
                            update.put(field, transcripted);
                        }
                    }
                }
                if (!update.isEmpty()) {
                    genericConfStorageService.update(ctx, confId, update);
                }
            }
        } catch (OXException e) {
            throw e;
        }
    }

    private static final String ACCOUNT_EXISTS = "SELECT 1 FROM filestorageAccount WHERE cid = ? AND user = ? LIMIT 1";

    public boolean hasEncryptedItems(final FileStorageService parentService, final Session session) throws OXException {
        final Set<String> secretProperties = parentService.getSecretProperties();
        if (secretProperties.isEmpty()) {
            return false;
        }
        final DatabaseService databaseService = getService(CLAZZ_DB);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = databaseService.getReadOnly(session.getContextId());
            stmt = con.prepareStatement(ACCOUNT_EXISTS);
            int pos = 1;
            stmt.setInt(pos++, session.getContextId());
            stmt.setInt(pos++, session.getUserId());
            // Query
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                databaseService.backReadOnly(session.getContextId(), con);
            }
        }
    }

    public void cleanUp(final FileStorageService parentService, final String secret, final Session session) throws OXException {
        final Set<String> secretProperties = parentService.getSecretProperties();
        if (secretProperties.isEmpty()) {
            return;
        }
        final String serviceId = parentService.getId();
        final TIntIntMap confId2AccountMap = getConfIdToAccountMappingForUser(session.getContextId(), session.getUserId(), serviceId);
        final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
        final CryptoService cryptoService = getService(CryptoService.class);
        // Proceed...
        final Context ctx = getContext(session);
        final Map<String, Object> content = new HashMap<String, Object>();
        final Map<String, Object> update = new HashMap<String, Object>();
        for (final int confId : confId2AccountMap.keys()) {
            content.clear();
            genericConfStorageService.fill(ctx, confId, content);
            update.clear();
            for (final Map.Entry<String, Object> entry : content.entrySet()) {
                final String field = entry.getKey();
                if (secretProperties.contains(field)) {
                    final String encrypted = entry.getValue().toString();
                    if (!com.openexchange.java.Strings.isEmpty(encrypted)) {
                        try {
                            // Check it
                            cryptoService.decrypt(encrypted, secret);
                        } catch (OXException x) {
                            // Discard
                            update.put(field, "");
                        }
                    }
                }
            }
            if (!update.isEmpty()) {
                genericConfStorageService.update(ctx, confId, update);
            }
        }
    }

    public void removeUnrecoverableItems(final FileStorageService parentService, final String secret, final Session session) throws OXException {
        final Set<String> secretProperties = parentService.getSecretProperties();
        if (secretProperties.isEmpty()) {
            return;
        }
        final String serviceId = parentService.getId();
        final TIntIntMap confId2AccountMap = getConfIdToAccountMappingForUser(session.getContextId(), session.getUserId(), serviceId);
        final GenericConfigurationStorageService genericConfStorageService = getService(CLAZZ_GEN_CONF);
        final CryptoService cryptoService = getService(CryptoService.class);
        // Proceed...
        final Context ctx = getContext(session);
        final Map<String, Object> content = new HashMap<String, Object>();

        List<FileStorageAccount> accountsToDelete = new ArrayList<FileStorageAccount>(confId2AccountMap.size());
        TIntArrayList confIdsToDelete = new TIntArrayList(confId2AccountMap.size());

        for (final int confId : confId2AccountMap.keys()) {
            content.clear();
            genericConfStorageService.fill(ctx, confId, content);
            for (final Map.Entry<String, Object> entry : content.entrySet()) {
                final String field = entry.getKey();
                if (secretProperties.contains(field)) {
                    final String encrypted = entry.getValue().toString();
                    if (!com.openexchange.java.Strings.isEmpty(encrypted)) {
                        try {
                            // Check it
                            cryptoService.decrypt(encrypted, secret);
                        } catch (OXException x) {
                            // Discard
                            if (!confIdsToDelete.contains(confId)) {
                                confIdsToDelete.add(confId);
                                DefaultFileStorageAccount account = new DefaultFileStorageAccount();
                                account.setFileStorageService(parentService);
                                account.setId("" + confId2AccountMap.get(confId));
                                account.setServiceId(serviceId);
                                accountsToDelete.add(account);
                            }
                        }
                    }
                }
            }

            deleteAccounts(serviceId, accountsToDelete.toArray(new FileStorageAccount[accountsToDelete.size()]), confIdsToDelete.toArray(), session);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------------------

    /** Simple helper class to acquire/release a connection */
    private static interface ConnectionProvider {

        /**
         * Gets the connection to use
         *
         * @return The connection
         * @throws OXException If connection cannot be obtained
         */
        Connection getConnection() throws OXException;

        /**
         * Ungets previously acquired connection.
         *
         * @param con The connection to unget
         */
        void ungetConnection(Connection con);

    }

    private static class DatabaseServiceConnectionProvider implements ConnectionProvider {

        private final int contextId;
        private final DatabaseService databaseService;

        DatabaseServiceConnectionProvider(int contextId, DatabaseService databaseService) {
            super();
            this.contextId = contextId;
            this.databaseService = databaseService;
        }

        @Override
        public Connection getConnection() throws OXException {
            return databaseService.getWritable(contextId);
        }

        @Override
        public void ungetConnection(Connection con) {
            databaseService.backWritable(contextId, con);
        }
    }

    private static class InstanceConnectionProvider implements ConnectionProvider {

        private final Connection con;

        InstanceConnectionProvider(Connection con) {
            super();
            this.con = con;
        }

        @Override
        public Connection getConnection() throws OXException {
            return con;
        }

        @Override
        public void ungetConnection(Connection con) {
            // Nothing to do
        }
    }

}
