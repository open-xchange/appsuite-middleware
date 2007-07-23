package com.openexchange.admin.rmi.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;


public abstract class OXContextCommonImpl extends OXCommonImpl {

    private final static Log log = LogFactory.getLog(OXContextCommonImpl.class);
    
    protected void createchecks(final Context ctx, final User admin_user, final OXToolStorageInterface tool) throws StorageException, ContextExistsException, InvalidDataException {
        if (tool.existsContext(ctx)) {
            throw new ContextExistsException("Context already exists!");
        }        
        
        if (!admin_user.attributesforcreateset()) {
            throw new InvalidDataException("Mandatory fields in admin user not set");               
        }
    }

    protected abstract Context createmaincall(final Context ctx, final User admin_user, Database db) throws StorageException, InvalidDataException;

    protected Context createcommon(final Context ctx, final User admin_user, final Database db, final Credentials auth) throws InvalidCredentialsException, ContextExistsException, InvalidDataException, StorageException {
        try{
            doNullCheck(ctx,ctx.getIdAsInt(),ctx.getMaxQuota(),admin_user);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context or user not correct");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator().doAuthentication(auth);
        
        if(log.isDebugEnabled()){
            log.debug(ctx + " - " + admin_user + " ");
        }
        
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            createchecks(ctx, admin_user, tool);
            return createmaincall(ctx, admin_user, db);
        } catch (final ContextExistsException e) {
            log.error(e.getMessage(),e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
