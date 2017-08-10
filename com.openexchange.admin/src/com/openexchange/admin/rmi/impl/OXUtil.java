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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.i;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.RecalculationScope;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.StorageInfo;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.snippet.QuotaAwareSnippetService;
import com.openexchange.user.UserService;

/**
 * Implementation class for the RMI interface for util
 *
 * @author d7
 *
 */
public class OXUtil extends OXCommonImpl implements OXUtilInterface {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXUtil.class);

    private final BasicAuthenticator basicauth;
    private final OXUtilStorageInterface oxutil;

    public OXUtil() throws StorageException {
        super();
        oxutil = OXUtilStorageInterface.getInstance();
        basicauth = new BasicAuthenticator();
    }

    @Override
    public Filestore registerFilestore(final Filestore fstore, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(fstore);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug("{} - {}", fstore.getUrl(), fstore.getSize());

        URI uri = checkValidStoreURI(fstore.getUrl());
        if (null == uri) {
            throw new InvalidDataException("Invalid URL sent");
        }

        if (null == fstore.getSize()) {
            fstore.setSize(DEFAULT_STORE_SIZE);
        } else if (fstore.getSize() == -1) {
            throw new InvalidDataException("Invalid store size -1");
        }

        if (null == fstore.getMaxContexts()) {
            fstore.setMaxContexts(DEFAULT_STORE_MAX_CTX);
        }

        if (tool.existsStore(fstore.getUrl())) {
            throw new InvalidDataException("Store already exists");
        }

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            try {
                File file = new File(uri);
                if (!file.exists()) {
                    throw new InvalidDataException("No such directory: \"" + fstore.getUrl() + "\"");
                }
                if (!file.isDirectory()) {
                    throw new InvalidDataException("No directory: \"" + fstore.getUrl() + "\"");
                }
            } catch (final IllegalArgumentException urex) {
                throw new InvalidDataException("Invalid filstore url");
            }
        }

        final int response = oxutil.registerFilestore(fstore);
        log.debug("RESPONSE {}", response);
        return new Filestore(response);

    }

    @Override
    public void changeFilestore(final Filestore fstore, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(fstore);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug("{} {} {} {}", fstore.getUrl(), fstore.getMaxContexts(), fstore.getSize(), fstore.getId());

        if (null != fstore.getUrl() && null == checkValidStoreURI(fstore.getUrl())) {
            throw new InvalidDataException("Invalid store url " + fstore.getUrl());
        }

        if (!tool.existsStore(fstore.getId())) {
            throw new InvalidDataException("No such store " + fstore.getUrl());
        }

        oxutil.changeFilestore(fstore);
    }

    @Override
    public Filestore[] listFilestore(String searchPattern, Credentials credentials, boolean omitUsage) throws StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials myCreds = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(searchPattern);
            if (0 == searchPattern.trim().length()) {
                throw new InvalidDataException("Search pattern is an empty string.");
            }
        } catch (InvalidDataException e) {
            log.error("Client did not sent a search pattern.", e);
            throw e;
        }
        basicauth.doAuthentication(myCreds);
        log.debug(searchPattern);
        return oxutil.listFilestores(searchPattern, omitUsage);
    }

    @Override
    public Filestore[] listFilestore(final String searchPattern, Credentials credentials) throws StorageException, InvalidCredentialsException, InvalidDataException {
        return listFilestore(searchPattern, credentials, false);
    }

    @Override
    public Filestore[] listAllFilestore(Credentials credentials) throws StorageException, InvalidCredentialsException, InvalidDataException {
        return listFilestore("*", credentials);
    }

    @Override
    public void unregisterFilestore(final Filestore store, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(store);
            doNullCheck(store.getId());
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(store.toString());

        if (!tool.existsStore(store.getId())) {
            throw new InvalidDataException("No such store");
        }

        if (tool.storeInUse(store.getId())) {
            throw new InvalidDataException("Store " + store + " in use");
        }
        oxutil.unregisterFilestore(store.getId());
    }


    @Override
    public MaintenanceReason createMaintenanceReason(final MaintenanceReason reason, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(reason);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(reason.toString());

        if (reason.getText() == null || reason.getText().trim().length() == 0) {
            throw new InvalidDataException("Invalid reason text!");
        }
        if (tool.existsReason(reason.getText())) {
            throw new InvalidDataException("Reason already exists!");
        }

        return  new MaintenanceReason(oxutil.createMaintenanceReason(reason));
    }

    public MaintenanceReason[] listMaintenanceReasons(Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        basicauth.doAuthentication(auth);

        return oxutil.getAllMaintenanceReasons();
    }

    @Override
    public MaintenanceReason[] listMaintenanceReason(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("The search_pattern is null");
            log.error("", invalidDataException);
            throw invalidDataException;
        }

        basicauth.doAuthentication(auth);

        return oxutil.listMaintenanceReasons(search_pattern);
    }

    @Override
    public MaintenanceReason[] listAllMaintenanceReason(final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listMaintenanceReason("*", credentials);
    }

    public void createDatabase(final Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: "+ db.getUnsetMembers());
            }

            if(db.getName()!=null && tool.existsDatabaseName(db.getName())){
                throw new InvalidDataException("Database " + db.getName() + " already exists!");
            }

        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        oxutil.createDatabase(db);
    }

    public void deleteDatabase(final Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryDeleteMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + db.getUnsetMembers());
            } else {
                oxutil.deleteDatabase(db);
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Override
    public Database registerDatabase(final Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryRegisterMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + db.getUnsetMembers());
            }
            if(db.getName()!=null && tool.existsDatabaseName(db.getName())){
                throw new InvalidDataException("Database " + db.getName() + " already exists!");
            }

        } catch (final EnforceableDataObjectException e) {
            log.error("", e);
            throw new InvalidDataException(e);
        }

        if (null == db.getDriver()) {
            db.setDriver(DEFAULT_DRIVER);
        }

        if (null == db.getMaxUnits()) {
            db.setMaxUnits(DEFAULT_MAXUNITS);
        }

        if (null == db.getPoolInitial()) {
            db.setPoolInitial(DEFAULT_POOL_INITIAL);
        }

        if (null == db.getPoolMax()) {
            db.setPoolMax(DEFAULT_POOL_MAX);
        }

        if (null == db.getLogin()) {
            db.setLogin(DEFAULT_USER);
        }

        if (null == db.getPoolHardLimit()) {
            db.setPoolHardLimit(DEFAULT_POOL_HARD_LIMIT ? 1 : 0);
        }

        if (null == db.getClusterWeight()) {
            db.setClusterWeight(DEFAULT_DB_WEIGHT);
        } else if (db.getClusterWeight() < 0 || db.getClusterWeight() > 100) {
            throw new InvalidDataException("Clusterweight not within range (0-100)");
        }

        if (null == db.getUrl()) {
            db.setUrl("jdbc:mysql://" + DEFAULT_HOSTNAME + "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=false&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        }

        return new Database(oxutil.registerDatabase(db));
    }


    @Override
    public Server registerServer(final Server srv, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(srv);
            doNullCheck(srv.getName());
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(srv.toString());

        if (srv.getName().trim().length() == 0) {
            throw new InvalidDataException("Invalid server name");
        }

        if (tool.existsServerName(srv.getName())) {
            throw new InvalidDataException("Server " + srv.getName() + " already exists!");
        }

        final Server sr = new Server ();
        sr.setName(srv.getName());
        sr.setId(oxutil.registerServer(srv.getName()));
        return sr;
    }

    @Override
    public void unregisterDatabase(final Database database, final Credentials credentials) throws StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(database);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        basicauth.doAuthentication(null == credentials ? new Credentials("","") : credentials);

        log.debug(database.toString());
        try {
            setIdOrGetIDFromNameAndIdObject(null, database);
        } catch (NoSuchObjectException e) {
         // FIXME normally NoSuchDatabaseException needs to be thrown here. Unfortunately it is not already in the throws declaration.
            throw new StorageException(e);
        }
        if (!tool.existsDatabase(i(database.getId()))) {
            throw new InvalidDataException("No such database " + database);
        }
        if (tool.poolInUse(i(database.getId()))) {
            throw new StorageException("Pool is in use " + database);
        }

        oxutil.unregisterDatabase(i(database.getId()), tool.isMasterDatabase(i(database.getId())));
    }

    @Override
    public void unregisterServer(final Server server, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(server);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(server.toString());

        try {
            setIdOrGetIDFromNameAndIdObject(null, server);
        } catch (NoSuchObjectException e) {
            throw new InvalidDataException(e);
        }
        if (!tool.existsServer(server.getId())) {
            throw new InvalidDataException("No such server " + server);
        }
        if (tool.serverInUse(server.getId())) {
            throw new StorageException("Server " + server+ " is in use");
        }

        oxutil.unregisterServer(server.getId());
    }


    @Override
    public Database[] listDatabase(final String search_pattern, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(search_pattern);

        if (search_pattern.length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }

        return oxutil.searchForDatabase(search_pattern);
    }

    @Override
    public Database[] listAllDatabase(final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listDatabase("*", credentials);
    }

    @Override
    public Database[][] checkDatabase(Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        basicauth.doAuthentication(auth);

        OXToolStorageInterface oxtools = OXToolStorageInterface.getInstance();
        List<List<Database>> databases = oxtools.listSchemasBeingLockedOrNeedsUpdate();

        Database[] needingUpdate;
        {
            List<Database> list = databases.get(0);
            needingUpdate = new Database[list.size()];
            int i = 0;
            for (Database database : list) {
                needingUpdate[i++] = database;
            }
        }

        Database[] currentlyUpdating;
        {
            List<Database> list = databases.get(1);
            currentlyUpdating = new Database[list.size()];
            int i = 0;
            for (Database database : list) {
                currentlyUpdating[i++] = database;
            }
        }

        Database[] outdatedUpdating;
        {
            List<Database> list = databases.get(2);
            outdatedUpdating = new Database[list.size()];
            int i = 0;
            for (Database database : list) {
                outdatedUpdating[i++] = database;
            }
        }

        return new Database[][] { needingUpdate, currentlyUpdating, outdatedUpdating };
    }

    @Override
    public Database[] unblockDatabase(Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, db);
        } catch (NoSuchObjectException e) {
            throw new NoSuchDatabaseException(e);
        }
        final Integer id = db.getId();
        if (!tool.existsDatabase(id)) {
            throw new NoSuchDatabaseException("No such database with id " + id);
        }

        OXToolStorageInterface oxtools = OXToolStorageInterface.getInstance();
        List<Database> unblockedDatabaseSchema = oxtools.unblockDatabaseSchema(db);
        return unblockedDatabaseSchema.toArray(new Database[unblockedDatabaseSchema.size()]);
    }

    @Override
    public Server[] listServer(final String search_pattern, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try{
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(search_pattern);

        if (search_pattern.length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }

        return oxutil.searchForServer(search_pattern);
    }

    @Override
    public Server[] listAllServer(final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listServer("*", credentials);
    }

    @Override
    public void changeDatabase(final Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        log.debug(db.toString());

        try {
            setIdOrGetIDFromNameAndIdObject(null, db);
        } catch (NoSuchObjectException e) {
            // FIXME normally NoSuchDatabaseException needs to be thrown here. Unfortunately it is not already in the throws declaration.
            throw new StorageException(e);
        }
        final Integer id = db.getId();
        if (!tool.existsDatabase(id)) {
            throw new InvalidDataException("No such database with id " + id);
        }

        if(db.getName()!=null && tool.existsDatabaseName(db)){
            throw new InvalidDataException("Database " + db.getName() + " already exists!");
        }

        if (db.getClusterWeight() != null) {
            if (db.getClusterWeight() < 0 || db.getClusterWeight() > 100) {
                throw new InvalidDataException("Clusterweight not within range (0-100)");
            }
        }

        oxutil.changeDatabase(db);
    }

    @Override
    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck((Object[])reasons);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(Arrays.toString(reasons));

        for (final MaintenanceReason element : reasons) {
            if (!tool.existsReason(element.getId())) {
                throw new InvalidDataException("Reason with id " + element + " does not exists");
            }
        }

        final int[] del_ids = new int[reasons.length];
        for (int i = 0; i < reasons.length; i++) {
            del_ids[i] = reasons[i].getId().intValue();
        }

        oxutil.deleteMaintenanceReason(del_ids);
    }

    /**
     * Attempts to parse given URI into an {@link java.net.URI} instance.
     *
     * @param uriToCheck The URI string to check
     * @return The parsed {@link java.net.URI} instance or <code>null</code> if URI string is invalid
     */
    private URI checkValidStoreURI(String uriToCheck) {
        try {
            return new URI(uriToCheck);
        } catch (URISyntaxException e) {
            // given string violates RFC 2396
            return null;
        } catch (NullPointerException e) {
            // given uri is null
            return null;
        } catch (RuntimeException e) {
            // Any unforeseen exception
            return null;
        }
    }

    @Override
    public Database createSchema(Credentials credentials, Integer optDBId) throws RemoteException, StorageException, InvalidCredentialsException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        basicauth.doAuthentication(auth);
        return oxutil.createSchema(optDBId);
    }

    @Override
    public void recalculateFilestoreUsage(Integer contextId, Integer userId, Credentials credentials) throws InvalidCredentialsException, StorageException, RemoteException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(contextId);
        } catch (final InvalidDataException e1) {
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }

        basicauth.doAuthentication(auth);

        doRecalculateFilestoreUsage(contextId, userId, true);
    }

    private void doRecalculateFilestoreUsage(Integer contextId, Integer userId, boolean errorOnMissingUserFilestore) throws StorageException {
        int iContextId = contextId.intValue();
        int iUserId = null == userId ? 0 : userId.intValue();
        boolean contextFilestore = true;
        try {
            QuotaFileStorage quotaFileStorage = null;
            if (iUserId <= 0) {
                // Get context-associated file storage
                quotaFileStorage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(iContextId, Info.administrative());
            } else {
                // Get user-associated file storage (if any)
                QuotaFileStorageService quotaFileStorageService = FileStorages.getQuotaFileStorageService();
                StorageInfo storageInfo = quotaFileStorageService.getFileStorageInfoFor(iUserId, iContextId);
                if (storageInfo.getOwnerInfo().getOwnerId() != iUserId) {
                    // There is no user-associated file storage
                    if (errorOnMissingUserFilestore) {
                        throw new StorageException("User " + iUserId + " in context " + iContextId + " has no individual file storage configured");
                    }
                    // Ignore...
                    return;
                }
                quotaFileStorage = quotaFileStorageService.getQuotaFileStorage(iUserId, iContextId, Info.administrative());
                contextFilestore = false;
            }

            // Only ignore files in case of a context-associated file storage
            if (false == contextFilestore) {
                // A user-associated file storage. Possible ignorable files are of no interest
                quotaFileStorage.recalculateUsage();
                return;
            }

            // A context-associated file storage. Consider possible ignorable files...
            Set<String> filesToIgnore = determineIgnorableFiles(iContextId);
            if (null == filesToIgnore) {
                quotaFileStorage.recalculateUsage();
            } else {
                quotaFileStorage.recalculateUsage(filesToIgnore);
            }
        } catch (OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    private Set<String> determineIgnorableFiles(int contextId) throws OXException {
        Set<String> filesToIgnore = null;

        // The resource cache might use file storage. Depending on its configuration (preview.properties) these files affect the context's quota or not.
        {
            boolean quotaAware = false;
            ConfigurationService configurationService = AdminServiceRegistry.getInstance().getService(ConfigurationService.class, true);
            if (configurationService != null) {
                quotaAware = configurationService.getBoolProperty("com.openexchange.preview.cache.quotaAware", false);
            }

            if (!quotaAware) {
                ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
                Set<String> refIds = metadataStore.loadRefIds(contextId);
                if (null != refIds && !refIds.isEmpty()) {
                    filesToIgnore = new HashSet<String>(refIds);
                }
            }
        }

        // Depending on configuration, snippets do not account to the quota/usage, too
        {
            QuotaAwareSnippetService service = AdminServiceRegistry.getInstance().getService(QuotaAwareSnippetService.class);
            if (service != null && service.ignoreQuota()) {
                List<String> snippetFilesToIgnore = service.getFilesToIgnore(contextId);
                if (null != snippetFilesToIgnore && !snippetFilesToIgnore.isEmpty()) {
                    if (null == filesToIgnore) {
                        filesToIgnore = new HashSet<String>(snippetFilesToIgnore);
                    } else {
                        filesToIgnore.addAll(snippetFilesToIgnore);
                    }
                }
            }
        }
        return filesToIgnore;
    }

    @Override
    public void recalculateFilestoreUsage(RecalculationScope scope, Integer optContextId, Credentials credentials) throws InvalidCredentialsException, StorageException, RemoteException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;

        basicauth.doAuthentication(auth);

        try {
            ContextService contextService = AdminServiceRegistry.getInstance().getService(ContextService.class);
            UserService userService = AdminServiceRegistry.getInstance().getService(UserService.class);
            List<Integer> allContextIds = optContextId == null ? contextService.getAllContextIds() : Collections.singletonList(optContextId);
            for (Integer contextId : allContextIds) {
                if (scope == null || RecalculationScope.ALL.equals(scope)) {
                    Context ctx = contextService.getContext(contextId.intValue());
                    int[] userIds = userService.listAllUser(ctx, false, false);
                    for (int userId : userIds) {
                        doRecalculateFilestoreUsage(contextId, Integer.valueOf(userId), false);
                    }
                    doRecalculateFilestoreUsage(contextId, null, false);
                } else {
                    if (RecalculationScope.USER.equals(scope)) {
                        Context ctx = contextService.getContext(contextId.intValue());
                        int[] userIds = userService.listAllUser(ctx, false, false);
                        for (int userId : userIds) {
                            doRecalculateFilestoreUsage(contextId, Integer.valueOf(userId), false);
                        }
                    } else {
                        doRecalculateFilestoreUsage(contextId, null, false);
                    }
                }
            }
        } catch (OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

}
