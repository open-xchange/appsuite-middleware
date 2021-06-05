/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.console.context;

import static com.openexchange.java.Autoboxing.i;
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

    /**
     * Initializes a new {@link Change}.
     */
    public Create() {
        super();
    }

    public static void main(final String args[]) {
        new Create().execute(args);
    }

    /**
     * Executes the command
     *
     * @param args the command line arguments
     */
    public void execute(String[] args) {
        commonfunctions(new AdminParser("createcontext"), args);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        parser.setExtendedOptions();
        ctxabs.setAddMappingOption(parser, false);
        ctxabs.setDestinationStoreIdOption(parser, false);
        ctxabs.setDestinationDatabaseIdOption(parser, false);

        setAddAccessRightCombinationNameOption(parser);
        setModuleAccessOptions(parser);

        setSchemaOptions(parser);
    }

    @Override
    protected Context maincall(final AdminParser parser, final Context ctx, final User usr, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException, NoSuchContextException {
        // get rmi ref
        OXContextInterface oxctx = OXContextInterface.class.cast(Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME));

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);
        ctxabs.parseAndSetDestinationStoreId(parser);
        ctxabs.parseAndSetDestinationDatabaseId(parser);

        ctxabs.changeMappingSetting(oxctx, ctx, auth, false);
        ctx.setFilestoreId(ctxabs.getStoreid());
        Integer db = ctxabs.getDatabaseid();
        if (null != db) {
            ctx.setWriteDatabase(new Database(i(db)));
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
