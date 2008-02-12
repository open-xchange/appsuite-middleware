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
package com.openexchange.admin.console.context;

import java.rmi.RemoteException;
import java.util.Arrays;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.user.UserHostingAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This class is used to abstract to context related attributes and methods which are only needed
 * in the hosting part of Open-Xchange. This class is not only used to derive from it but it is also
 * used as aggregation inside some object. So the public method are used through aggregation while the
 * protected are used by inheritance.
 * 
 * @author d7
 *
 */
public class ContextHostingAbstraction extends UserHostingAbstraction {
//    private final static char OPT_REASON_SHORT = 'r';
//    private final static String OPT_REASON_LONG= "reason";

    private final static char OPT_NAME_DATABASE_ID_SHORT = 'd';
    private final static String OPT_NAME_DATABASE_ID_LONG = "database";

    private final static char OPT_NAME_DBNAME_SHORT = 'n';
    private final static String OPT_NAME_DBNAME_LONG = "name";


    private final static char OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT = 'L';
    private final static String OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG = "addmapping";
    
    private final static char OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT = 'R';
    private final static String OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG = "removemapping";
    static final char OPT_FILESTORE_SHORT = 'f';
    static final String OPT_FILESTORE_LONG = "filestore";
    
    private Option addLoginMappingOption = null;
    private Option removeLoginMappingOption = null;
    
    private Option databaseIdOption = null;
    private Option databaseNameOption = null;
    
    private String[] remove_mappings = null;
    private String[] add_mappings = null;

    protected Integer dbid = null;
    protected String dbname = null;
    
    protected Integer filestoreid = null;
    
    protected Option targetFilestoreIDOption = null;
    

//    protected Option maintenanceReasonIDOption = null;

//    protected void setMaintenanceReasodIDOption(final AdminParser parser,final boolean required){
//        this.maintenanceReasonIDOption = setShortLongOpt(parser, OPT_REASON_SHORT,OPT_REASON_LONG,"Maintenance reason id",true, convertBooleantoTriState(required));
//    }
//    
    
    @Override
    protected String getObjectName() {
        return "context";
    }

    public void setAddMappingOption(final AdminParser parser,final boolean required ){
        this.addLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG,"Add login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    public void setRemoveMappingOption(final AdminParser parser,final boolean required ){
        this.removeLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG,"Remove login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    protected void setDatabaseIDOption(final AdminParser parser) {
        this.databaseIdOption = setShortLongOpt(parser, OPT_NAME_DATABASE_ID_SHORT,OPT_NAME_DATABASE_ID_LONG,"The id of the database.",true, NeededQuadState.eitheror);
    }

    protected void setDatabaseNameOption(final AdminParser parser, final NeededQuadState required){
        this.databaseNameOption = setShortLongOpt(parser, OPT_NAME_DBNAME_SHORT,OPT_NAME_DBNAME_LONG,"Name of the database",true, required); 
    }
    
    protected final void displayDisabledMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "disabled", parser);
    }

    protected final void displayEnabledMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "enabled", parser);
    }

    protected final void displayMovedMessage(final String id, final Integer ctxid, final String text, final AdminParser parser) {
        createMessageForStdout(id, ctxid, text, parser);
    }

    public void parseAndSetRemoveLoginMapping(AdminParser parser) {
        if (parser.getOptionValue(this.removeLoginMappingOption) != null) {
            this.remove_mappings = ((String) parser.getOptionValue(this.removeLoginMappingOption)).split(",");
        }
    }

    public void parseAndSetAddLoginMapping(AdminParser parser) {
        if (parser.getOptionValue(this.addLoginMappingOption) != null) {
            this.add_mappings = ((String) parser.getOptionValue(this.addLoginMappingOption)).split(",");
        }
    }

    public void changeMappingSetting(final OXContextInterface oxres, final Context ctx, final Credentials auth, final boolean change) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // check if wants to change login mappings, then first load current mappings from server
        if(add_mappings!=null || remove_mappings!=null){
            if (change) {
                Context server_ctx = oxres.getData(ctx, auth);
                ctx.setLoginMappings(server_ctx.getLoginMappings());
            } else {
                ctx.addLoginMapping(ctx.getIdAsString());
            }
            // add new mappings
            if (add_mappings != null) {
                ctx.addLoginMappings(Arrays.asList(add_mappings));
            }

            // remove mappings
            if(remove_mappings!=null){
                ctx.removeLoginMappings(Arrays.asList(remove_mappings));
            }
        }
    }

    /**
     * The disable, enable and move* command line tools are extended from this class so we can override
     * this method in order to create proper error messages.
     */
    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid, final AdminParser parser) {
        if (getClass().getName().matches("^.*\\.\\w*(?i)enable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be enabled: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)disable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be disabled: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wdatabase\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to database ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wfilestore\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to filestore ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else {
            super.printFirstPartOfErrorText(id, ctxid, parser);
        }
    }

    @Override
    protected void printErrors(final String id, final Integer ctxid, final Exception e, final AdminParser parser) {
        if (e instanceof NoSuchReasonException) {
            final NoSuchReasonException exc = (NoSuchReasonException) e;
            printServerException(id, ctxid, exc, parser);
            sysexit(1);
        } else if (e instanceof OXContextException) {
            final OXContextException exc = (OXContextException) e;
            printServerException(id, ctxid, exc, parser);
            sysexit(1);
        } else if (e instanceof NoSuchFilestoreException) {
            final NoSuchFilestoreException exc = (NoSuchFilestoreException) e;
            printServerException(id, ctxid, exc, parser);
            sysexit(1);
        } else {
            super.printErrors(id, ctxid, e, parser);
        }
    }

    protected void parseAndSetDatabaseID(final AdminParser parser, final Database db) {
        final String optionvalue = (String) parser.getOptionValue(this.databaseIdOption);
        if (null != optionvalue) {
            dbid = Integer.parseInt(optionvalue);
            db.setId(dbid);
        }
    }

    protected void parseAndSetDatabasename(final AdminParser parser, final Database db) {
        dbname = (String) parser.getOptionValue(this.databaseNameOption);
        if (null != dbname) {
            db.setName(dbname);
        }
    }
    
    protected void setFilestoreIdOption(final AdminParser parser) {
        this.targetFilestoreIDOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "Target filestore id", true, NeededQuadState.needed);
    }

    protected Filestore parseAndSetFilestoreId(final AdminParser parser) {
        filestoreid = Integer.parseInt((String) parser.getOptionValue(this.targetFilestoreIDOption));
        final Filestore fs = new Filestore(filestoreid);
        return fs;
    }

    /**
     * @return the filestoreid
     */
    public final Integer getFilestoreid() {
        return filestoreid;
    }
}
