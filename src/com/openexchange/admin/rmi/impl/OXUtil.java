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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

    public Filestore registerFilestore(final Filestore fstore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(fstore);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
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
        } catch (final URISyntaxException urex) {
            throw new InvalidDataException("Invalid filstore url");
        } catch (final IllegalArgumentException urex) {
            throw new InvalidDataException("Invalid filstore url");
        }

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
        basicauth.doAuthentication(auth);

        log.debug(fstore.getUrl() + " " + fstore.getMaxContexts() + " " + fstore.getSize() + " " + fstore.getId());

        if (null != fstore.getUrl() && !checkValidStoreURI(fstore.getUrl())) {
            throw new InvalidDataException("Invalid store url " + fstore.getUrl());           
        }
        
        if (!tool.existsStore(fstore.getId())) {
            throw new InvalidDataException("No such store " + fstore.getUrl());
        }

        oxutil.changeFilestore(fstore);
    }

    public Filestore[] listFilestore(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(search_pattern);

        if (search_pattern.trim().length() == 0) {
            throw new InvalidDataException("Invalid search pattern");
        }

        return oxutil.listFilestores(search_pattern);
    }

    public Filestore[] listAllFilestore(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listFilestore("*", auth);
    }
    
    public void unregisterFilestore(final Filestore store, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(store,store.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        basicauth.doAuthentication(auth);

        log.debug(store);

        if (!tool.existsStore(store.getId())) {
            throw new InvalidDataException("No such store");
        }

        if (tool.storeInUse(store.getId())) {
            throw new InvalidDataException("Store " + store + " in use");
        }
        oxutil.unregisterFilestore(store.getId());
    }

    
    public MaintenanceReason createMaintenanceReason(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(reason);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        basicauth.doAuthentication(auth);

        log.debug(reason);

        if (reason.getText() == null || reason.getText().trim().length() == 0) {
            throw new InvalidDataException("Invalid reason text!");
        }
        if (tool.existsReason(reason.getText())) {
            throw new InvalidDataException("Reason already exists!");
        }

        return  new MaintenanceReason(oxutil.createMaintenanceReason(reason));
    }

    public MaintenanceReason[] listMaintenanceReasons(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        basicauth.doAuthentication(auth);

        return oxutil.getAllMaintenanceReasons();
    }

    public MaintenanceReason[] listMaintenanceReason(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("The search_pattern is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        basicauth.doAuthentication(auth);
        
        return oxutil.listMaintenanceReasons(search_pattern);
    }
    
    public MaintenanceReason[] listAllMaintenanceReason(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listMaintenanceReason("*", auth);
    }
    
    public void createDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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

    public void deleteDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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

    public Database registerDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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
    }

   
    public Server registerServer(final Server srv, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(srv,srv.getName());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
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
    }

    public void unregisterDatabase(final Database database, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(database);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
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
    }

  
    public void unregisterServer(final Server server, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(server);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
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
    }

    
    public Database[] listDatabase(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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

    public Database[] listAllDatabase(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listDatabase("*", auth);
    }
    
    public Server[] listServer(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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

    public Server[] listAllServer(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listServer("*", auth);
    }
    
    public void changeDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
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
    }

    public void deleteMaintenanceReason(final MaintenanceReason[] reasons, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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
