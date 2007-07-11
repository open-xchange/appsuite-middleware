package com.openexchange.admin.console.group;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends GroupAbstraction {
    
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // we need csv output , so we add this option
        setCSVOutputOption(parser);
        // create options for this command line tool
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing. This applies to name.", true, false);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXGroupInterface oxgrp = getGroupInterface();

            String pattern = (String) parser.getOptionValue(this.searchOption);

            if (null == pattern) {
                pattern = "*";
            }

            final Group[] allgrps = oxgrp.list(ctx, pattern, auth);

            final ArrayList<Group> grplist = new ArrayList<Group>();

            maincall(parser, oxgrp, ctx, grplist, allgrps, auth);
            
            if (parser.getOptionValue(this.csvOutputOption) != null) {
                // DO csv output if needed
                precsvinfos(grplist);
            } else {
                sysoutOutput(grplist);
            }

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException e) {
            printInvalidInputMsg("The Option for the id of the group contains no parseable integer number");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e);
            sysexit(1);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final NoSuchGroupException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_GROUP);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final ArrayList<Group> grplist, final Group[] allgrps, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    private void sysoutOutput(final ArrayList<Group> grouplist) {
        for (final Group group : grouplist) {
            // TODO FIX THE HUMAN READABLE OUTPUT OF THIS COMMANDLINE TOOL
            System.out.println(group);
            System.out.println("  Members:");
            final Integer[] members = group.getMembers();
            if (members != null) {
                for (final int id : members) {
                    System.out.println("   " + id);
                }
            }
            printExtensionsError(group);
        }
    }

    private void precsvinfos(final ArrayList<Group> grplist) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("displayname");
        columns.add("members");
        extendscvscolumns(columns);
        
        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final Group my_grp : grplist) {
            data.add(makeDataForCsv(my_grp, my_grp.getMembers()));
            printExtensionsError(my_grp);
        }
        doCSVOutput(columns, data);
    }

    protected abstract void extendscvscolumns(final ArrayList<String> columns);

    /**
     * Generate data which can be processed by the csv output method.
     * 
     * @param group
     * @param members
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public ArrayList<String> makeDataForCsv(final Group group, final Integer[] members) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final ArrayList<String> grp_data = new ArrayList<String>();
    
        grp_data.add(String.valueOf(group.getId())); // id
    
        final String name = group.getName();
        if (name != null && name.trim().length() > 0) {
            grp_data.add(name);
        } else {
            grp_data.add(null); // name
        }
        final String displayname = group.getDisplayname();
        if (displayname != null && displayname.trim().length() > 0) {
            grp_data.add(displayname);
        } else {
            grp_data.add(null); // displayname
        }
        final StringBuilder sb = new StringBuilder();
        if (null != members) {
            for (final int id : members) {
                sb.append(id);
                sb.append(",");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            grp_data.add(sb.toString()); // members
        } else {
            grp_data.add(null); // members
        }
    
        extendmakeCSVData(group, grp_data);
        return grp_data;
    }

    protected abstract void extendmakeCSVData(Group group, ArrayList<String> grp_data);
}
