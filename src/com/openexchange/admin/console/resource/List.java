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
package com.openexchange.admin.console.resource;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ResourceAbstraction {

    public static void main(final String[] args) {
        new List(args);
    }

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("list");
        setDefaultCommandLineOptions(parser);
        // we need csv output , so we add this option
        setCSVOutputOption(parser);
        setSearchPatternOption(parser);

        try {
            parser.ownparse(args2);

            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            String pattern = (String) parser.getOptionValue(this.searchOption);
            
            if (null == pattern) {
                pattern = "*";
            }

            final OXResourceInterface oxres = (OXResourceInterface) Naming.lookup(OXResourceInterface.RMI_NAME);

            final Resource[] allres = oxres.list(ctx, pattern, auth);

            // TODO FIX THE NORMAL OUTPUT OF THIS COMMANDLINE TOOL
            final ArrayList<Resource> resourceList = new ArrayList<Resource>();
            for (final Resource da_res : allres) {
                resourceList.add(oxres.get(ctx, da_res, auth));
            }

            if (parser.getOptionValue(this.csvOutputOption) != null) {
                // do CSV output
                precvsinfos(resourceList);
            } else {
                sysoutOutput(resourceList);
            }
            System.exit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            System.exit(1);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            System.exit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (final DatabaseUpdateException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        }

    }

    private void precvsinfos(final ArrayList<Resource> resourceList) {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("email");

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final Resource my_res : resourceList) {
            data.add(makeCsvData(my_res));
        }
        doCSVOutput(columns, data);
    }

    private final void sysoutOutput(final ArrayList<Resource> resources) {
        for (final Resource resource : resources) {
            System.out.println(resource.toString());
            printExtensionsError(resource);
        }
    }

    private ArrayList<String> makeCsvData(final Resource my_res) {

        final ArrayList<String> res_data = new ArrayList<String>();

        res_data.add(String.valueOf(my_res.getId())); // id

        if (my_res.getName() != null && my_res.getName().trim().length() > 0) {
            res_data.add(my_res.getName()); // name
        } else {
            res_data.add(null); // name
        }

        if (my_res.getDisplayname() != null && my_res.getDisplayname().trim().length() > 0) {
            res_data.add(my_res.getDisplayname()); // displayname
        } else {
            res_data.add(null); // displayname
        }

        if (my_res.getEmail() != null && my_res.getEmail().trim().length() > 0) {
            res_data.add(my_res.getEmail()); // email
        } else {
            res_data.add(null); // email
        }

        return res_data;
    }

}
