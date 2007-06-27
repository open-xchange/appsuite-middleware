
package com.openexchange.admin.console.group;

import java.net.MalformedURLException;
import java.rmi.Naming;
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
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends GroupAbstraction {

    public static void main(final String[] args) {
        new Change(args);
    }

    private Change() {

    }

    public Change(final String[] args2) {

        final AdminParser parser = new AdminParser("change");
        setOptions(parser);

        try {
            parser.ownparse(args2);
            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            final OXGroupInterface oxgrp = (OXGroupInterface) Naming.lookup(RMI_HOSTNAME +  OXGroupInterface.RMI_NAME);
            final Group grp = new Group();

            grp.setId(Integer.valueOf((String) parser.getOptionValue(this.IdOption)));

            int[] newMemberList = null;
            int[] removeMemberList = null;
            if (parser.getOptionValue(this.addMemberOption) != null) {
                final String tmpmembers = (String) parser.getOptionValue(this.addMemberOption);
                final ArrayList<Integer> newmembers = new ArrayList<Integer>();
                for (final String member : tmpmembers.split(",")) {
                    newmembers.add(Integer.parseInt(member));
                }
                if (newmembers.size() > 0) {
                    newMemberList = new int[newmembers.size()];
                    for (int i = 0; i < newmembers.size(); i++) {
                        newMemberList[i] = newmembers.get(i);
                    }
                }
            }
            if (parser.getOptionValue(this.removeMemberOption) != null) {
                final String tmpmembers = (String) parser.getOptionValue(this.removeMemberOption);
                final ArrayList<Integer> removemembers = new ArrayList<Integer>();
                for (final String member : tmpmembers.split(",")) {
                    removemembers.add(Integer.parseInt(member));
                }
                if (removemembers.size() > 0) {
                    removeMemberList = new int[removemembers.size()];
                    for (int i = 0; i < removemembers.size(); i++) {
                        removeMemberList[i] = removemembers.get(i);
                    }
                }
            }
            if (newMemberList != null) {
                oxgrp.addMember(ctx, grp, newMemberList, auth);
            }
            if (removeMemberList != null) {
                oxgrp.removeMember(ctx, grp, removeMemberList, auth);
            }

            if (parser.getOptionValue(this.nameOption) != null) {
                grp.setName((String) parser.getOptionValue(this.nameOption));
            }
            if (parser.getOptionValue(this.displayNameOption) != null) {
                grp.setName((String) parser.getOptionValue(this.displayNameOption));
            }

            oxgrp.change(ctx, grp, auth);

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final MalformedURLException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final StorageException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidDataException e) {
            printServerResponse(e);
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
            printServerResponse(e);
            sysexit(1);
        } catch (final NoSuchUserException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_USER);
        } catch (final NoSuchGroupException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_GROUP);
        }
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // create options for this command line tool
        setGroupIdOption(parser, true);
        setGroupNameOption(parser, false);
        setGroupDisplayNameOption(parser, false);
        setAddMembersOption(parser, false);
        setRemoveMembersOption(parser, false);

    }
}
