/**
 * 
 */
package com.openexchange.admin.reseller.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
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
import com.openexchange.admin.soap.OXSOAPRMIMapper;


/**
 * @author choeger
 *
 */
public class OXResellerContext extends OXSOAPRMIMapper {

    public OXResellerContext() throws RemoteException {
        super(OXContextInterface.class);
    }

    private void changeWrapper(ResellerContext ctx, UserModuleAccess access, String access_combination_name, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        Context cin = resellerContext2Context(ctx);
        if( access == null && access_combination_name == null ) {
            ((OXContextInterface)rmistub).change(cin, auth);
        } else if( access != null ) {
            ((OXContextInterface)rmistub).changeModuleAccess(cin, access, auth);
        } else if( access_combination_name != null ) {
            ((OXContextInterface)rmistub).changeModuleAccess(cin, access_combination_name, auth);   
        }
    }
    /**
     * Same as {@link OXContextInterface#change(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void change(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, null, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, null, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#changeModuleAccess(Context, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param access
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void changeModuleAccess(ResellerContext ctx, UserModuleAccess access, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, access, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, access, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#changeModuleAccess(Context, String, Credentials)}
     * 
     * @param ctx
     * @param access_combination_name
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void changeModuleAccessByName(ResellerContext ctx, String access_combination_name, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, null, access_combination_name, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, null, access_combination_name, auth);
        }
    }

    private ResellerContext createWrapper(ResellerContext ctx, User admin_user, UserModuleAccess access, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        Context cin = resellerContext2Context(ctx);
        Context res = null;
        if( access == null && access_combination_name == null ) {
            res = ((OXContextInterface)rmistub).create(cin, admin_user, auth);
        } else if( access != null ) {
            res = ((OXContextInterface)rmistub).create(ctx, admin_user, access, auth);
        } else if( access_combination_name != null ) {
            res = ((OXContextInterface)rmistub).create(ctx, admin_user, access_combination_name, auth);
        }
        return new ResellerContext(res);
    }
    /**
     * Same as {@link OXContextInterface#create(Context, User, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext create(ResellerContext ctx, User admin_user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, null, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, null, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#create(Context, User, String, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param access_combination_name
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext createModuleAccessByName(ResellerContext ctx, User admin_user, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, null, access_combination_name, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, null, access_combination_name, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#create(Context, User, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param access
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext createModuleAccess(ResellerContext ctx, User admin_user, UserModuleAccess access, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, access, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, access, null, auth);
        }
    }

    private void deleteWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        Context cin = resellerContext2Context(ctx);
        ((OXContextInterface)rmistub).delete(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#delete(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void delete(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            deleteWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            deleteWrapper(ctx, auth);
        }
    }

    private void disableWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        if( ctx != null ) {
            Context cin = resellerContext2Context(ctx);
            ((OXContextInterface)rmistub).disable(cin, auth);       
        } else {
            ((OXContextInterface)rmistub).disableAll(auth);       
        }
    }
    /**
     * Same as {@link OXContextInterface#disable(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws NoSuchReasonException
     * @throws OXContextException
     * @throws DuplicateExtensionException 
     */
    public void disable(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException, DuplicateExtensionException {
        reconnect();
        try {
            disableWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            disableWrapper(ctx, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#disableAll(Credentials)}
     * 
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchReasonException
     * @throws OXContextException 
     * @throws NoSuchContextException 
     * @throws DuplicateExtensionException 
     */
    public void disableAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException, DuplicateExtensionException, NoSuchContextException, OXContextException {
        reconnect();
        try {
            disableWrapper(null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            disableWrapper(null, auth);
        }
    }

    private void downgradeWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        Context cin = resellerContext2Context(ctx);
        ((OXContextInterface)rmistub).downgrade(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#downgrade(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void downgrade(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            downgradeWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            downgradeWrapper(ctx, auth);
        }
    }

    private void enableWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if( ctx != null ) {
            Context cin = resellerContext2Context(ctx);
            ((OXContextInterface)rmistub).enable(cin, auth);       
        } else {
            ((OXContextInterface)rmistub).enableAll(auth);       
        }
    }
    /**
     * Same as {@link OXContextInterface#enable(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void enable(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            enableWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            enableWrapper(ctx, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#enableAll(Credentials)}
     * 
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException 
     * @throws NoSuchContextException 
     * @throws DuplicateExtensionException 
     */
    public void enableAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, DuplicateExtensionException, NoSuchContextException, InvalidDataException {
        reconnect();
        try {
            enableWrapper(null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            enableWrapper(null, auth);
        }
    }

    private String getAccessCombinationNameWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = resellerContext2Context(ctx);
        return ((OXContextInterface)rmistub).getAccessCombinationName(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#getAccessCombinationName(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public String getAccessCombinationName(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getAccessCombinationNameWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getAccessCombinationNameWrapper(ctx, auth);
        }
    }

    private ResellerContext getDataWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = resellerContext2Context(ctx);
        Context res = ((OXContextInterface)rmistub).getData(cin, auth);
        return new ResellerContext(res);
    }
    /**
     * Same as {@link OXContextInterface#getData(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext getData(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getDataWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getDataWrapper(ctx, auth);
        }
    }

    private UserModuleAccess getModuleAccessWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = resellerContext2Context(ctx);
        return ((OXContextInterface)rmistub).getModuleAccess(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#getModuleAccess(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public UserModuleAccess getModuleAccess(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getModuleAccessWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getModuleAccessWrapper(ctx, auth);
        }
    }

    private ResellerContext[] listWrapper(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        final Context[] allctx = ((OXContextInterface)rmistub).list(search_pattern, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#list(String, Credentials)}
     * 
     * @param search_pattern
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public ResellerContext[] list(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return listWrapper(search_pattern, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listWrapper(search_pattern, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#listAll(Credentials)}
     * 
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public ResellerContext[] listAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return listWrapper("*", auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listWrapper("*", auth);
        }
    }

    private ResellerContext[] listByDatabaseWrapper(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException{
        final Context[] allctx = ((OXContextInterface)rmistub).listByDatabase(db, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#listByDatabase(Database, Credentials)}
     * 
     * @param db
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchDatabaseException
     */
    public ResellerContext[] listByDatabase(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        reconnect();
        try {
            return listByDatabaseWrapper(db, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listByDatabaseWrapper(db, auth);
        }
    }

    private ResellerContext[] listByFilestoreWrapper(Filestore fs, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        final Context[] allctx = ((OXContextInterface)rmistub).listByFilestore(fs, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#listByFilestore(Filestore, Credentials)}
     * 
     * @param fs
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchFilestoreException
     */
    public ResellerContext[] listByFilestore(Filestore fs, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        reconnect();
        try {
            return listByFilestoreWrapper(fs, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listByFilestoreWrapper(fs, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#moveContextDatabase(Context, Database, Credentials)}
     * 
     * @param ctx
     * @param dst_database_id
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws OXContextException
     */
    public int moveContextDatabase(ResellerContext ctx, Database dst_database_id, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).moveContextDatabase(ctx, dst_database_id, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).moveContextDatabase(ctx, dst_database_id, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#moveContextFilestore(Context, Filestore, Credentials)}
     * 
     * @param ctx
     * @param dst_filestore_id
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws NoSuchFilestoreException
     * @throws NoSuchReasonException
     * @throws OXContextException
     */
    public int moveContextFilestore(ResellerContext ctx, Filestore dst_filestore_id, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).moveContextFilestore(ctx, dst_filestore_id, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).moveContextFilestore(ctx, dst_filestore_id, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#getAdminId(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     */
    public int getAdminId(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).getAdminId(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).getAdminId(ctx, auth);
        }
    }

    /**
     * @param ctx
     * @return
     * @throws DuplicateExtensionException
     */
    private Context resellerContext2Context(ResellerContext ctx) throws DuplicateExtensionException {
        Context ret = new Context();
        ret.setAverage_size(ctx.getAverage_size());
        ret.setEnabled(ctx.getEnabled());
        ret.setFilestore_name(ctx.getFilestore_name());
        ret.setFilestoreId(ctx.getFilestoreId());
        ret.setLoginMappings(ctx.getLoginMappings());
        ret.setMaintenanceReason(ctx.getMaintenanceReason());
        ret.setMaxQuota(ctx.getMaxQuota());
        ret.setName(ctx.getName());
        ret.setReadDatabase(ctx.getReadDatabase());
        ret.setUsedQuota(ctx.getUsedQuota());
        ret.setWriteDatabase(ctx.getWriteDatabase());
        OXContextExtensionImpl ctxext = new OXContextExtensionImpl();
        ctxext.setCustomid(ctx.getCustomid());
        ctxext.setOwner(ctx.getOwner());
        ctxext.setRestriction(ctx.getRestriction());
        ctxext.setSid(ctx.getSid());
        ret.addExtension(ctxext);
        return ret;
    }


}
