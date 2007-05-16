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

import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.exceptions.ContextException;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.groupware.OXThrowsMultiple;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.ContextExceptionFactory;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXUtilException;
import com.openexchange.admin.jobs.AdminJob;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXCONTEXT, component = Component.ADMIN_CONTEXT)
public class OXContext extends BasicAuthenticator implements OXContextInterface {

    static ContextExceptionFactory CONTEXT_EXCEPTIONS = new ContextExceptionFactory(OXContext.class);

    private final Log log = LogFactory.getLog(this.getClass());

    // error messages and so on
    public static final String MSG_SQL_QUERY_FAILED = " ";

    public static final String MSG_NO_SUCH_USER_IN_CONTEXT = "No such user %s in context %s";

    public static final String MSG_SQL_OPERATION_ERROR = "SQL operation error";

    public static final String MSG_INTERNAL_ERROR = "Internal error";

    public static final String MSG_INVALID_DATA_SENT = "Invalid data sent";

    public static final String RESPONSE_ERROR = "ERROR";

    public static final String LOG_ERROR = "Error";

    public static final String LOG_RESPONSE = "Response - ";

    public static final String LOG_CLIENT_ERROR = "Client error";

    public static final String LOG_PROBLEM_WITH_DB_POOL = "Problem with database connection pool";

    public OXContext() {
        super();
        log.info("Class loaded: " + this.getClass().getName());
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, "Invalid data sent by client" }, exceptionId = { 0, 1 }, msg = { MSG_SQL_OPERATION_ERROR, "Invalid data sent-%s" })
    public Context[] searchByDatabase(final Database db, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException {
        
        doNullCheck(db,auth);
        
        doAuthentication(auth);
        
        final String db_host_url = db.getUrl();
        log.debug("" + db_host_url);
        if (db_host_url != null) {

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByDatabase(db);
            // }catch(PoolException ecp){
            // log.error (LOG_ERROR,ecp);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+ecp.getMessage ());
            // }catch(SQLException sql){
            // log.error (MSG_SQL_OPERATION_ERROR,sql);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create (0).getMessage ());
            // }
        } else {
            throw new InvalidDataException("Invalid db url");
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create
            // (1,db_host_url).getMessage ());
        }
        // log.debug (LOG_RESPONSE+retValue);

    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, "Invalid data sent by client" }, exceptionId = { 2, 3 }, msg = { MSG_SQL_OPERATION_ERROR, "Invalid data sent-%s" })
    public Context[] searchByFilestore(final Filestore filestore, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException {
                
        doNullCheck(filestore,auth);
        
        doAuthentication(auth);
        
        final String filestore_url = filestore.getUrl();
        log.debug("" + filestore_url);
        if (null != filestore_url) {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContextByFilestore(filestore);

            // } catch (PoolException ecp) {
            // log.error (LOG_ERROR,ecp);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+ecp.getMessage ());
            // } catch (SQLException sql) {
            // log.error (MSG_SQL_OPERATION_ERROR,sql);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create (2).getMessage ());
            // }
        } else {
            throw new InvalidDataException("Invalid store url");
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create
            // (3,filestore_url).getMessage ());
        }
        // log.debug (LOG_RESPONSE+retValue);

    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, " " }, exceptionId = { 4, 5 }, msg = { MSG_SQL_OPERATION_ERROR, OXContextException.NO_SUCH_CONTEXT + " %s" })
    public void changeDatabase(final Context ctx, final Database db_handle, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException {
        
        doNullCheck(ctx,db_handle,auth);
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();

        log.debug("" + context_id + " - " + db_handle);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(5, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.changeDatabase(ctx, db_handle);
        // TODO: d7
        // }catch(PoolException ecp){
        // log.error (LOG_ERROR,ecp);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+ecp.getMessage ());
        // }catch(SQLException sql){
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (4).getMessage ());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // // log.debug (LOG_CLIENT_ERROR,ctxe);
        // // retValue.add (RESPONSE_ERROR);
        // // retValue.add (""+ctxe.getMessage ());
        // // }catch(RemoteException remi){
        // // log.error (LOG_ERROR,remi);
        // // retValue.add (RESPONSE_ERROR);
        // // retValue.add (""+remi.getMessage ());
        // }

        // log.debug (LOG_RESPONSE+retValue);

    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, " ", "invalid quota size" }, exceptionId = { 6, 7, 8 }, msg = { MSG_SQL_OPERATION_ERROR, OXContextException.NO_SUCH_CONTEXT + " %s", "Invalid quota size" })
    public void changeStorageData(final Context ctx, final Filestore filestore, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException {
        
        doNullCheck(ctx,filestore,auth);
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();

        log.debug("" + context_id + " - " + filestore);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(7, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.changeStorageData(ctx, filestore);
        // TODO: d7
        // }catch(PoolException ecp){
        // log.error (LOG_ERROR,ecp);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+ecp.getMessage ());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // log.debug (LOG_CLIENT_ERROR,ctxe);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+ctxe.getMessage ());
        // }catch(SQLException sql){
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (6).getMessage ());
        // }catch(QuotaException genxo){
        // log.debug (LOG_CLIENT_ERROR,genxo);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (8).getMessage ());
        // }
        // log.debug (LOG_RESPONSE+retValue);
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT, Category.CODE_ERROR, Category.USER_INPUT, Category.USER_INPUT, Category.USER_INPUT, Category.CODE_ERROR, Category.CODE_ERROR, Category.CODE_ERROR, Category.CODE_ERROR, Category.CODE_ERROR, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, " ", "not implemented", " ", " ", " ", " ", " ", " ", " ", " ", " " }, exceptionId = { 9, 10, 11, 40, 41, 42, 43, 44, 45, 46, 47, 49 }, msg = { MSG_SQL_OPERATION_ERROR, OXContextException.NO_SUCH_CONTEXT + " %s", "Not implemented", OXUtilException.NO_SUCH_STORE + " %s", OXUtilException.NO_SUCH_REASON + " %s", OXContextException.CONTEXT_DISABLED + " %s", "Unable to disable Context %s", "Unable to get Context data %s", "Unable to get filestore directory %s", "Unable to list filestores", "Unable to move filestore", "Src and dst store id is the same: %s" })
    public String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason, final Credentials auth)
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException {
        
        doNullCheck(ctx,dst_filestore_id,reason,auth);
        
        doAuthentication(auth);
        
        Context retval = null;
        final int context_id = ctx.getIdAsInt();
        log.debug("" + ctx.getIdAsInt() + " - " + dst_filestore_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
                // throw CONTEXT_EXCEPTIONS.create(10, ctx.getIdAsInt());
            }
            if (!tool.existsStore(dst_filestore_id.getId())) {
                throw CONTEXT_EXCEPTIONS.create(40, dst_filestore_id);
            }
            if (!tool.existsReason(reason.getId())) {
                throw CONTEXT_EXCEPTIONS.create(41, reason.getId());
            }
            if (!tool.isContextEnabled(ctx)) {
                throw CONTEXT_EXCEPTIONS.create(42, ctx.getIdAsInt());
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            // disable context
            try {
                oxcox.disable(ctx, reason);
            } catch (final StorageException e) {
                throw CONTEXT_EXCEPTIONS.create(43, context_id);
            }

            // find old context store data
            try {
                retval = oxcox.getSetup(ctx);
            } catch (final StorageException e) {
                reEnableContext(ctx, oxcox);
                throw CONTEXT_EXCEPTIONS.create(44, context_id);
            }

            final Filestore fs = retval.getFilestore();
            final int srcStore_id = fs.getId();

            if (srcStore_id == dst_filestore_id.getId()) {
                reEnableContext(ctx, oxcox);
                throw CONTEXT_EXCEPTIONS.create(49, dst_filestore_id);
            }

            final String ctxdir = fs.getName();
            if (ctxdir == null) {
                reEnableContext(ctx, oxcox);
                throw CONTEXT_EXCEPTIONS.create(45, context_id);
            }

            // get src and dst path from filestores
            final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore[] fstores = null;
            try {
                fstores = oxu.listFilestores("*");

                String src = null;
                String dst = null;

                for (final Filestore elem : fstores) {
                    final String s_url = elem.getUrl();
                    final int id = elem.getId();
                    if (id == srcStore_id) {
                        final URI uri = new URI(s_url);
                        src = uri.getPath();
                        if (!dst.endsWith("/")) {
                            dst += "/";
                        }
                        dst += ctxdir;
                        if (dst.endsWith("/")) {
                            dst = dst.substring(0, dst.length() - 1);
                        }
                    } else if (id == dst_filestore_id.getId()) {
                        final URI uri = new URI(s_url);
                        dst = uri.getPath();
                        if (!dst.endsWith("/")) {
                            dst += "/";
                        }
                        dst += ctxdir;
                        if (dst.endsWith("/")) {
                            dst = dst.substring(0, dst.length() - 1);
                        }
                    }
                }

                if (src == null || dst == null) {
                    if (src == null) {
                        log.error("src is null");
                    }
                    if (dst == null) {
                        log.error("dst is null");
                    }
                    reEnableContext(ctx, oxcox);
                    throw CONTEXT_EXCEPTIONS.create(47);
                }

                final FilestoreDataMover fsdm = new FilestoreDataMover(src, dst, context_id, dst_filestore_id.getId());
                // TODO: d7 set return value of jobid in addJob, so we can
                // return the id here
                ClientAdminThreadExtended.ajx.addJob(fsdm, context_id, dst_filestore_id.getId(), reason.getId(), AdminJob.Mode.MOVE_FILESTORE);

            } catch (final StorageException e) {
                reEnableContext(ctx, oxcox);
                throw CONTEXT_EXCEPTIONS.create(46);
            }
            // } catch (PoolException ecp) {
            // log.error(LOG_ERROR, ecp);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + ecp.getMessage());
            // } catch (SQLException sql) {
            // log.error(MSG_SQL_OPERATION_ERROR, sql);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + CONTEXT_EXCEPTIONS.create(9).getMessage());
        } catch (final ContextException ctxe) {
            throw new StorageException(ctxe);
            // log.debug(LOG_CLIENT_ERROR, ctxe);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + ctxe.getMessage());
            // } catch (RemoteException e) {
            // log.debug(LOG_ERROR, e);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + e.getMessage());
        } catch (final URISyntaxException e) {
            throw new StorageException(e);
            // log.debug(LOG_ERROR, e);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + e.getMessage());
        }
        // log.debug(LOG_RESPONSE + retValue);
        // return retValue;
        return null;
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT, Category.CODE_ERROR, Category.USER_INPUT, Category.USER_INPUT, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, " ", "not implemented", "", "", "" }, exceptionId = { 12, 13, 14, 37, 38, 39 }, msg = { MSG_SQL_OPERATION_ERROR, OXContextException.NO_SUCH_CONTEXT + " %s", "Not implemented", OXUtilException.NO_SUCH_REASON + " %s", "Context %s is already disabled.Move already in progress?", "Database with id %s is NOT a master!" })
    public String moveContextDatabase(final Context ctx, final Database database_id, final MaintenanceReason reason, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException, DatabaseUpdateException {
        
        doNullCheck(ctx,database_id,reason,auth);
        
        doAuthentication(auth);
        
        final String retval = null;
        final int context_id = ctx.getIdAsInt();
        final int reason_id = reason.getId();
        log.debug("" + context_id + " - " + database_id + " - " + reason_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsReason(reason_id)) {
                throw CONTEXT_EXCEPTIONS.create(37, reason_id);
            }

            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
                // throw CONTEXT_EXCEPTIONS.create(13, context_id);
            }

            if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
                throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
            }

            if (!tool.isContextEnabled(ctx)) {
                throw CONTEXT_EXCEPTIONS.create(38, context_id);
            }

            if (!tool.isMasterDatabase(database_id.getId())) {
                throw CONTEXT_EXCEPTIONS.create(39, database_id);
            }

            final DatabaseDataMover ddm = new DatabaseDataMover(context_id, database_id.getId(), reason_id);

            // add to job queue
            // retValue.clear();
            // retValue.add("OK");
            ClientAdminThreadExtended.ajx.addJob(ddm, context_id, database_id.getId(), reason_id, AdminJob.Mode.MOVE_DATABASE);
            // TODO: d7
            // } catch (PoolException ecp) {
            // log.error(LOG_ERROR, ecp);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + ecp.getMessage());
            // } catch (SQLException sql) {
            // log.error(MSG_SQL_OPERATION_ERROR, sql);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + CONTEXT_EXCEPTIONS.create(12).getMessage());
        } catch (final ContextException ctxe) {
            throw new StorageException(ctxe);
            // log.debug(LOG_CLIENT_ERROR, ctxe);
            // retValue.add(RESPONSE_ERROR);
            // retValue.add("" + ctxe.getMessage());
        }

        // log.debug(LOG_RESPONSE + retValue);
        return retval;
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { MSG_SQL_QUERY_FAILED }, exceptionId = { 15 }, msg = { MSG_SQL_OPERATION_ERROR })
    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        
        
        
        doAuthentication(auth);
        
        log.debug("");
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.enableAll();
        // TODO: d7
        // }catch(PoolException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+popx.getMessage ());
        // }catch(SQLException sql){
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (15).getMessage ());
        // }
        // log.debug (LOG_RESPONSE+retValue);
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.USER_INPUT }, desc = { MSG_SQL_QUERY_FAILED, "Invalid data" }, exceptionId = { 16, 17 }, msg = { MSG_SQL_OPERATION_ERROR, OXUtilException.NO_SUCH_REASON + " %s" })
    public void disableAll(final MaintenanceReason reason, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException {
        
        doNullCheck(reason,auth);
        
        doAuthentication(auth);
        
        final int reason_id = reason.getId();
        log.debug("" + reason_id);
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.existsReason(reason_id)) {
                throw CONTEXT_EXCEPTIONS.create(17);
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disableAll(reason);
            // TODO: d7
            // }catch(PoolException popx){
            // log.error (LOG_ERROR,popx);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+popx.getMessage ());
            // }catch(SQLException sql){
            // log.error (MSG_SQL_OPERATION_ERROR,sql);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create (16).getMessage ());
        } catch (final ContextException ctxe) {
            throw new StorageException(ctxe);
            // log.debug (LOG_CLIENT_ERROR,ctxe);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+ctxe.getMessage ());
        }
        // log.debug (LOG_RESPONSE+retValue);
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR, Category.SETUP_ERROR }, desc = { " ", MSG_SQL_QUERY_FAILED, MSG_INTERNAL_ERROR }, exceptionId = { 21, 22, 23 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", MSG_SQL_OPERATION_ERROR, MSG_INTERNAL_ERROR + "-%s" })
    public void delete(final Context ctx, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException {
        
       
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(21, context_id);
        }
        
        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.delete(ctx);
        // }catch(SQLException sql){
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (22).getMessage ());
        // }catch(PoolException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+popx.getMessage ());
        // }catch(DBPoolingException pexp){
        // log.error ("Problem with database connection pool",pexp);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+pexp.getMessage ());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // log.debug (LOG_CLIENT_ERROR,ctxe);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+ctxe.getMessage ());
        // }catch(OXContextException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (23,popx.getMessage
        // ()).getMessage ());
        // }catch(RemoteException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (23,popx.getMessage
        // ()).getMessage ());
        // }catch(com.openexchange.groupware.contexts.ContextException
        // pexp3){
        // log.error ("Context error in OX delete API ",pexp3);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+pexp3.getMessage ());
        // }catch(DeleteFailedException pexp4){
        // log.error ("Delete error in OX delete API ",pexp4);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+pexp4.getMessage ());
        // }catch(LdapException pexp5){
        // log.error ("Delete error in OX delete API ",pexp5);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+pexp5.getMessage ());
        // }
        // log.debug (LOG_RESPONSE+retValue);
    }

    public Context[] search(final String search_pattern, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException {
    
        doNullCheck(search_pattern,auth);
        
        doAuthentication(auth);
        
        log.debug("" + search_pattern);
        
            // try{
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.searchContext(search_pattern);
            // TODO: d7
            // }catch(SQLException sql){
            // log.error (MSG_SQL_OPERATION_ERROR,sql);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create (25).getMessage ());
            // }catch(PoolException popx){
            // log.error (LOG_ERROR,popx);
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+popx.getMessage ());
            // }
        
            // retValue.add (RESPONSE_ERROR);
            // retValue.add (""+CONTEXT_EXCEPTIONS.create
            // (24,search_pattern).getMessage ());
        
        // log.debug(LOG_RESPONSE + retval);

    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR, Category.USER_INPUT, Category.USER_INPUT }, desc = { " ", MSG_SQL_QUERY_FAILED, "Invalid data", "context is disabled" }, exceptionId = { 26, 27, 28, 29 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", MSG_SQL_OPERATION_ERROR, OXUtilException.NO_SUCH_REASON + " %s", "Context %s is already disabled" })
    public void disable(final Context ctx, final MaintenanceReason reason, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException {
        
        doNullCheck(ctx,reason,auth);
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        final int reason_id = reason.getId();
        log.debug("" + context_id + " - " + reason_id);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(26, context_id);
        }
        if (!tool.existsReason(reason_id)) {
            throw new InvalidDataException("No such reason");
            // throw CONTEXT_EXCEPTIONS.create(28, reason_id);
        }
        if (!tool.isContextEnabled(ctx)) {
            throw new InvalidDataException("This context is already disabled");
            // throw CONTEXT_EXCEPTIONS.create(29, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.disable(ctx, reason);
        // } catch (final StorageException e) {
        // throw new RemoteException("Error in underlying Storage", e);
        // TODO: d7: Add messages to the underlying
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (27).getMessage ());
        // }catch(PoolException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+popx.getMessage ());
        // } catch (final ContextException ctxe) {
        // log.debug(LOG_CLIENT_ERROR, ctxe);
        // // retValue.add (RESPONSE_ERROR);
        // // retValue.add (""+ctxe.getMessage ());
        // throw new RemoteException("Context exception occured", ctxe);
        // }

    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR }, desc = { " ", MSG_SQL_QUERY_FAILED }, exceptionId = { 30, 31 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", MSG_SQL_OPERATION_ERROR })
    public void enable(final Context ctx, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException {
        
        
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(30, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.enable(ctx);
        // TODO: d7
        // } catch (SQLException sql) {
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (31).getMessage ());
        // } catch (PoolException popx) {
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+popx.getMessage ());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // // log.debug (LOG_CLIENT_ERROR,ctxe);
        // // retValue.add (RESPONSE_ERROR);
        // // retValue.add (""+ctxe.getMessage ());
        // }
        // log.debug (LOG_RESPONSE+retValue);
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR }, desc = { " ", MSG_SQL_QUERY_FAILED }, exceptionId = { 32, 33 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", MSG_SQL_OPERATION_ERROR })
    public Context getSetup(final Context ctx, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException {
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(32, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        return oxcox.getSetup(ctx);
        // TODO: d7
        // }catch(SQLException sql){
        // log.error (MSG_SQL_OPERATION_ERROR,sql);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+CONTEXT_EXCEPTIONS.create (33).getMessage ());
        // }catch(PoolException popx){
        // log.error (LOG_ERROR,popx);
        // retValue.add (RESPONSE_ERROR);
        // retValue.add (""+popx.getMessage ());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // // log.debug (LOG_CLIENT_ERROR,ctxe);
        // // retValue.add (RESPONSE_ERROR);
        // // retValue.add (""+ctxe.getMessage ());
        // }
        // log.debug (LOG_RESPONSE+retValue);
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR }, desc = { " ", MSG_SQL_QUERY_FAILED }, exceptionId = { 34, 35 }, msg = { OXContextException.NO_SUCH_CONTEXT + " %s", MSG_SQL_OPERATION_ERROR })
    public void changeQuota(final Context ctx, final long quota_max, final Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException {
        
        doNullCheck(ctx,auth);
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id + " - " + quota_max);
        // try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            // throw CONTEXT_EXCEPTIONS.create(34, context_id);
        }
        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        oxcox.changeQuota(ctx, quota_max);
        // TODO: d7
        // } catch (SQLException sql) {
        // log.error(MSG_SQL_OPERATION_ERROR, sql);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + CONTEXT_EXCEPTIONS.create(35).getMessage());
        // } catch (PoolException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + popx.getMessage());
        // } catch (final ContextException ctxe) {
        // throw new StorageException(ctxe);
        // // log.debug(LOG_CLIENT_ERROR, ctxe);
        // // retValue.add(RESPONSE_ERROR);
        // // retValue.add("" + ctxe.getMessage());
        // }
        // log.debug(LOG_RESPONSE + retValue);
    }

    @OXThrowsMultiple(category = { Category.USER_INPUT, Category.CODE_ERROR }, desc = { "invalid data", MSG_SQL_QUERY_FAILED }, exceptionId = { 24, 25 }, msg = { "Invalid data sent-%s", MSG_SQL_OPERATION_ERROR })
    public Context create(final Context ctx, final User admin_user, final long quota_max, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException {
        
        doNullCheck(ctx,admin_user,auth);
        
        doAuthentication(auth);
        
        final int context_id = ctx.getIdAsInt();
        log.debug("" + context_id + " - " + quota_max + " - " + admin_user);
        //try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (tool.existsContext(ctx)) {
            throw new ContextExistsException();
        }

        if (!admin_user.attributesforcreateset()) {
            throw new InvalidDataException("Mandatory fields not set");               
        } 

        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        // MonitoringInfos.incrementNumberOfCreateContextCalled();
        return oxcox.create(ctx, admin_user, quota_max);
        // TODO: cutmasta
        // } catch (SQLException sql) {
        // log.error(MSG_SQL_OPERATION_ERROR, sql);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + CONTEXT_EXCEPTIONS.create(18).getMessage());
        //} catch (final ContextException ctxe) {
        //    throw new StorageException(ctxe);
        // log.debug(LOG_CLIENT_ERROR, ctxe);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + ctxe.getMessage());
        // } catch (NoSuchAlgorithmException ctxe) {
        // log.debug(LOG_ERROR, ctxe);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add(""
        // + CONTEXT_EXCEPTIONS.create(20, ctxe.getMessage())
        // .getMessage());
        // } catch (UserException ctxe) {
        // log.debug(LOG_CLIENT_ERROR, ctxe);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + ctxe.getMessage());
        // } catch (PoolException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + popx.getMessage());
        // } catch (DBPoolingException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + popx.getMessage());
        // } catch (OXContextException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add(""
        // + CONTEXT_EXCEPTIONS.create(20, popx.getMessage())
        // .getMessage());
        // } catch (OXException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add("" + CONTEXT_EXCEPTIONS.create(36).getMessage());
        // } catch (RemoteException popx) {
        // log.error(LOG_ERROR, popx);
        // retValue.add(RESPONSE_ERROR);
        // retValue.add(""
        // + CONTEXT_EXCEPTIONS.create(20, popx.getMessage())
        // .getMessage());
        //}
        // log.debug(LOG_RESPONSE + retValue);
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { " " }, exceptionId = { 48 }, msg = { "Unable to disable Context %s" })
    private void reEnableContext(final Context ctx, final OXContextStorageInterface oxcox) throws ContextException {
        try {
            oxcox.enable(ctx);
        } catch (final StorageException e) {
            throw CONTEXT_EXCEPTIONS.create(43, ctx.getIdAsInt());
        }
    }

}
