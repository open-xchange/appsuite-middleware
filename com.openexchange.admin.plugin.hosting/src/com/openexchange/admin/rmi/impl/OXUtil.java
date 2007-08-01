
package com.openexchange.admin.rmi.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;

public class OXUtil extends OXCommonImpl implements OXUtilInterface {

    private final static Log log = LogFactory.getLog(OXUtil.class);

    public OXUtil() throws RemoteException {
        super();
    }

    public Filestore registerFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(fstore);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(fstore.getUrl() + " - " + fstore.getSize());

        if (!checkValidStoreURI(fstore.getUrl())) {
            throw new InvalidDataException("Invalid url sent");           
        }

        if (null == fstore.getSize()) {
            fstore.setSize(DEFAULT_STORE_SIZE);
        } else if (fstore.getSize() == -1) {
            throw new InvalidDataException("Invalid store size -1");
        }

        if (null == fstore.getMaxContexts()) {
            fstore.setMaxContexts(DEFAULT_STORE_MAX_CTX);
        }
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (tools.existsStore(fstore.getUrl())) {
            throw new InvalidDataException("Store already exists");           
        }

        try {
            final java.io.File f = new java.io.File(new java.net.URI(fstore.getUrl()));
            if (!f.exists()) {
                throw new InvalidDataException("No such directory: \"" + fstore.getUrl() + "\"");                
            }
            if (!f.isDirectory()) {
                throw new InvalidDataException("No directory: \"" + fstore.getUrl() + "\"");
            }
        } catch (final URISyntaxException urex) {
            throw new InvalidDataException("Invalid filstore url");
        } catch (final IllegalArgumentException urex) {
            throw new InvalidDataException("Invalid filstore url");
        }

        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        final int response = oxutil.registerFilestore(fstore);
        log.debug("RESPONSE " + response);
        return new Filestore(response);

    }

    public void changeFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(fstore);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(fstore.getUrl() + " " + fstore.getMaxContexts() + " " + fstore.getSize() + " " + fstore.getId());

        if (null != fstore.getUrl() && !checkValidStoreURI(fstore.getUrl())) {
            throw new InvalidDataException("Invalid store url " + fstore.getUrl());           
        }
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (!tools.existsStore(fstore.getId())) {
            throw new InvalidDataException("No such store " + fstore.getUrl());
        }

        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        oxutil.changeFilestore(fstore);
    }

    public Filestore[] listFilestores(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(search_pattern);

        if (search_pattern.trim().length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }

        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.listFilestores(search_pattern);
    }

    public Filestore[] listAllFilestores(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listFilestores("*", auth);
    }
    
    public void unregisterFilestore(final Filestore store, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(store,store.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(store);

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (!tools.existsStore(store.getId())) {
            throw new InvalidDataException("No such store");
        }

        if (tools.storeInUse(store.getId())) {
            throw new InvalidDataException("Store " + store + " in use");
        }
        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        oxutil.unregisterFilestore(store.getId());
    }

    
    public MaintenanceReason createMaintenanceReason(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(reason);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(reason);

        if (reason.getText() == null || reason.getText().trim().length() == 0) {
            throw new InvalidDataException("Invalid reason text!");
        }
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (tools.existsReason(reason.getText())) {
            throw new InvalidDataException("Reason already exists!");
        }

        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return  new MaintenanceReason(oxutil.createMaintenanceReason(reason));

    }

    public MaintenanceReason[] listMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        new BasicAuthenticator().doAuthentication(auth);

        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.getAllMaintenanceReasons();
    }

    public MaintenanceReason[] listMaintenanceReasons(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("The search_pattern is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.listMaintenanceReasons(search_pattern);
    }
    
    public MaintenanceReason[] listAllMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listMaintenanceReasons("*", auth);
    }
    
    public void createDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: "+ db.getUnsetMembers());
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.createDatabase(db);
    }

    public void deleteDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryDeleteMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + db.getUnsetMembers());
            } else {
                final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
                oxcox.deleteDatabase(db);
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    public Database registerDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(db);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(db.toString());

        try {
            if (!db.mandatoryRegisterMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + db.getUnsetMembers());
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
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
            db.setUrl("jdbc:mysql://" + DEFAULT_HOSTNAME + "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        }
        
        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return new Database(oxcox.registerDatabase(db));
    }

   
    public Server registerServer(final Server srv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(srv,srv.getName());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(srv.getName());

        if (srv.getName().trim().length() == 0) {
            throw new InvalidDataException("Invalid server name");
        }

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (tools.existsServer(srv.getName())) {
            throw new InvalidDataException("Server already exists!");
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        final Server sr = new Server ();
        sr.setName(srv.getName());
        sr.setId(oxcox.registerServer(srv.getName()));
        return sr;
    }

    public void unregisterDatabase(final Database database, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(database,database.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(database);

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsDatabase(database.getId())) {
            throw new InvalidDataException("No such database " + database);
        }
        if (tools.poolInUse(database.getId())) {
            throw new StorageException("Pool is in use " + database);
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.unregisterDatabase(database.getId());
    }

  
    public void unregisterServer(final Server server, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(server,server.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(server);

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsServer(server.getId())) {
            throw new InvalidDataException("No such server " + server);
        }
        if (tools.serverInUse(server.getId())) {
            throw new StorageException("Server " + server+ " is in use");
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.unregisterServer(server.getId());
    }

    
    public Database[] listDatabases(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(search_pattern);

        if (search_pattern.length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.searchForDatabase(search_pattern);
    }

    public Database[] listAllDatabases(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listDatabases("*", auth);
    }
    
    public Server[] listServers(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);
        
        log.debug(search_pattern);
        
        if (search_pattern.length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }
        
        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.searchForServer(search_pattern);
    }

    public Server[] listAllServers(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listServers("*", auth);
    }
    
    public void changeDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(db.toString());

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsDatabase(db.getId())) {
            throw new InvalidDataException("No such database with id " + db.getId());
        }

        if (null != db.getDisplayname()) {
            if (tools.existsDatabase(db.getDisplayname())) {
                throw new InvalidDataException("Database with name " + db.getDisplayname() + " already exists");
            }
        }

        if (db.getClusterWeight() != null) {
            if (db.getClusterWeight() < 0 || db.getClusterWeight() > 100) {
                throw new InvalidDataException("Clusterweight not within range (0-100)");
            }
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.changeDatabase(db);
    }

    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck((Object[])reasons);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);

        log.debug(Arrays.toString(reasons));

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        for (final MaintenanceReason element : reasons) {
            if (!tools.existsReason(element.getId())) {
                throw new InvalidDataException("Reason with id " + element + " does not exists");
            }
        }

        final OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        final int[] del_ids = new int[reasons.length];
        for (int i = 0; i < reasons.length; i++) {
            del_ids[i] = reasons[i].getId().intValue();
        }
        
        oxcox.deleteMaintenanceReason(del_ids);
    }

    private boolean checkValidStoreURI( String uriToCheck ) {
        boolean isOK = true;
        
        try {
            URI.create( uriToCheck );
            isOK = true;
        } catch ( IllegalArgumentException e ) {
            // given string violates RFC 2396
            isOK = false;
        } catch ( NullPointerException e ) {
            // given uri is null
            isOK = false;
        }
        
        return isOK;
    }
}
