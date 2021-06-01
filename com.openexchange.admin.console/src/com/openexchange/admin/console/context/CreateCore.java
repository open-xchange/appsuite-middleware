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

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import au.com.bytecode.opencsv.CSVReader;

public abstract class CreateCore extends ContextAbstraction {

    protected void setOptions(final AdminParser parser) {
        setCsvImport(parser);
        setDefaultCommandLineOptions(parser);
        setContextNameOption(parser, NeededQuadState.notneeded);
        setMandatoryOptions(parser);

        setContextQuotaOption(parser, true);
        seGABModeOption(parser);

        setFurtherOptions(parser);
        setExtendedOptions(parser);
        setPrimaryAccountOption(parser);
        setOptionalOptions(parser);

        parser.allowDynamicOptions();
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);
        setExtensionOptions(parser, ContextConsoleCreateInterface.class);

        try {
            Context ctx = null;
            Credentials auth = null;
            // create user obj
            User usr = null;
            try {
                parser.ownparse(args);

                ctx = contextparsing(parser);

                parseAndSetContextName(parser, ctx);

                auth = credentialsparsing(parser);

                usr = new User();

                // fill user obj with mandatory values from console
                parseAndSetMandatoryOptionsinUser(parser, usr);
                parseAndSetPrimaryAccountName(parser, usr);
                
                // fill user obj with optional values from console
                parseAndSetOptionalOptionsinUser(parser, usr);

                applyExtendedOptionsToUser(parser, usr);

                applyDynamicOptionsToUser(parser, usr);
                
                applyDriveFolderModeOption(parser, usr);
                
                // Fill ctx obj with additional values from console
                
                parseAndSetContextQuota(parser, ctx);
                parseAndSetGABMode(parser, ctx);

                parseAndSetExtensions(parser, ctx, auth);

                // Dynamic Options
                applyDynamicOptionsToContext(parser, ctx);
            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }

            SchemaSelectStrategy schemaSelectStrategy = parseCheckAndGetSchemaSelectStrategy(parser);

            final String csvFile = (String) parser.getOptionValue(parser.getCsvImportOption());

            if (null != csvFile) {
                csvparsing(csvFile, auth);
            } else {
                ctxid = maincall(parser, ctx, usr, auth, schemaSelectStrategy).getId();
            }
        } catch (Exception e) {
            printErrors((null != ctxid) ? String.valueOf(ctxid) : null, null, e, parser);
        }

        try {
            displayCreatedMessage((null != ctxid) ? String.valueOf(ctxid) : null, null, parser);
        } catch (RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
        sysexit(0);
    }

    protected void csvparsing(final String filename, final Credentials auth) throws NotBoundException, IOException, InvalidDataException, StorageException, InvalidCredentialsException {
        // First check if we can login with the given credentials. Otherwise there's no need to continue
        {
            OXLoginInterface oxlgn = (OXLoginInterface) Naming.lookup(RMI_HOSTNAME + OXLoginInterface.RMI_NAME);
            oxlgn.login(auth);
        }

        CSVReader reader = new CSVReader(new FileReader(filename), ',', '"');
        try {
            int[] idarray = csvParsingCommon(reader);
            lookupRMI();

            int lineNumber = 2;
            for (String[] nextLine; (nextLine = reader.readNext()) != null;) {

                if (nextLine.length == 0 || nextLine.length == 1 && nextLine[0].isEmpty()) {
                    continue; // skip empty lines (Bug #45259)
                }
                // nextLine[] is an array of values from the line
                try {
                    Context context = getContext(nextLine, idarray);
                    SchemaSelectStrategy schemaSelectStrategy = getSchemaSelectStrategy(nextLine, idarray);
                    try {
                        Context createdCtx;
                        {
                            applyExtensionValuesFromCSV(nextLine, idarray, context);
                            User adminuser = getUser(nextLine, idarray);
                            int i = idarray[AccessCombinations.ACCESS_COMBI_NAME.getIndex()];
                            if (i >= 0) {
                                // create call
                                createdCtx = simpleMainCall(context, adminuser, nextLine[i], auth, schemaSelectStrategy);
                            } else {
                                final UserModuleAccess moduleacess = getUserModuleAccess(nextLine, idarray);
                                if (!NO_RIGHTS_ACCESS.equals(moduleacess)) {
                                    // with module access
                                    createdCtx = simpleMainCall(context, adminuser, moduleacess, auth, schemaSelectStrategy);
                                } else {
                                    // without module access
                                    createdCtx = simpleMainCall(context, adminuser, auth, schemaSelectStrategy);
                                }

                            }
                        }
                        System.out.println("Context " + createdCtx.getId() + " successfully created");
                    } catch (OXConsolePluginException e1) {
                        System.err.println("Failed to create context: Error while processing extension options: " + e1.getClass().getSimpleName() + ": " + e1.getMessage());
                    } catch (StorageException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (RemoteException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (InvalidCredentialsException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (InvalidDataException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (ContextExistsException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (ParseException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    }
                } catch (ParseException e2) {
                    System.err.println("Failed to create context in line " + lineNumber + ": " + e2);
                }

                // Increment line number
                lineNumber++;
            }
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    protected abstract void lookupRMI() throws MalformedURLException, RemoteException, NotBoundException;

    @Override
    protected void prepareConstantsMap() {
        super.prepareConstantsMap();
        for (final ContextConstants value : ContextConstants.values()) {
            this.constantsMap.put(value.getString(), value);
        }
        extensionConstantProcessing(this.constantsMap);
    }

    @Override
    public void checkRequired(final int[] idarray) throws InvalidDataException {
        super.checkRequired(idarray);
        for (final ContextConstants value : ContextConstants.values()) {
            if (value.isRequired()) {
                if (-1 == idarray[value.getIndex()]) {
                    throw new InvalidDataException("The required column \"" + value.getString() + "\" is missing");
                }
            }
        }

    }

    private String getContextIdOrLine(final Context context, final int linenumber) {
        final Integer id = context.getId();
        return null == id ? "in line " + linenumber : id.toString();
    }

    protected SchemaSelectStrategy parseCheckAndGetSchemaSelectStrategy(AdminParser parser) throws InvalidDataException {
        parseAndSetSchemaOptions(parser);
        if (schema != null && schemaStrategy != null) {
            throw new InvalidDataException(SCHEMA_NAME_AND_SCHEMA_STRATEGY_ERROR);
        }

        SchemaSelectStrategy retval;
        if (schema != null) {
            retval = SchemaSelectStrategy.schema(schema);
        } else if (schemaStrategy != null) {
            if (schemaStrategy.equals("automatic")) {
                retval = SchemaSelectStrategy.automatic();
            } else if (schemaStrategy.equals("in-memory")) {
                // Fall-back to "automatic"
                retval = SchemaSelectStrategy.automatic();
            } else {
                throw new InvalidDataException(SCHEMA_NAME_ERROR);
            }
        } else {
            retval = SchemaSelectStrategy.getDefault(); // default
        }

        return retval;
    }

    protected abstract Context simpleMainCall(final Context ctx, final User usr, final String accessCombiName, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException;

    protected abstract Context simpleMainCall(final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException;

    protected abstract Context simpleMainCall(final Context ctx, final User usr, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException;

    protected abstract Context maincall(final AdminParser parser, Context ctx, User usr, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException, NoSuchContextException;

    protected abstract void setFurtherOptions(final AdminParser parser);

}
