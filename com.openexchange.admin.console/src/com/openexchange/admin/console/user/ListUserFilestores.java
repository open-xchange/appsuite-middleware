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

package com.openexchange.admin.console.user;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link ListUserFilestores} lists all user filestores and their corresponding users
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ListUserFilestores extends BasicCommandlineOptions {

    private final static String MASTER_ONLY_LONG = "master_only";
    private final static char MASTER_ONLY_SHORT = 'm';
    private final static String FILESTORE_ID_LONG = "filestore_id";
    private final static char FILESTORE_ID_SHORT = 'f';
    private CLIOption filestore_id = null;
    private CLIOption master_only = null;

    public static void main(String[] args) {
        new ListUserFilestores().execute(args);
    }

    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("list user filestores");
        try {
            setOptions(parser);
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);
            final Credentials auth = credentialsparsing(parser);
            final OXUserInterface oxusr = (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);

            final String fid_str = (String) parser.getOptionValue(filestore_id, null, false);
            final Integer fid = fid_str != null ? Integer.valueOf(fid_str) : null;
            User[] users = oxusr.listUsersWithOwnFilestore(ctx, auth, fid);

            if (null == users || users.length == 0) {
                printNothingFound(fid);
                parser.printUsage();
                sysexit(0);
            }
            users = oxusr.getData(ctx, users, auth);
            final boolean masteronly = (boolean) parser.getOptionValue(master_only, false, false);
            printUserFilestores(users, masteronly);

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
        } catch (InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (StorageException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (NoSuchContextException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (InvalidDataException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (DatabaseUpdateException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (NoSuchUserException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_NO_SUCH_USER);
        }
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.needed);
        this.master_only = setShortLongOpt(parser, MASTER_ONLY_SHORT, MASTER_ONLY_LONG, "Prints only the master user", false, NeededQuadState.notneeded);
        this.filestore_id = setShortLongOpt(parser, FILESTORE_ID_SHORT, FILESTORE_ID_LONG, "id", "The id of the filestore. Lists users for this filestore.", false);
    }

    private void printUserFilestores(User[] users, boolean master_only) throws InvalidDataException {
        for(User user: users){
            if (user.getFilestoreId().intValue() == 0) {
                continue;
            }
            
            if (user.getFilestoreOwner().intValue() == 0)
            {
                final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
                data.add(makeStandardData(user, true));
                if (!master_only) {

                    for (User tmp : users) {
                        if (tmp.getId().intValue() == user.getId().intValue()) {
                            continue;
                        }
                        if (tmp.getFilestoreOwner().intValue() == user.getId().intValue())
                        {
                            data.add(makeStandardData(tmp, false));
                        }
                    }
                }
                System.out.println("------------------------------------------------------------------------------------");
                System.out.println("Filestore_id: " + user.getFilestoreId());
                System.out.println("Filestore_name: " + user.getFilestore_name());
                System.out.println();
                doOutput(new String[] { "l", "r", "l", "l", "l", "l", "l" },
                    new String[] { "Master", "Id", "Name", "Displayname", "Email", "qmax", "qused" }, data);   

            }
        }
        System.out.println("------------------------------------------------------------------------------------");
    }

    private void printNothingFound(Integer id) {
        if (id == null) {
            System.out.println("There are no users with own filestores.");
        } else {
            System.out.println("There is no user filestore with id " + id);
        }
    }

    private ArrayList<String> makeStandardData(final User user, boolean master) {
        final ArrayList<String> res_data = new ArrayList<String>();

        if (master) {
            res_data.add("TRUE");
        } else {
            res_data.add("");
        }
        res_data.add(String.valueOf(user.getId()));// id

        {
            final String name = user.getName();
            if (name != null && name.trim().length() > 0) {
                res_data.add(name);// name
            } else {
                res_data.add(null);// name
            }
        }

        {
            final String displayname = user.getDisplay_name();
            if (displayname != null && displayname.trim().length() > 0) {
                res_data.add(displayname);// displayname
            } else {
                res_data.add(null);// displayname
            }
        }

        {
            final String email = user.getPrimaryEmail();
            if (email != null && email.trim().length() > 0) {
                res_data.add(email);// email
            } else {
                res_data.add(null);// email
            }
        }

        {
            final Long qmax = user.getMaxQuota();
            if (null != qmax) {
                res_data.add(qmax.toString());// qmax
            } else {
                res_data.add(null);// qmax
            }
        }

        {
            final Long qused = user.getUsedQuota();
            if (null != qused) {
                res_data.add(qused.toString());// qused
            } else {
                res_data.add(null);// qused
            }
        }

        return res_data;
    }

}
