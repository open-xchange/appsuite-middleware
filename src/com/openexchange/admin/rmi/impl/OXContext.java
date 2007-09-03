
package com.openexchange.admin.rmi.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchPluginException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.admin.tools.PropertyHelper;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextStorage;

public class OXContext extends OXContextCommonImpl implements OXContextInterface {

    private final Log log = LogFactory.getLog(this.getClass());

    public OXContext() throws StorageException {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }        
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#change(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(new String[] { "ctx" }, new Object[] { ctx });

            validateloginmapping(ctx);

            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            log.debug(ctx);

            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            
            if (ctx.getName() != null && tool.existsContextName(ctx)) {
                throw new InvalidDataException("Context " + ctx.getName() + " already exists!");
            }
            
            // check if he wants to change the filestore id, if yes, make sure filestore with this id exists in the system
            if (ctx.getFilestoreId() != null) {
                if (!tool.existsStore(ctx.getFilestoreId().intValue())) {
                    final InvalidDataException inde = new InvalidDataException("No such filestore with id "+ctx.getFilestoreId());
                    log.error(inde.getMessage(),inde);
                    throw inde;
                }
            }
            
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            final Context backup_ctx = oxcox.getData(ctx); // used for invalidating old login mappings in the cache
            oxcox.change(ctx);

            final ContextStorage cs = ContextStorage.getInstance(); 
            cs.invalidateContext(ctx.getId());
            if (backup_ctx.getLoginMappings() != null && backup_ctx.getLoginMappings().size() > 0) {
                Iterator<String> itr = backup_ctx.getLoginMappings().iterator();
                while(itr.hasNext()){                    
                    cs.invalidateLoginInfo(itr.next());
                }
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchPluginException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final ContextException e) {
            log.error("Error invalidating cached infos of context "+ctx.getId()+" in context storage",e);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#create(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, auth);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#delete(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        try {
            doNullCheck(new String[] { "ctx" }, new Object[] { ctx });
        
            final BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
            basicAuthenticator.doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            log.debug(ctx);

            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.delete(ctx);
            basicAuthenticator.removeFromAuthCache(ctx);

            ContextStorage.getInstance().invalidateContext(ctx.getId());
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final ContextException e) {
            log.error("Error invalidating context " + ctx.getId() + " in ox context storage", e);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#disable(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        // Already logged
        MaintenanceReason reason = new MaintenanceReason(42);
        disable(ctx, reason, auth);
    }

    private void disable(final Context ctx, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        try {
            doNullCheck(new String[] { "ctx", "reason", "reason.getId" }, new Object[] { ctx, reason, reason.getId() });        
        
            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            log.debug(ctx + " - " + reason);

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
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
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

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#disableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        // Already logged
        MaintenanceReason reason = new MaintenanceReason(42);
        disableAll(reason, auth);
    }

    /**
     * @param reason
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchReasonException
     */
    private void disableAll(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        try {
            doNullCheck(new String[] { "reason", "reason.getId" }, new Object[] { reason, reason.getId() });
            new BasicAuthenticator().doAuthentication(auth);

            final int reason_id = reason.getId();
            log.debug("" + reason_id);
//            if (!tool.existsReason(reason_id)) {
//                throw new NoSuchReasonException();
//            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disableAll(reason);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
//        } catch (final NoSuchReasonException e) {
//            log.error(e.getMessage(), e);
//            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#enable(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(new String[] { "ctx" }, new Object[] { ctx });
        
            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            log.debug(ctx);
            
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enable(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#enableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        try {
            new BasicAuthenticator().doAuthentication(auth);
        
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enableAll();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    // this method will remove getSetup
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#getData(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(new String[] { "ctx" }, new Object[] { ctx });
        
            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            log.debug(ctx);

            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getData(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#list(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(new String[] { "search_pattern" }, new Object[] { search_pattern });

            new BasicAuthenticator().doAuthentication(auth);

            log.debug("" + search_pattern);
        
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.listContext(search_pattern);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#listAll(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context[] listAll(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        // Already logged
        return list("*", auth);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#listByDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        try {
            doNullCheck(new String[] { "db" }, new Object[] { db });

            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, db);
            log.debug(db);

            if( !tool.existsDatabase(db.getId()) ) {
                throw new NoSuchDatabaseException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByDatabase(db);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchDatabaseException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXContextInterface#listByFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context[] listByFilestore(final Filestore filestore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        try {
            doNullCheck(new String[] { "filestore", "filestore.getID" }, new Object[] { filestore, filestore.getId() });

            new BasicAuthenticator().doAuthentication(auth);

            log.debug(filestore);

            if (!tool.existsStore(filestore.getId())) {
                throw new NoSuchFilestoreException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByFilestore(filestore);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchFilestoreException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @see com.openexchange.admin.rmi.OXContextInterface#moveContextDatabase(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public int moveContextDatabase(final Context ctx, final Database db, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        // Already logged
        MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextDatabase(ctx, db, reason, auth);
    }

    private int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        try {
            doNullCheck(new String[] { "ctx", "db_reason", "reason.getId" }, new Object[] { ctx, db, reason, reason.getId() });
        
            new BasicAuthenticator().doAuthentication(auth);

            setIdOrGetIDFromNameAndIdObject(null, ctx);
            setIdOrGetIDFromNameAndIdObject(null, db);
            final int reason_id = reason.getId();
            if (log.isDebugEnabled()) {
                log.debug(ctx + " - " + db + " - " + reason_id);
            }

            /*if (!tool.existsReason(reason_id)) {
                // FIXME: Util in context???
                throw new OXContextException(OXUtilException.NO_SUCH_REASON);
            }*/
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or is currently beeing updated");
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
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        // Already logged
        MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextFilestore(ctx, dst_filestore, reason, auth);
    }

    private int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        OXContextStorageInterface oxcox = null;
        try {
            doNullCheck(new String[] { "ctx", "dst_filestore", "dst_filestore.getID", "reason", "reason.getId" }, new Object[] { ctx, dst_filestore, dst_filestore.getId(), reason, reason.getId() });
        
            new BasicAuthenticator().doAuthentication(auth);

            Context retval = null;

            log.debug(ctx+ " - " + dst_filestore);

            oxcox = OXContextStorageInterface.getInstance();

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
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        /*} catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;*/
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw new OXContextException(e);
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (null != oxcox) {
                oxcox.enable(ctx);
            }
        }
    }

    protected Context createmaincall(final Context ctx, final User admin_user, Database db) throws StorageException, InvalidDataException, NoSuchPluginException {
        validateloginmapping(ctx);
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        final Context retval = oxcox.create(ctx, admin_user);
        return retval;
    }
    
    private void validateloginmapping(final Context ctx) throws InvalidDataException, NoSuchPluginException {
        final HashSet<String> loginMappings = ctx.getLoginMappings();
        final String login_regexp = PropertyHelper.getString(PropertyHelper.CHECK_CONTEXT_LOGIN_MAPPING_REGEXP);        
        if (null != loginMappings) {
            for (final String mapping : loginMappings) {
                final String illegal = mapping.replaceAll(login_regexp,"");
                if (illegal.length() > 0) {
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
}
