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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();

    private OXContextInterface csv_oxctx;

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("createcontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Create(args);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        parser.setExtendedOptions();
        ctxabs.setAddMappingOption(parser, false);
        ctxabs.setDestinationStoreIdOption(parser, false);
        ctxabs.setDestinationDatabaseIdOption(parser, false);

        setConfigOption(parser);
        setRemoveConfigOption(parser);

        setAddAccessRightCombinationNameOption(parser);
        setModuleAccessOptions(parser);

        setSchemaOptions(parser);
    }

    @Override
    protected Context maincall(final AdminParser parser, final Context ctx, final User usr, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException, NoSuchContextException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);
        ctxabs.parseAndSetDestinationStoreId(parser);
        ctxabs.parseAndSetDestinationDatabaseId(parser);

        ctxabs.changeMappingSetting(oxctx, ctx, auth, false);
        ctx.setFilestoreId(ctxabs.getStoreid());
        final Integer db = ctxabs.getDatabaseid();
        if (null != db) {
            ctx.setWriteDatabase(new Database(db));
        }

        Context createdctx = null;

        // needed for comparison
        UserModuleAccess NO_RIGHTS_ACCESS = new UserModuleAccess();
        NO_RIGHTS_ACCESS.disableAll();

        // now check which create method we must call,
        // this depends on the access rights supplied by the client
        UserModuleAccess parsed_access = new UserModuleAccess();
        parsed_access.disableAll();

        // parse access options
        setModuleAccessOptions(parser, parsed_access);

        String accessCombinationName = parseAndSetAccessCombinationName(parser);

        if (!parsed_access.equals(NO_RIGHTS_ACCESS) && null != accessCombinationName) {
            // BOTH WAYS TO SPECIFY ACCESS RIGHTS ARE INVALID!
            throw new InvalidDataException(ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR);
        }

        if (null != accessCombinationName) {
            // Client supplied access combination name. create context with this name
            createdctx = oxctx.create(ctx, usr, accessCombinationName, auth, schemaSelectStrategy);
        } else if (!parsed_access.equals(NO_RIGHTS_ACCESS)) {
            // Client supplied access attributes
            createdctx = oxctx.create(ctx, usr, parsed_access, auth, schemaSelectStrategy);
        } else {
            createdctx = oxctx.create(ctx, usr, auth, schemaSelectStrategy);
        }

        // TODO: We have to add a cleanup here. If creation of mappings fails the context should be deleted
        return createdctx;
    }

    @Override
    protected void lookupRMI() throws MalformedURLException, RemoteException, NotBoundException {
        this.csv_oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
    }

    @Override
    protected Context simpleMainCall(Context ctx, User usr, String accessCombiName, Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return this.csv_oxctx.create(ctx, usr, accessCombiName, auth, schemaSelectStrategy);
    }

    @Override
    protected Context simpleMainCall(Context ctx, User usr, UserModuleAccess access, Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return this.csv_oxctx.create(ctx, usr, access, auth, schemaSelectStrategy);
    }

    @Override
    protected Context simpleMainCall(Context ctx, User usr, Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return this.csv_oxctx.create(ctx, usr, auth, schemaSelectStrategy);
    }

}
