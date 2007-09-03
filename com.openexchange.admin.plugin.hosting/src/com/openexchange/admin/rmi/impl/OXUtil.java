
package com.openexchange.admin.rmi.impl;

import java.io.File;
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
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;

/**
 * Implementation class for the RMI interface for util
 * 
 * @author d7
 *
 */
public class OXUtil extends OXCommonImpl implements OXUtilInterface {

    private final static Log log = LogFactory.getLog(OXUtil.class);
    
    private final BasicAuthenticator basicauth;
    
    private final OXUtilStorageInterface oxutil;

    public OXUtil() throws RemoteException, StorageException {
        super();
        oxutil = OXUtilStorageInterface.getInstance();
        basicauth = new BasicAuthenticator();
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore registerFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "fstore" }, new Object[] { fstore });
        
            basicauth.doAuthentication(auth);

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

            if (tool.existsStore(fstore.getUrl())) {
                throw new InvalidDataException("Store already exists");           
            }
            try {
                final File file = new File(new URI(fstore.getUrl()));
                if (!file.exists()) {
                    throw new InvalidDataException("No such directory: \"" + fstore.getUrl() + "\"");                
                }
                if (!file.isDirectory()) {
                    throw new InvalidDataException("No directory: \"" + fstore.getUrl() + "\"");
                }
                final int response = oxutil.registerFilestore(fstore);
                log.debug("RESPONSE " + response);
                return new Filestore(response);
            } catch (final URISyntaxException urex) {
                throw new InvalidDataException(urex);
            } catch (final IllegalArgumentException urex) {
                throw new InvalidDataException(urex);
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void changeFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "fstore" }, new Object[] { fstore });

            basicauth.doAuthentication(auth);

            log.debug(fstore.getUrl() + " " + fstore.getMaxContexts() + " " + fstore.getSize() + " " + fstore.getId());

            if (null != fstore.getUrl() && !checkValidStoreURI(fstore.getUrl())) {
                throw new InvalidDataException("Invalid store url " + fstore.getUrl());           
            }

            if (!tool.existsStore(fstore.getId())) {
                throw new InvalidDataException("No such store " + fstore.getUrl());
            }

            oxutil.changeFilestore(fstore);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listFilestore(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore[] listFilestore(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "search_pattern" }, new Object[] { search_pattern });

            basicauth.doAuthentication(auth);

            log.debug(search_pattern);

            if (search_pattern.trim().length() == 0) {
                throw new InvalidDataException("Invalid search pattern");
            }

            return oxutil.listFilestores(search_pattern);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllFilestore(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore[] listAllFilestore(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        // Already logged
        return listFilestore("*", auth);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterFilestore(final Filestore store, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "store", "store.getId" }, new Object[] { store, store.getId() });
        
            basicauth.doAuthentication(auth);

            log.debug(store);

            if (!tool.existsStore(store.getId())) {
                throw new InvalidDataException("No such store");
            }

            if (tool.storeInUse(store.getId())) {
                throw new InvalidDataException("Store " + store + " in use");
            }
            oxutil.unregisterFilestore(store.getId());
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#createMaintenanceReason(com.openexchange.admin.rmi.dataobjects.MaintenanceReason, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason createMaintenanceReason(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "reason" }, new Object[] { reason });

            basicauth.doAuthentication(auth);

            log.debug(reason);

            if (reason.getText() == null || reason.getText().trim().length() == 0) {
                throw new InvalidDataException("Invalid reason text!");
            }
            if (tool.existsReason(reason.getText())) {
                throw new InvalidDataException("Reason already exists!");
            }

            return new MaintenanceReason(oxutil.createMaintenanceReason(reason));
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

//    /**
//     * @param auth
//     * @return
//     * @throws RemoteException
//     * @throws StorageException
//     * @throws InvalidCredentialsException
//     */
//    public MaintenanceReason[] listMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
//        basicauth.doAuthentication(auth);
//
//        return oxutil.getAllMaintenanceReasons();
//    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listMaintenanceReason(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason[] listMaintenanceReason(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "search_pattern" }, new Object[] { search_pattern });
        
            basicauth.doAuthentication(auth);

            return oxutil.listMaintenanceReasons(search_pattern);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllMaintenanceReason(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason[] listAllMaintenanceReason(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        // Already logged
        return listMaintenanceReason("*", auth);
    }
    
//    /**
//     * @param db
//     * @param auth
//     * @throws RemoteException
//     * @throws StorageException
//     * @throws InvalidCredentialsException
//     * @throws InvalidDataException
//     */
//    public void createDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
//        try {
//            doNullCheck(new String[] { "db" }, new Object[] { db });
//        
//            basicauth.doAuthentication(auth);
//
//            log.debug(db.toString());
//
//            try {
//                if (!db.mandatoryCreateMembersSet()) {
//                    throw new InvalidDataException("Mandatory fields not set: "+ db.getUnsetMembers());
//                }
//
//                if(db.getName()!=null && tool.existsDatabaseName(db.getName())){
//                    throw new InvalidDataException("Database " + db.getName() + " already exists!");
//                }            
//
//            } catch (final EnforceableDataObjectException e) {
//                throw new InvalidDataException(e.getMessage());
//            }
//            oxutil.createDatabase(db);
//        } catch (final InvalidDataException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        } catch (final InvalidCredentialsException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        } catch (final StorageException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//    }

//    /**
//     * @param db
//     * @param auth
//     * @throws RemoteException
//     * @throws StorageException
//     * @throws InvalidCredentialsException
//     * @throws InvalidDataException
//     */
//    public void deleteDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
//        try {
//            doNullCheck(new String[] { "db" }, new Object[] { db });
//
//            basicauth.doAuthentication(auth);
//
//            log.debug(db.toString());
//
//            try {
//                if (!db.mandatoryDeleteMembersSet()) {
//                    throw new InvalidDataException("Mandatory fields not set: " + db.getUnsetMembers());
//                } else {
//                    oxutil.deleteDatabase(db);
//                }
//            } catch (EnforceableDataObjectException e) {
//                throw new InvalidDataException(e.getMessage());
//            }
//        } catch (final InvalidDataException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        } catch (final InvalidCredentialsException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        } catch (final StorageException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database registerDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(new String[] { "db" }, new Object[] { db });
        
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
                log.error(e.getMessage(), e);
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
                db.setUrl("jdbc:mysql://" + DEFAULT_HOSTNAME + "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
            }

            return new Database(oxutil.registerDatabase(db));
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
   
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerServer(com.openexchange.admin.rmi.dataobjects.Server, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server registerServer(final Server srv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "srv", "srv.getName" }, new Object[] { srv, srv.getName() });
        
            basicauth.doAuthentication(auth);

            log.debug(srv.getName());

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
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterDatabase(final Database database, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "database" }, new Object[] { database });
        
            basicauth.doAuthentication(auth);

            log.debug(database);

            setIdOrGetIDFromNameAndIdObject(null, database);
            if (!tool.existsDatabase(database.getId())) {
                throw new InvalidDataException("No such database " + database);
            }
            if (tool.poolInUse(database.getId())) {
                throw new StorageException("Pool is in use " + database);
            }

            oxutil.unregisterDatabase(database.getId());
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

  
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterServer(com.openexchange.admin.rmi.dataobjects.Server, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterServer(final Server server, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "server" }, new Object[] { server });
        
            basicauth.doAuthentication(auth);

            log.debug(server);

            setIdOrGetIDFromNameAndIdObject(null, server);
            if (!tool.existsServer(server.getId())) {
                throw new InvalidDataException("No such server " + server);
            }
            if (tool.serverInUse(server.getId())) {
                throw new StorageException("Server " + server+ " is in use");
            }

            oxutil.unregisterServer(server.getId());
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listDatabase(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database[] listDatabase(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "search_pattern" }, new Object[] { search_pattern });

            basicauth.doAuthentication(auth);

            log.debug(search_pattern);

            if (search_pattern.length() == 0) {
                throw new InvalidDataException("Invalid search pattern");
            }

            return oxutil.searchForDatabase(search_pattern);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllDatabase(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database[] listAllDatabase(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        // Already logged
        return listDatabase("*", auth);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listServer(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server[] listServer(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "search_pattern" }, new Object[] { search_pattern });

            basicauth.doAuthentication(auth);

            log.debug(search_pattern);

            if (search_pattern.length() == 0) {
                throw new InvalidDataException("Invalid search pattern");
            }

            return oxutil.searchForServer(search_pattern);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllServer(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server[] listAllServer(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        // Already logged
        return listServer("*", auth);
    }
    
    public void changeDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "db" }, new Object[] { db });
        
            basicauth.doAuthentication(auth);

            log.debug(db.toString());

            setIdOrGetIDFromNameAndIdObject(null, db);
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
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#deleteMaintenanceReason(com.openexchange.admin.rmi.dataobjects.MaintenanceReason[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "reasons" }, new Object[] { reasons });
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
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
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
