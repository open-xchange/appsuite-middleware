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

package com.openexchange.admin.user.copy.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.user.copy.rmi.OXUserCopyInterface;

public class UserCopy extends ObjectNamingAbstraction {

    protected static final char OPT_NAME_FROM_CONTEXT_SHORT='c';
    protected static final String OPT_NAME_FROM_CONTEXT_LONG="srccontextid";
    protected static final String OPT_NAME_FROM_CONTEXT_DESCRIPTION="The id of the source context";
    protected static final char OPT_NAME_TO_CONTEXT_SHORT='d';
    protected static final String OPT_NAME_TO_CONTEXT_LONG="destcontextid";
    protected static final String OPT_NAME_TO_CONTEXT_DESCRIPTION="The id of the destination context";
    protected static final char OPT_NAME_USER_SHORT='u';
    protected static final String OPT_NAME_USER_LONG="username";
    protected static final char OPT_ID_USER_SHORT='i';
    protected static final String OPT_ID_USER_LONG="userid";
    protected static final String OPT_NAME_USER_DESCRIPTION="The name of the user which should be copied";
    protected static final String OPT_ID_USER_DESCRIPTION="The id of the user which should be copied";
    protected static final String OPT_NAME_MASTERADMINPASS_DESCRIPTION="master Admin password";
    protected static final String OPT_NAME_MASTERADMINUSER_DESCRIPTION="master Admin user name";

    protected CLIOption fromContextOption = null;
    protected CLIOption toContextOption = null;
    protected CLIOption userNameOption = null;
    protected CLIOption userIdOption = null;

    @Override
    protected String getObjectName() {
        return "user";
    }

    protected OXUserCopyInterface getUserCopyInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXUserCopyInterface) Naming.lookup(RMI_HOSTNAME + OXUserCopyInterface.RMI_NAME);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final UserCopy usercopy = new UserCopy();
        usercopy.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("usercopy");

        setOptions(parser);


        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);
            final OXUserCopyInterface rsi = getUserCopyInterface();

            final User user = userParsing(parser, userNameOption, userIdOption);
            final Context src = contextParsing(parser, fromContextOption);
            final Context dest = contextParsing(parser, toContextOption);

            final User result = rsi.copyUser(user, src, dest, auth);
            displaySuccessMessage(user, result, src.getId(), dest.getId(), parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    protected final void displaySuccessMessage(final User srcuser, final User destuser, final Integer srcctxid, final Integer destcontext, final AdminParser parser) {
        final StringBuilder sb = new StringBuilder(getObjectName());
        sb.append(" ");
        String srcUserName = srcuser.getName();
        Integer srcUserId = srcuser.getId();
        if (srcUserName == null) {
            sb.append(srcUserId);
        } else {
            sb.append(srcUserName);
        }
        sb.append(" copied");
        if (null != srcctxid) {
            sb.append(" from context ");
            sb.append(srcctxid);
        }
        if (null != destcontext) {
            sb.append(" to context ");
            sb.append(destcontext);
        }
        Integer destuserid = destuser.getId();
        if (null != destuserid) {
            sb.append(" with new user id ");
            sb.append(destuserid);
        }
        if ( null != parser && parser.checkNoNewLine()) {
            final String output = sb.toString().replace("\n", "");
            System.out.println(output);
        } else {
            System.out.println(sb.toString());
        }

    }


    private final Context contextParsing(final AdminParser parser, final CLIOption option) {
        final Context ctx = new Context();

        if (parser.getOptionValue(option) != null) {
            final Integer contextId = Integer.valueOf((String) parser.getOptionValue(option));
            ctx.setId(contextId);
        }
        return ctx;
    }

    private final User userParsing(final AdminParser parser, final CLIOption optionName, final CLIOption optionId) {
        final User user = new User();

        if (parser.getOptionValue(optionId) != null) {
            final Integer userid = Integer.valueOf((String) parser.getOptionValue(optionId));
            user.setId(userid);
        }
        if (parser.getOptionValue(optionName) != null) {
            final String username = (String) parser.getOptionValue(optionName);
            user.setName(username);
        }
        return user;
    }


    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        this.fromContextOption = setShortLongOpt(parser,OPT_NAME_FROM_CONTEXT_SHORT, OPT_NAME_FROM_CONTEXT_LONG, OPT_NAME_FROM_CONTEXT_DESCRIPTION, true, NeededQuadState.needed);
        this.toContextOption = setShortLongOpt(parser,OPT_NAME_TO_CONTEXT_SHORT, OPT_NAME_TO_CONTEXT_LONG, OPT_NAME_TO_CONTEXT_DESCRIPTION, true, NeededQuadState.needed);
        this.userNameOption = setShortLongOpt(parser,OPT_NAME_USER_SHORT, OPT_NAME_USER_LONG, OPT_NAME_USER_DESCRIPTION, true, NeededQuadState.eitheror);
        this.userIdOption = setShortLongOpt(parser,OPT_ID_USER_SHORT, OPT_ID_USER_LONG, OPT_ID_USER_DESCRIPTION, true, NeededQuadState.eitheror);
    }

    @Override
    protected void setAdminPassOption(final AdminParser admp) {
        this.adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_MASTERADMINPASS_DESCRIPTION, true, NeededQuadState.possibly);
    }

    @Override
    protected void setAdminUserOption(final AdminParser admp) {
        this.adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_MASTERADMINUSER_DESCRIPTION, true, NeededQuadState.possibly);
    }

}
