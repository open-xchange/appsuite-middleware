
package com.openexchange.admin.rmi.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXUtilException;
import com.openexchange.admin.rmi.BasicAuthenticator;
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
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.admin.tools.monitoring.Monitor;

public class OXContext extends BasicAuthenticator implements OXContextInterface {

    
    private final Log log = LogFactory.getLog(this.getClass());

    public OXContext() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }        
    }

    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Database is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        doAuthentication(auth);
        
        final String db_host_url = db.getUrl();
        log.debug("" + db_host_url);
        try {
            if (db_host_url != null) {
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                return oxcox.searchContextByDatabase(db);
            } else {
                throw new InvalidDataException("Invalid db url");
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Context[] listByFilestore(final Filestore filestore, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(filestore);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Filestore is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        doAuthentication(auth);
        
        final String filestore_url = filestore.getUrl();
        log.debug("" + filestore_url);
        try {
            if (null != filestore_url) {
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                return oxcox.searchContextByFilestore(filestore);
            } else {
                throw new InvalidDataException("Invalid store url");
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public String moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        
        try {
            doNullCheck(ctx, ctx.getIdAsInt(),dst_filestore,dst_filestore.getId(), reason,reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        doAuthentication(auth);
        
        Context retval = null;
        
        log.debug("" + ctx.getIdAsInt() + " - " + dst_filestore);
        
        try {
            
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            } else if (!tool.existsStore(dst_filestore.getId())) {
                throw new NoSuchFilestoreException();
            } else if (!tool.existsReason(reason.getId())) {
                throw new NoSuchReasonException();
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
                TaskManager.getInstance().addJob(fsdm, "movefilestore", "move context " + ctx.getIdAsString() + " to filestore " + dst_filestore.getId());

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
        } catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return null;
    }

    public int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        
        try{
            doNullCheck(ctx,ctx.getIdAsInt(),db,db.getId(),reason,reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        final int reason_id = reason.getId();
        if (log.isDebugEnabled()) {
            log.debug("" + context_id + " - " + db + " - " + reason_id);
        }
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsReason(reason_id)) {
                // FIXME: Util in context???
                throw new OXContextException(OXUtilException.NO_SUCH_REASON);
            }
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or is currently beeing updated");
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            if (!tool.isMasterDatabase(db.getId())) {
                throw new OXContextException("Database with id " + db.getId() + " is NOT a master!");
            }
            final DatabaseDataMover ddm = new DatabaseDataMover(ctx, db, reason);

            return TaskManager.getInstance().addJob(ddm, "movedatabase", "move context " + context_id + " to database " + db.getId());
//            ClientAdminThreadExtended.ajx.addJob(ddm, context_id, db.getId(), reason_id, AdminJob.Mode.MOVE_DATABASE);
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

    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        doAuthentication(auth);
        
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enableAll();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
    }

    public void disableAll(final MaintenanceReason reason, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        try{
            doNullCheck(reason,reason.getId());
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        doAuthentication(auth);
        
        final int reason_id = reason.getId();
        log.debug("" + reason_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsReason(reason_id)) {
                throw new NoSuchReasonException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disableAll(reason);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
    }

    public void delete(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        try {
            doNullCheck(ctx,ctx.getIdAsInt());
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.delete(ctx);
            removeFromAuthCache(ctx);
            
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
    }

    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Search pattern is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        doAuthentication(auth);
        
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
    
    public void disable(final Context ctx, final MaintenanceReason reason, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        
        
        try {
            doNullCheck(ctx, ctx.getIdAsInt(),reason,reason.getId());        
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }        
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        final int reason_id = reason.getId();
        log.debug("" + context_id + " - " + reason_id);
        try {
            // try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            if (!tool.existsReason(reason_id)) {
                throw new NoSuchReasonException();
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disable(ctx, reason);
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchReasonException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void enable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx,ctx.getIdAsInt());
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
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

    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        
        try{
            doNullCheck(ctx,ctx.getIdAsInt(),ctx.getMaxQuota(),admin_user);
        } catch (final InvalidDataException e1) {            
            log.error("Invalid data sent by client!", e1);
            throw e1;
        }
        
        doAuthentication(auth);
        
        log.debug("" + ctx + " - " + admin_user);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (tool.existsContext(ctx)) {
                throw new ContextExistsException();
            }
            if (!admin_user.attributesforcreateset()) {
                throw new InvalidDataException("Mandatory fields not set");
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            final Context retval = oxcox.create(ctx, admin_user);
            Monitor.incrementNumberOfCreateContextCalled();
            return retval;
        } catch (final ContextExistsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
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
    
    public void change(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        
        try {
            doNullCheck(ctx,ctx.getIdAsInt());
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
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
    
    }
    
    
    // this method will remove getSetup
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            doNullCheck(ctx,ctx.getIdAsInt());
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
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
}
