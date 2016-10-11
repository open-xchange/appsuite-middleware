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

    private final char OPT_DB_ID_SHORT = 'i';
    private final String OPT_DB_ID_LONG = "id";
    private final String OPT_DB_ID_DESCRIPTION = "An optional database id";
    private final String USAGE = "-A <masteradmin> -P <password> [-i <db_id>] [--csv]";
    private final String DESCRIPTION = "Creates additional database schemata which can be used during the creation of contexts.";

    private CLIOption optDBIdOption;
    private static final List<String> COLUMNS;
    static {
        COLUMNS = new ArrayList<>(2);
        COLUMNS.add("ID");
        COLUMNS.add("Scheme");
    }



 public static void main(String [] args){
     new CreateSchema().execute(args);
 }


 public void execute(String [] args) {
     final AdminParser parser = new AdminParser("createschema");
     parser.setUsage(USAGE);
     parser.setCltDescription(DESCRIPTION);
     try {
        setOptions(parser);
        parser.ownparse(args);
        Credentials creds = credentialsparsing(parser);
        String id_str = (String) parser.getOptionValue(optDBIdOption);
        Integer id = Strings.isEmpty(id_str) ? null : Integer.valueOf(id_str);
        OXUtilInterface oxUtil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
        Database db = oxUtil.createSchema(creds, id);


        if(null!= parser.getOptionValue(this.csvOutputOption)){
            ArrayList<ArrayList<String>> data = new ArrayList<>(1);
            ArrayList<String> row = new ArrayList<>(2);
            row.add(String.valueOf(db.getId()));
            row.add(db.getScheme());
            data.add(row);
            doCSVOutput(COLUMNS, data);
        } else {
            System.out.printf("Created a new schema with name \"%s\" in database with id %s \n", db.getScheme(), db.getId());
        }
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
     this.adminUserOption = setShortLongOpt(parser, OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, "Master admin user", true, AdminParser.NeededQuadState.needed);
     this.adminPassOption = setShortLongOpt(parser, OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, "Master admin password", true, AdminParser.NeededQuadState.needed);
     this.optDBIdOption = setShortLongOpt(parser, OPT_DB_ID_SHORT, OPT_DB_ID_LONG, OPT_DB_ID_DESCRIPTION, true, AdminParser.NeededQuadState.notneeded);
     setCSVOutputOption(parser);
 }

}
