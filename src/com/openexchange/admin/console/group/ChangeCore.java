package com.openexchange.admin.console.group;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends GroupAbstraction {
    
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // create options for this command line tool
        setGroupIdOption(parser, true);
        setGroupNameOption(parser, false);
        setGroupDisplayNameOption(parser, false);
        setAddMembersOption(parser, false);
        setRemoveMembersOption(parser, false);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXGroupInterface oxgrp = getGroupInterface();
            final Group grp = new Group();

            grp.setId(Integer.valueOf((String) parser.getOptionValue(this.IdOption)));

            
            if (parser.getOptionValue(this.addMemberOption) != null) {
                final User[] newMemberList = getMembers(parser, this.addMemberOption);
                if (newMemberList != null) {
                    oxgrp.addMember(ctx, grp, newMemberList, auth);
                }
            }
            if (parser.getOptionValue(this.removeMemberOption) != null) {
                final User[] removeMemberList = getMembers(parser, this.removeMemberOption);
                if (removeMemberList != null) {
                    oxgrp.removeMember(ctx, grp, removeMemberList, auth);
                }
            }

            if (parser.getOptionValue(this.nameOption) != null) {
                grp.setName((String) parser.getOptionValue(this.nameOption));
            }
            if (parser.getOptionValue(this.displayNameOption) != null) {
                grp.setDisplayname((String) parser.getOptionValue(this.displayNameOption));
            }

            maincall(parser, oxgrp, ctx, grp, auth);

            oxgrp.change(ctx, grp, auth);

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
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
        } catch (final NoSuchUserException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_USER);
        } catch (final NoSuchGroupException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_GROUP);
        } catch (final DuplicateExtensionException e) {
            printServerException(e);
            sysexit(1);
        }
    }

    private User[] getMembers(final AdminParser parser, final Option memberOption) {
        final String tmpmembers = (String) parser.getOptionValue(memberOption);
        final String[] split = tmpmembers.split(",");
        final User[] memberList = new User[split.length];
        for (int i = 0; i < split.length; i++) {
            memberList[i] = new User(Integer.parseInt(split[i]));
        }
        return memberList;
    }

    protected abstract void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final Group grp, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
