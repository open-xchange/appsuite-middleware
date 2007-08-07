
package com.openexchange.admin.rmi.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

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
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
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

    public void change(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        
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
            
            if(ctx.getName()!=null && tool.existsContext(ctx)){
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
            oxcox.change(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }   
        
        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId());
        } catch (ContextException e) {
            log.error("Error invalidating context "+ctx.getId()+" in ox context storage",e);
        }
    
    }

    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, auth);
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

            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
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
            return oxcox.searchContext(search_pattern);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    public Context[] listAll(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", auth);
    }
    
    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByDatabase(db);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Context[] listByFilestore(final Filestore filestore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
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
        final Integer dbid = db.getId();
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
            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or is currently beeing updated");
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
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

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            oxcox.disable(ctx, reason);
            retval = oxcox.getData(ctx);

            final int srcStore_id = retval.getFilestoreId();

            if (srcStore_id == dst_filestore.getId()) {
                reEnableContext(ctx, oxcox);
                throw new OXContextException("Src and dst store id is the same: " + dst_filestore);
            }

            final String ctxdir = retval.getFilestore_name();
            if (ctxdir == null) {
                reEnableContext(ctx, oxcox);
                throw new OXContextException("Unable to get filestore directory " + ctx.getIdAsString());
            }

            // get src and dst path from filestores
            final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            try {
                final Filestore[] fstores = oxu.listFilestores("*");

                final StringBuilder src = new StringBuilder();
                final StringBuilder dst = new StringBuilder();

                for (final Filestore elem : fstores) {
                    final String s_url = elem.getUrl();
                    final int id = elem.getId();
                    final URI uri = new URI(s_url);
                    if (id == srcStore_id) {
                        builduppath(ctxdir, src, uri);
                    } else if (id == dst_filestore.getId()) {
                        builduppath(ctxdir, dst, uri);
                    }
                }

                final OXContextException contextException = new OXContextException("Unable to move filestore");
                if (src == null) {
                    log.error("src is null");
                    reEnableContext(ctx, oxcox);
                    throw contextException;
                } else if (dst == null) {
                    log.error("dst is null");
                    reEnableContext(ctx, oxcox);
                    throw contextException;
                }

                final FilestoreDataMover fsdm = new FilestoreDataMover(src.toString(), dst.toString(), ctx, dst_filestore);
                return TaskManager.getInstance().addJob(fsdm, "movefilestore", "move context " + ctx.getIdAsString() + " to filestore " + dst_filestore.getId());

            } catch (final StorageException e) {
                reEnableContext(ctx, oxcox);
                throw new OXContextException("Unable to list filestores");
            } catch (final IOException e) {
                reEnableContext(ctx, oxcox);
                throw new OXContextException("Unable to list filestores");
            }
        } catch (final URISyntaxException e) {
            throw new StorageException(e);
        } catch (final NoSuchFilestoreException e) {
            log.error(e.getMessage(), e);
            throw e;
        /*} catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;*/
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    protected Context createmaincall(final Context ctx, final User admin_user, Database db) throws StorageException, InvalidDataException {
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        final Context retval = oxcox.create(ctx, admin_user);
        return retval;
    }
    
    private StringBuilder builduppath(final String ctxdir, final StringBuilder src, final URI uri) {
        src.append(uri.getPath());
        if (src.charAt(src.length()) != '/') {
            src.append('/');
        }
        src.append(ctxdir);
        if (src.charAt(src.length()) == '/') {
            src.deleteCharAt(src.length() - 1);
        }
        return src;
    }
    
    
    private void reEnableContext(final Context ctx, final OXContextStorageInterface oxcox) throws StorageException {
        oxcox.enable(ctx);
    }
}
