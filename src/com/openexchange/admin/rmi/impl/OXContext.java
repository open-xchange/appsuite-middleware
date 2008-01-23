
package com.openexchange.admin.rmi.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
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
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;

public class OXContext extends OXContextCommonImpl implements OXContextInterface {

    private final Log log = LogFactory.getLog(this.getClass());

    public OXContext() throws StorageException {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }        
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
        
        new BasicAuthenticator().doAuthentication(auth);
        
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
            ContextStorage cs =ContextStorage.getInstance(); 
            cs.invalidateContext(ctx.getId());
            if(backup_ctx.getLoginMappings()!=null && backup_ctx.getLoginMappings().size()>0){
                Iterator<String> itr = backup_ctx.getLoginMappings().iterator();
                while(itr.hasNext()){                    
                    cs.invalidateLoginInfo(itr.next());
                }
            }
        } catch (ContextException e) {
            log.error("Error invalidating cached infos of context "+ctx.getId()+" in context storage",e);
        }
    }

    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, null, auth);
    }
    
    public Context create(Context ctx, User admin_user,String access_combination_name, Credentials auth)
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
			log.debug(ctx.toString() + " - " + admin_user.toString() + " - "+ access_combination_name.toString() + " - "+ auth.toString());
		}
		
		final BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
		basicAuthenticator.doAuthentication(auth);

		
        UserModuleAccess access = ClientAdminThread.cache.getNamedAccessCombination(access_combination_name.trim());
        if(access==null){
        	// no such access combination name defined in configuration
        	// throw error!
        	throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
        }
    	
    	return createcommon(ctx, admin_user, null, access, auth);
    }

    public Context create(Context ctx, User admin_user,UserModuleAccess access, Credentials auth) 
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
        
        final BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
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
            ContextStorage.getInstance().invalidateContext(ctx.getId());
        } catch (ContextException e) {
            log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
        }
    }

    public void disable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        MaintenanceReason reason = new MaintenanceReason(42);
        disable(ctx, reason, auth);
    }

    private void disable(final Context ctx, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        try {
            doNullCheck(ctx, reason,reason.getId());        
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }        
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx + " - " + reason);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            /*if (!tool.existsReason(reason_id)) {
                throw new NoSuchReasonException();
            }*/
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disable(ctx, reason);
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
        MaintenanceReason reason = new MaintenanceReason(42);
        disableAll(reason, auth);
    }

    private void disableAll(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        try{
            doNullCheck(reason,reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator().doAuthentication(auth);
        
        final int reason_id = reason.getId();
        log.debug("" + reason_id);
        try {
//            if (!tool.existsReason(reason_id)) {
//                throw new NoSuchReasonException();
//            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disableAll(reason);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
//        } catch (final NoSuchReasonException e) {
//            log.error(e.getMessage(), e);
//            throw e;
        }
    }

    public void enable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enable(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        new BasicAuthenticator().doAuthentication(auth);
        
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enableAll();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    // this method will remove getSetup
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        log.debug(ctx);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getData(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Search pattern is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        new BasicAuthenticator().doAuthentication(auth);
        
        log.debug("" + search_pattern);
        
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.listContext(search_pattern);
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
            doNullCheck(filestore, filestore.getId());
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
        MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextDatabase(ctx, db, reason, auth);
    }

    private int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        try{
            doNullCheck(ctx,db,reason, reason.getId());
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
        MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextFilestore(ctx, dst_filestore, reason, auth);
    }

    private int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        try {
            doNullCheck(ctx, dst_filestore,dst_filestore.getId(), reason,reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
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
                final StringBuilder dst = builduppath(ctxdir, new URI(fulldstfilestore.getUrl()));
                
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

    protected Context createmaincall(final Context ctx, final User admin_user, Database db, UserModuleAccess access) throws StorageException, InvalidDataException {
        validateloginmapping(ctx);
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        
        // If access == null, use default create method, 
        // else the new create method with the access objects
        if(access==null){
        	return oxcox.create(ctx, admin_user);
        }else{
        	return oxcox.create(ctx, admin_user,access);
        }
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

	public void changeModuleAccess(Context ctx, UserModuleAccess access,Credentials auth) 
		throws RemoteException,InvalidCredentialsException, NoSuchContextException,	StorageException, InvalidDataException {
				
		try {
			doNullCheck(access);			
		} catch (final InvalidDataException e3) {
			log.error("One of the given arguments for create is null", e3);
			throw e3;
		}	
		
		new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx+" - "+access);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }            
            
            OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            
            // change rights for all users in context to specified one in access
            oxu.changeModuleAccess(ctx, oxu.getAll(ctx), access);
                 
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
		
	}

	public void changeModuleAccess(Context ctx, String access_combination_name,Credentials auth)
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
		
		new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx+" - "+access_combination_name);
        
        try {
        	
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            UserModuleAccess access = ClientAdminThread.cache.getNamedAccessCombination(access_combination_name.trim());
            if(access==null){
            	// no such access combination name defined in configuration
            	// throw error!
            	throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
            }
            
            OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            
            // change rights for all users in context to specified one in access combination name
            oxu.changeModuleAccess(ctx, oxu.getAll(ctx), access);
                 
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
		
	}

	public void downgrade(Context ctx, Credentials auth)
		throws RemoteException, InvalidCredentialsException,NoSuchContextException, StorageException, DatabaseUpdateException,InvalidDataException {
		
		// TODO: OX GROUPWARE MUST SUPPLY AN API FUNCTION FOR DOWNGRADE!
		
	}

	public String getAccessCombinationName(Context ctx, Credentials auth)
		throws RemoteException, InvalidCredentialsException,NoSuchContextException, StorageException, InvalidDataException {
		
		// Resolve admin user and get the module access from db and query cache for access combination name
		try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            // Get admin id and fetch current access object and query cache for its name!
            OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
                        
            return ClientAdminThreadExtended.cache.getNameForAccessCombination(oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx)));            
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
	}

	public UserModuleAccess getModuleAccess(Context ctx, Credentials auth)
		throws RemoteException, InvalidCredentialsException,NoSuchContextException, StorageException, InvalidDataException {
		
		try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        new BasicAuthenticator().doAuthentication(auth);
        
        setIdOrGetIDFromNameAndIdObject(null, ctx);
        
        log.debug(ctx);
        
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            // Get admin id and fetch current access object and return it to the client!
            OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            return oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx));            
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
	}

	
}
