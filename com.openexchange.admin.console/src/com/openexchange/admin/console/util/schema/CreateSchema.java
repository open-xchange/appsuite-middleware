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

package com.openexchange.admin.console.util.schema;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.java.Strings;

/**
 * {@link CreateSchema}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class CreateSchema extends BasicCommandlineOptions {

    private static final char OPT_DB_ID_SHORT = 'i';
    private static final String OPT_DB_ID_LONG = "id";
    private static final String OPT_DB_ID_DESCRIPTION = "An optional database id";
    private static final String USAGE = "-A <masteradmin> -P <password> [-i <db_id>] [--csv]";
    private static final String DESCRIPTION = "Creates additional database schemata, which can be used for the creation of contexts.\n ---=== /!\\ ===--- NOTE: In order to use that schema for a new context, the returned schema name is supposed to be used for the \"schema\" option ---=== /!\\ ===--- ";

    private CLIOption optDBIdOption;
    private static final List<String> COLUMNS;
    static {
        COLUMNS = new ArrayList<>(2);
        COLUMNS.add("ID");
        COLUMNS.add("Scheme");
    }

    public static void main(String[] args) {
        new CreateSchema().execute(args);
    }

    public void execute(String[] args) {
        final AdminParser parser = new AdminParser("createschema");
        parser.setUsage(USAGE);
        parser.setCltDescription(DESCRIPTION);
        try {
            setOptions(parser);
            parser.ownparse(args);
            Credentials creds = credentialsparsing(parser);

            Integer id;
            {
                String id_str = (String) parser.getOptionValue(optDBIdOption);
                if (Strings.isEmpty(id_str)) {
                    id = null;
                } else {
                    id_str = id_str.trim();
                    try {
                        id = Integer.valueOf(id_str);
                    } catch (NumberFormatException e) {
                        printError("Invalid database id: " + id_str, parser);
                        sysexit(1);
                        return;
                    }
                }
            }

            OXUtilInterface oxUtil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
            Database db = oxUtil.createSchema(creds, id);

            if (null == parser.getOptionValue(this.csvOutputOption)) {
                System.out.printf("Created a new schema with name \"%s\" in database with id %s %n", db.getScheme(), db.getId());
                return;
            }

            // CSV output...
            ArrayList<ArrayList<String>> data = new ArrayList<>(1);
            ArrayList<String> row = new ArrayList<>(2);
            row.add(String.valueOf(db.getId()));
            row.add(db.getScheme());
            data.add(row);
            doCSVOutput(COLUMNS, data);
        } catch (CLIParseException e) {
            printError("Parsing command-line failed : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIIllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIUnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (MalformedURLException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (RemoteException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (NotBoundException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (StorageException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (InvalidDataException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_DATA);
        }

    }

    private void setOptions(final AdminParser parser) {
        this.adminUserOption = setShortLongOpt(parser, OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, "Master admin user", true, AdminParser.NeededQuadState.possibly);
        this.adminPassOption = setShortLongOpt(parser, OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, "Master admin password", true, AdminParser.NeededQuadState.possibly);
        this.optDBIdOption = setShortLongOpt(parser, OPT_DB_ID_SHORT, OPT_DB_ID_LONG, OPT_DB_ID_DESCRIPTION, true, AdminParser.NeededQuadState.notneeded);
        setCSVOutputOption(parser);
    }

}
