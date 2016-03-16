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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
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
public class ContextHostingAbstraction extends ObjectNamingAbstraction {

    private String[] remove_mappings = null;
    private String[] add_mappings = null;
    private Integer storeid = null;
    private Integer databaseid = null;

    private CLIOption addLoginMappingOption = null;
    private CLIOption removeLoginMappingOption = null;
    private CLIOption destinationStoreIdMappingOption = null;
    private CLIOption destinationDatabaseIdMappingOption = null;

    @Override
    protected String getObjectName() {
        return "context";
    }

    public void parseAndSetRemoveLoginMapping(final AdminParser parser) {
        if (parser.getOptionValue(this.removeLoginMappingOption) != null) {
            this.remove_mappings = ((String) parser.getOptionValue(this.removeLoginMappingOption)).split(",");
        }
    }

    public void parseAndSetAddLoginMapping(final AdminParser parser) {
        if (parser.getOptionValue(this.addLoginMappingOption) != null) {
            this.add_mappings = ((String) parser.getOptionValue(this.addLoginMappingOption)).split(",");
        }
    }

    public void parseAndSetDestinationStoreId(final AdminParser parser) {
        if (parser.getOptionValue(this.destinationStoreIdMappingOption) != null) {
            this.storeid = Integer.parseInt((String)parser.getOptionValue(this.destinationStoreIdMappingOption));
        }
    }

    public void parseAndSetDestinationDatabaseId(final AdminParser parser) {
        if (parser.getOptionValue(this.destinationDatabaseIdMappingOption) != null) {
            this.databaseid = Integer.parseInt((String)parser.getOptionValue(this.destinationDatabaseIdMappingOption));
        }
    }

    public void setDestinationStoreIdOption(final AdminParser parser,final boolean required) {
        this.destinationStoreIdMappingOption = setShortLongOpt(parser, ContextAbstraction.OPT_CONTEXT_DESTINATION_STORE_ID_SHORT, ContextAbstraction.OPT_CONTEXT_DESTINATION_STORE_ID_LONG,"Create context in the given filestore",true, convertBooleantoTriState(required));
    }

    public void setDestinationDatabaseIdOption(final AdminParser parser,final boolean required) {
        this.destinationDatabaseIdMappingOption = setShortLongOpt(parser, ContextAbstraction.OPT_CONTEXT_DESTINATION_DATABASE_ID_SHORT, ContextAbstraction.OPT_CONTEXT_DESTINATION_DATABASE_ID_LONG,"Create context in the given database",true, convertBooleantoTriState(required));
    }

    public void setAddMappingOption(final AdminParser parser,final boolean required) {
        this.addLoginMappingOption = setShortLongOpt(parser, ContextAbstraction.OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT, ContextAbstraction.OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG,"Add login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }

    public void setRemoveMappingOption(final AdminParser parser,final boolean required) {
        this.removeLoginMappingOption = setShortLongOpt(parser, ContextAbstraction.OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT, ContextAbstraction.OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG,"Remove login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }


    public void changeMappingSetting(final OXContextInterface oxres, final Context ctx, final Credentials auth, final boolean change) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // check if wants to change login mappings, then first load current mappings from server
        if(add_mappings!=null || remove_mappings!=null){
            if (change) {
                final Context server_ctx = oxres.getData(ctx, auth);
                ctx.setLoginMappings(server_ctx.getLoginMappings());
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
     * @return the storeid
     */
    public final Integer getStoreid() {
        return storeid;
    }


    /**
     * @return the databaseid
     */
    public final Integer getDatabaseid() {
        return databaseid;
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

}
