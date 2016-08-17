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

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import au.com.bytecode.opencsv.CSVReader;
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

public abstract class CreateCore extends ContextAbstraction {

    protected void setOptions(final AdminParser parser) {
        setCsvImport(parser);
        setDefaultCommandLineOptions(parser);
        setContextNameOption(parser, NeededQuadState.notneeded);
        setMandatoryOptions(parser);

        setLanguageOption(parser);
        setTimezoneOption(parser);

        setContextQuotaOption(parser, true);

        setFurtherOptions(parser);

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
                // fill user obj with mandatory values from console
                final String tz = (String) parser.getOptionValue(this.timezoneOption);
                if (null != tz) {
                    usr.setTimezone(tz);
                }

                final String languageoptionvalue = (String) parser.getOptionValue(this.languageOption);
                if (languageoptionvalue != null) {
                    usr.setLanguage(languageoptionvalue);
                }

                parseAndSetContextQuota(parser, ctx);

                parseAndSetExtensions(parser, ctx, auth);

                // Dynamic Options
                applyDynamicOptionsToContext(parser, ctx);
            } catch (final RuntimeException e) {
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
        } catch (final Exception e) {
            printErrors((null != ctxid) ? String.valueOf(ctxid) : null, null, e, parser);
        }

        try {
            displayCreatedMessage((null != ctxid) ? String.valueOf(ctxid) : null, null, parser);
        } catch (final RuntimeException e) {
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
                    } catch (final OXConsolePluginException e1) {
                        System.err.println("Failed to create context: Error while processing extension options: " + e1.getClass().getSimpleName() + ": " + e1.getMessage());
                    } catch (final StorageException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (final RemoteException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (final InvalidCredentialsException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (final InvalidDataException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (final ContextExistsException e) {
                        System.err.println("Failed to create context " + getContextIdOrLine(context, lineNumber) + ": " + e);
                    } catch (final ParseException e) {
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
            } catch (final Exception e) {
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
                retval = SchemaSelectStrategy.inMemory();
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
