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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.plugins.SQLQueryExtension;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;

public class OXContext extends OXContextCommonImpl implements OXContextInterface {

    private static final String NAME_OXCACHE = "oxcache";

	private static final String SYMBOLIC_NAME_CACHE = "com.openexchange.caching";

	private final Log log = LogFactory.getLog(this.getClass());

	private PropertyHandlerExtended prop = null;
	
    public OXContext(final BundleContext context) throws StorageException {
        super();
        this.context = context;
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }        
        prop = ClientAdminThreadExtended.cache.getProperties();
    }

    public void change(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        validateloginmapping(ctx);
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx);
        
        Context backup_ctx = null; // used for invalidating old login mappings in the cache
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            if (ctx.getName() != null && tool.existsContextName(ctx)) {
                throw new InvalidDataException("Context " + ctx.getName() + " already exists!");
            }
            
            // check if he wants to change the filestore id, if yes, make sure filestore with this id exists in the system
            if(ctx.getFilestoreId()!=null) {
                if(!tool.existsStore(ctx.getFilestoreId().intValue())){
                    final InvalidDataException inde = new InvalidDataException("No such filestore with id "+ctx.getFilestoreId());
                    log.error(inde.getMessage(),inde);
                    throw inde;
                }
            }
            
            callPluginMethod("change", ctx, auth);
            
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            backup_ctx = oxcox.getData(ctx);
            oxcox.change(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }   
        
        try {
            final ContextStorage cs =ContextStorage.getInstance(); 
            cs.invalidateContext(ctx.getId().intValue());
            if(backup_ctx.getLoginMappings()!=null && backup_ctx.getLoginMappings().size()>0){
                final Iterator<String> itr = backup_ctx.getLoginMappings().iterator();
                while(itr.hasNext()){                    
                    cs.invalidateLoginInfo(itr.next());
                }
            }
        } catch (final ContextException e) {
            log.error("Error invalidating cached infos of context "+ctx.getId()+" in context storage",e);
        }
    }

    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, null, auth);
    }
    
    public Context create(final Context ctx, final User admin_user,final String access_combination_name, final Credentials auth)
		throws RemoteException, StorageException,InvalidCredentialsException, InvalidDataException,	ContextExistsException {
    	
    	// Resolve access rights by name    	
    	try {
			doNullCheck(admin_user, access_combination_name);
			if (access_combination_name.trim().length() == 0) {
				throw new InvalidDataException("Invalid access combination name");
			}
		} catch (final InvalidDataException e3) {
			log.error("One of the given arguments for create is null", e3);
			throw e3;
		}

		if (log.isDebugEnabled()) {
			log.debug(ctx + " - " + admin_user + " - "+ access_combination_name
			    + " - "+ auth);
		}
		
        final UserModuleAccess access = ClientAdminThread.cache.getNamedAccessCombination(access_combination_name.trim());
        if(access==null){
        	// no such access combination name defined in configuration
        	// throw error!
        	throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
        }
    	
    	return createcommon(ctx, admin_user, null, access, auth);
    }

    public Context create(final Context ctx, final User admin_user,final UserModuleAccess access, final Credentials auth) 
    	throws RemoteException,StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException {
    	
    	try {
			doNullCheck(admin_user, access);			
		} catch (final InvalidDataException e3) {
			log.error("One of the given arguments for create is null", e3);
			throw e3;
		}
    	
    	return createcommon(ctx, admin_user, null, access, auth);
    	
    }

    public void delete(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(context);
        basicAuthenticator.doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            if( tool.checkAndUpdateSchemaIfRequired(ctx) ) {
                throw new DatabaseUpdateException("Database is locked or is now beeing updated, please try again later");
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            callPluginMethod("delete", ctx, auth);

            oxcox.delete(ctx);
            basicAuthenticator.removeFromAuthCache(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
        } catch (final ContextException e) {
            log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
        }
    }

    public void disable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(42);
        disable(ctx, reason, auth);
    }

    private void disable(final Context ctx, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        try {
            doNullCheck(ctx, reason);        
            doNullCheck(reason.getId());        
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }        
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx + " - " + reason);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            /*if (!tool.existsReason(reason_id)) {
                throw new NoSuchReasonException();
            }*/
            callPluginMethod("disable", ctx, auth);

            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disable(ctx, reason);
            log.info("Context " + ctx.getId() + " successfully disabled");
            
            try {
                ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
                log.info("Context " + ctx.getId() + " successfully invalidated");
            } catch (final ContextException e) {
                log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
            }
            
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        /*} catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;*/
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        final MaintenanceReason reason = new MaintenanceReason(42);
        disableAll(reason, auth);
    }

    private void disableAll(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        try{
            doNullCheck(reason);
            doNullCheck(reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator(context).doAuthentication(auth);
        
        final int reason_id = reason.getId();
        log.debug("" + reason_id);
        try {
//            if (!tool.existsReason(reason_id)) {
//                throw new NoSuchReasonException();
//            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if( ClientAdminThreadExtended.cache.isMasterAdmin(auth) ) {
                oxcox.disableAll(reason);
            } else {
                callPluginMethod("disableAll", auth);
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
//        } catch (final NoSuchReasonException e) {
//            log.error(e.getMessage(), e);
//            throw e;
        }
        
        // Clear context cache
        // CACHE
        final CacheService cacheService = AdminDaemon.getService(SYMBOLIC_NAME_CACHE, NAME_OXCACHE, context,
				CacheService.class);
		if (null != cacheService) {
	        try {
	        	final Cache cache = cacheService.getCache("Context");
	        	cache.clear();
	        } catch (final CacheException e) {
	            log.error(e.getMessage(), e);
	        } finally {
	        	AdminDaemon.ungetService(SYMBOLIC_NAME_CACHE, NAME_OXCACHE, context);
	        }
        }
        // END OF CACHE
    }

    public void enable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            callPluginMethod("enable", ctx, auth);
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enable(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
        } catch (final ContextException e) {
            log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
        }
    }

    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        new BasicAuthenticator(context).doAuthentication(auth);
        
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if( ClientAdminThreadExtended.cache.isMasterAdmin(auth) ) {
                oxcox.enableAll();
            } else {
                callPluginMethod("enableAll", auth);
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        // Clear context cache
        // CACHE
        final CacheService cacheService = AdminDaemon.getService(SYMBOLIC_NAME_CACHE, NAME_OXCACHE, context,
				CacheService.class);
		if (null != cacheService) {
	        try {
	        	final Cache cache = cacheService.getCache("Context");
	        	cache.clear();
	        } catch (final CacheException e) {
	            log.error(e.getMessage(), e);
	        } finally {
	        	AdminDaemon.ungetService(SYMBOLIC_NAME_CACHE, NAME_OXCACHE, context);
	        }
        }
        // END OF CACHE
    }

    public Context[] getData(Context[] ctxs, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            try {
                doNullCheck((Object[])ctxs);
            } catch (final InvalidDataException e1) {
                log.error("One of the given arguments for getData is null", e1);
                throw e1;
            }
            
            new BasicAuthenticator(context).doAuthentication(auth);
            
            final List<Context> retval = new ArrayList<Context>();
            boolean filled = true;
            for (final Context ctx : ctxs) {
                if (!ctx.isListrun()) {
                    filled = false;
                }
                setIdOrGetIDFromNameAndIdObject(null, ctx);
                log.debug(ctx);
                try {
                    if (!tool.existsContext(ctx)) {
                        throw new NoSuchContextException();
                    }
                } catch (final NoSuchContextException e) {
                    log.error(e.getMessage(), e);
                    throw e;
                }        
            }
            try {
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                
                if (filled) {
                    final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ctxs), auth, oxcox);
                    if (null != callGetDataPlugins) {
                        retval.addAll(callGetDataPlugins);
                    } else {
                        retval.addAll(Arrays.asList(ctxs));
                    }
                } else {
                    final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(oxcox.getData(ctxs)), auth, oxcox);
                    if (null != callGetDataPlugins) {
                        retval.addAll(callGetDataPlugins);
                    } else {
                        retval.addAll(Arrays.asList(ctxs));
                    }
                }
            } catch (final StorageException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
            return retval.toArray(new Context[retval.size()]);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        return getData(new Context[]{ctx}, auth)[0];
    }

    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Search pattern is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        log.debug("" + search_pattern);
        
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            
            SQLQueryExtension temp = null;
            SQLQueryExtension retval = null;
            final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
            for (final Bundle bundle : bundles) {
                final String bundlename = bundle.getSymbolicName();
                if (Bundle.ACTIVE==bundle.getState()) {
                    final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                    if (null != servicereferences) {
                        for (final ServiceReference servicereference : servicereferences) {
                            final Object property = servicereference.getProperty("name");
                            if (null != property && property.toString().equalsIgnoreCase("oxcontext")) {
                                final OXContextPluginInterface oxctx = (OXContextPluginInterface) this.context.getService(servicereference);
                                //TODO: Implement check for contextadmin here
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling list for plugin: " + bundlename);
                                }
                                try {
                                    temp = oxctx.list(search_pattern, auth);
                                    if (null != temp) {
                                        if (null != retval) {
                                            retval = new SQLQueryExtension(retval.getTablename() + "," + temp.getTablename(), retval.getQuerypart() + temp.getQuerypart());
                                        } else {
                                            retval = temp;
                                        }
                                    }
                                } catch (final PluginException e) {
                                    log.error("Error while calling method list of plugin " + bundlename,e);
                                    throw new StorageException(e.getCause());
                                }
                            }
                        }
                    }
                }
            }

            if( null == retval ) {
                return oxcox.listContext(search_pattern);
            } else {
                return oxcox.listContext(search_pattern, retval.getTablename(), retval.getQuerypart());
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    public Context[] listAll(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", auth);
    }

    public Context[] listAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", auth);
    }
    
    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Database is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, db);
        log.debug(db);
        try {
            if( !tool.existsDatabase(db.getId()) ) {
                throw new NoSuchDatabaseException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByDatabase(db);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Context[] listByFilestore(final Filestore filestore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        try {
            doNullCheck(filestore);
            doNullCheck(filestore.getId());
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Filestore is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        new BasicAuthenticator().doAuthentication(auth);
        
        log.debug(filestore);
        try {
            if( !tool.existsStore(filestore.getId()) ) {
                throw new NoSuchFilestoreException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByFilestore(filestore);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @see com.openexchange.admin.rmi.OXContextInterface#moveContextDatabase(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public int moveContextDatabase(final Context ctx, final Database db, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextDatabase(ctx, db, reason, auth);
    }

    private int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        try{
            doNullCheck(ctx,db,reason);
            doNullCheck(reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        setIdOrGetIDFromNameAndIdObject(null, db);
        final int reason_id = reason.getId();
        if (log.isDebugEnabled()) {
            log.debug(ctx + " - " + db + " - " + reason_id);
        }
        try {
            /*if (!tool.existsReason(reason_id)) {
                // FIXME: Util in context???
                throw new OXContextException(OXUtilException.NO_SUCH_REASON);
            }*/
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            if( tool.checkAndUpdateSchemaIfRequired(ctx) ) {
                throw new DatabaseUpdateException("Database is locked or is now beeing updated, please try again later");
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final Integer dbid = db.getId();
            if (!tool.isMasterDatabase(dbid)) {
                throw new OXContextException("Database with id " + dbid + " is NOT a master!");
            }
	        {
	            // Check if target database is already source database
	            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
	            final Context[] results = oxcox.searchContextByDatabase(db);
	            for (final Context context : results) {
					if (context.getId().intValue() == ctx.getId().intValue()) {
						throw new OXContextException("Context with id " + ctx.getId() + " already exists in database with id " + dbid);
					}
				}
	        }
            final DatabaseDataMover ddm = new DatabaseDataMover(ctx, db, reason);

            return TaskManager.getInstance().addJob(ddm, "movedatabase", "move context " + ctx.getIdAsString() + " to database " + dbid);
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextFilestore(ctx, dst_filestore, reason, auth);
    }

    private int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        try {
            doNullCheck(ctx, dst_filestore,reason);
            doNullCheck(dst_filestore.getId(), reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        Context retval = null;
        
        log.debug(ctx+ " - " + dst_filestore);
        
        final OXContextStorageInterface oxcox;
        try {
            oxcox = OXContextStorageInterface.getInstance();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw new OXContextException(e);
        }
        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            } else if (!tool.existsStore(dst_filestore.getId())) {
                throw new NoSuchFilestoreException();
            /*} else if (!tool.existsReason(reason.getId())) {
                throw new NoSuchReasonException();*/
            } else if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException("Unable to disable Context " + ctx.getIdAsString());
            }

            oxcox.disable(ctx, reason);
            retval = oxcox.getData(ctx);

            final int srcStore_id = retval.getFilestoreId();

            if (srcStore_id == dst_filestore.getId()) {
                throw new OXContextException("Src and dst store id is the same: " + dst_filestore);
            }

            final String ctxdir = retval.getFilestore_name();
            if (ctxdir == null) {
                throw new OXContextException("Unable to get filestore directory " + ctx.getIdAsString());
            }

            // get src and dst path from filestores
            final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            try {
                final Filestore srcfilestore = oxu.getFilestore(srcStore_id);
                final StringBuilder src = builduppath(ctxdir, new URI(srcfilestore.getUrl()));
                final Filestore fulldstfilestore = oxu.getFilestore(dst_filestore.getId());
                final String dst = new URI(fulldstfilestore.getUrl()).getPath();
                
                final OXContextException contextException = new OXContextException("Unable to move filestore");
                if (src == null) {
                    log.error("src is null");
                    throw contextException;
                } else if (dst == null) {
                    log.error("dst is null");
                    throw contextException;
                }

                final FilestoreDataMover fsdm = new FilestoreDataMover(src.toString(), dst.toString(), ctx, dst_filestore);
                return TaskManager.getInstance().addJob(fsdm, "movefilestore", "move context " + ctx.getIdAsString() + " to filestore " + dst_filestore.getId());
            } catch (final StorageException e) {
                throw new OXContextException(e);
            } catch (final IOException e) {
                throw new OXContextException(e);
            }
        } catch (final URISyntaxException e) {
            final StorageException storageException = new StorageException(e);
            log.error(storageException.getMessage(), storageException);
            throw storageException;
        } catch (final NoSuchFilestoreException e) {
            log.error(e.getMessage(), e);
            throw e;
        /*} catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;*/
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            oxcox.enable(ctx);
        }
    }

    @Override
    protected Context createmaincall(final Context ctx, final User admin_user, final Database db, final UserModuleAccess access, final Credentials auth) throws StorageException, InvalidDataException {
        validateloginmapping(ctx);
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();


        String DEFAULT_ACCESS_COMBINATION_NAME = prop.getProp("NEW_CONTEXT_DEFAULT_ACCESS_COMBINATION_NAME", "NOT_DEFINED");
        // If not defined or access combination name does NOT exist, use hardcoded fallback!
        UserModuleAccess createaccess = null;
        if( access == null ) {
            if(DEFAULT_ACCESS_COMBINATION_NAME.equals("NOT_DEFINED") || ClientAdminThread.cache.getNamedAccessCombination(DEFAULT_ACCESS_COMBINATION_NAME) == null){
                createaccess = ClientAdminThread.cache.getDefaultUserModuleAccess();
            }else{
                createaccess = ClientAdminThread.cache.getNamedAccessCombination(DEFAULT_ACCESS_COMBINATION_NAME);
            }
        } else {
            createaccess = access;
        }

        Context ret = null;
        try {
            ret = (Context)callPluginMethod("preCreate", ctx, admin_user, createaccess, auth);
        } catch(StorageException e) {
            log.error(e.getMessage(),e);
            oxcox.delete(ctx);
            throw e;
        }
        final String name = ctx.getName();
        final HashSet<String> loginMappings = ctx.getLoginMappings();
        if (null == loginMappings || loginMappings.isEmpty()) {
            ctx.addLoginMapping(ctx.getIdAsString());
        }
        if (null != name) {
            // Add the name of the context to the login mappings and the id
            ctx.addLoginMapping(name);
        }
        ret = oxcox.create(ctx, admin_user, createaccess);
        try {
            ret = (Context)callPluginMethod("postCreate", ctx, admin_user, createaccess, auth);
        } catch(StorageException e) {
            log.error(e.getMessage(),e);
            oxcox.delete(ctx);
            throw e;
        }
        return ret;
    }
    
    private void validateloginmapping(final Context ctx) throws InvalidDataException {
        final HashSet<String> loginMappings = ctx.getLoginMappings();
        final String login_regexp = ClientAdminThreadExtended.cache.getProperties().getProp("CHECK_CONTEXT_LOGIN_MAPPING_REGEXP", "[$%\\.+a-zA-Z0-9_-]");        
        if (null != loginMappings) {
            for (final String mapping : loginMappings) {
                final String illegal = mapping.replaceAll(login_regexp,"");
                if( illegal.length() > 0 ) {
                    throw new InvalidDataException("Illegal chars: \"" + illegal + "\"" + " in login mapping");
                }
            }
        }
    }

    private StringBuilder builduppath(final String ctxdir, final URI uri) {
        final StringBuilder src = new StringBuilder(uri.getPath());
        if (src.charAt(src.length()-1) != '/') {
            src.append('/');
        }
        src.append(ctxdir);
        if (src.charAt(src.length()-1) == '/') {
            src.deleteCharAt(src.length() - 1);
        }
        return src;
    }

	public void changeModuleAccess(final Context ctx, final UserModuleAccess access,final Credentials auth) 
		throws RemoteException,InvalidCredentialsException, NoSuchContextException,	StorageException, InvalidDataException {
				
		try {
			doNullCheck(access);			
		} catch (final InvalidDataException e3) {
			log.error("One of the given arguments for create is null", e3);
			throw e3;
		}	
		
		new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx+" - "+access);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }            

            callPluginMethod("changeModuleAccess", ctx, access, auth);
            
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            
            // change rights for all users in context to specified one in access
            oxu.changeModuleAccess(ctx, oxu.getAll(ctx), access);
                 
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
		
	}

	public void changeModuleAccess(final Context ctx, final String access_combination_name,final Credentials auth)
		throws RemoteException,InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
			
		
		try {
			doNullCheck(access_combination_name);	
			if (access_combination_name.trim().length() == 0) {
				throw new InvalidDataException("Invalid access combination name");
			}
		} catch (final InvalidDataException e3) {
			log.error("One of the given arguments for create is null", e3);
			throw e3;
		}	
		
		new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx+" - "+access_combination_name);
        
        try {
        	
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            final UserModuleAccess access = ClientAdminThread.cache.getNamedAccessCombination(access_combination_name.trim());
            if(access==null){
            	// no such access combination name defined in configuration
            	// throw error!
            	throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
            }
            callPluginMethod("changeModuleAccess", ctx, access_combination_name, auth);
            
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            
            // change rights for all users in context to specified one in access combination name
            oxu.changeModuleAccess(ctx, oxu.getAll(ctx), access);
                 
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
		
	}

    /**
     * {@inheritDoc}
     */
    public void downgrade(final Context ctx, final Credentials auth) throws
        RemoteException, InvalidCredentialsException,NoSuchContextException,
        StorageException, DatabaseUpdateException,InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException
                = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        new BasicAuthenticator(context).doAuthentication(auth);

        setIdOrGetIDFromNameAndIdObject(null, ctx);

        log.debug(ctx);

        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        try {
            callPluginMethod("downgrade", ctx, auth);
            oxcox.downgrade(ctx);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
        } catch (final ContextException e) {
            log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
        }
    }

	public String getAccessCombinationName(final Context ctx, final Credentials auth)
		throws RemoteException, InvalidCredentialsException,NoSuchContextException, StorageException, InvalidDataException {
		
		// Resolve admin user and get the module access from db and query cache for access combination name
		try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            callPluginMethod("getAccessCombinationName", ctx, auth);
            // Get admin id and fetch current access object and query cache for its name!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
                        
            return ClientAdminThread.cache.getNameForAccessCombination(oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx)));            
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
	}

	public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth)
		throws RemoteException, InvalidCredentialsException,NoSuchContextException, StorageException, InvalidDataException {
		
		try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator(context).doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            callPluginMethod("getModuleAccess", ctx, auth);
            // Get admin id and fetch current access object and return it to the client!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            return oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx));            
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
	}

    private List<Context> callGetDataPlugins(final List<Context> ctxs, final Credentials auth, final OXContextStorageInterface oxcox) throws StorageException {
        List<OXCommonExtension> retval = null;
        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxcontext")) {
                            final OXContextPluginInterface oxctx = (OXContextPluginInterface) this.context.getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling getData for plugin: " + bundlename);
                            }
                            try {
                                retval = oxctx.getData(ctxs, auth);
                                addExtensionToContext(ctxs, retval, bundlename);
                            } catch (final PluginException e) {
                                log.error("Error while calling method list of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            }
                        }
                    }
                }
            }
        }
        return ctxs;
    }

    private void addExtensionToContext(List<Context> ctxs, List<OXCommonExtension> retval, String bundlename) throws PluginException {
        if (null == retval) {
            throw new PluginException("The plugin: " + bundlename + " returned an empty extension list, this is not allowed");
        }
        if (retval.size() != ctxs.size()) {
            throw new PluginException("After the call of plugin: " + bundlename + " the size of the context and the extensions differ");
        }
        for (int i = 0; i < retval.size(); i++) {
            try {
                ctxs.get(i).addExtension(retval.get(i));
            } catch (final DuplicateExtensionException e) {
                throw new PluginException(e);
            }
        }
    }

}
