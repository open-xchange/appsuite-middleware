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

package com.openexchange.admin.console.user;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
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
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class ListUserFilestores extends BasicCommandlineOptions {

    private final static String MASTER_ONLY_LONG = "master_only";
    private final static char MASTER_ONLY_SHORT = 'm';
    private final static String FILESTORE_ID_LONG = "filestore_id";
    private final static char FILESTORE_ID_SHORT = 'f';
    private CLIOption filestore_id = null;
    private CLIOption master_only = null;

    /**
     * Entry point
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        new ListUserFilestores().execute(args);
    }

    /**
     * Executes the command
     *
     * @param args command-line arguments
     */
    private void execute(String[] args) {
        AdminParser parser = new AdminParser("listuserfilestores");
        try {
            setOptions(parser);
            setLengthOption(parser);
            setOffsetOption(parser);
            parser.ownparse(args);

            Context ctx = contextparsing(parser);
            Credentials auth = credentialsparsing(parser);
            OXUserInterface oxusr = (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);

            String fid_str = (String) parser.getOptionValue(filestore_id, null, false);
            Integer fid = fid_str != null ? Integer.valueOf(fid_str) : null;

            Integer length = null;
            if (parser.hasOption(this.lengthOption)) {
                length = Integer.valueOf((String) parser.getOptionValue(this.lengthOption));
            }

            Integer offset = null;
            if (parser.hasOption(this.offsetOption)) {
                offset = Integer.valueOf((String) parser.getOptionValue(this.offsetOption));
            }

            User[] users = oxusr.listUsersWithOwnFilestore(ctx, auth, fid, length, offset);

            if (null == users || users.length == 0) {
                printNothingFound(fid);
                sysexit(0);
            }
            users = oxusr.getData(ctx, users, auth);
            boolean masteronly = ((Boolean) parser.getOptionValue(master_only, Boolean.FALSE, false)).booleanValue();
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

    /**
     * Prints user file storages
     *
     * @param users The users
     * @param masterOnly flag to only print master fs
     * @throws InvalidDataException if invalid data is passed
     */
    private void printUserFilestores(User[] users, boolean masterOnly) throws InvalidDataException {
        for (User user : users) {
            if (user.getFilestoreId().intValue() == 0) {
                continue;
            }

            if (user.getFilestoreOwner().intValue() != 0) {
                continue;
            }
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            data.add(makeStandardData(user, true));
            if (!masterOnly) {
                processAll(users, user, data);
            }
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Filestore_id: " + user.getFilestoreId());
            System.out.println("Filestore_name: " + user.getFilestore_name());
            System.out.println();
            doOutput(new String[] { "l", "r", "l", "l", "l", "l", "l" }, new String[] { "Master", "Id", "Name", "Displayname", "Email", "qmax", "qused" }, data);
        }
        System.out.println("------------------------------------------------------------------------------------");
    }

    /**
     * Processes all users
     *
     * @param users The users
     * @param user The master user
     * @param data The data
     */
    private void processAll(User[] users, User user, final ArrayList<ArrayList<String>> data) {
        int userId = user.getId().intValue();
        for (User tmp : users) {
            if (tmp.getId().intValue() != userId && tmp.getFilestoreOwner() != null && tmp.getFilestoreOwner().intValue() == userId) {
                data.add(makeStandardData(tmp, false));
            }
        }
    }

    private void printNothingFound(Integer id) {
        if (id == null) {
            System.out.println("There are no users with own filestores.");
        } else {
            System.out.println("There is no user filestore with id " + id);
        }
    }

    private ArrayList<String> makeStandardData(final User user, boolean master) {
        ArrayList<String> data = new ArrayList<String>(7);
        data.add(master ? "TRUE" : "");
        data.add(String.valueOf(user.getId()));
        addData(data, user.getName());
        addData(data, user.getDisplay_name());
        addData(data, user.getPrimaryEmail());
        addData(data, user.getMaxQuota());
        addData(data, user.getUsedQuota());
        return data;
    }

    private void addData(List<String> data, String value) {
        if (value != null && value.trim().length() > 0) {
            data.add(value);
        } else {
            data.add(null);
        }
    }

    private void addData(List<String> data, Long value) {
        if (null != value) {
            data.add(value.toString());
        } else {
            data.add(null);
        }
    }
}
