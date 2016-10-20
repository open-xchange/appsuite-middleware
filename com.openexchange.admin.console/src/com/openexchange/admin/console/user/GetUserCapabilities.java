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
import java.util.Set;
import java.util.TreeSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.java.Strings;

/**
 * {@link GetUserCapabilities} - Determines the capabilities for a context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public class GetUserCapabilities extends UserAbstraction {

    /**
     * Initializes a new {@link GetUserCapabilities}.
     *
     * @param args The CLI arguments
     */
    public GetUserCapabilities(String[] args) {
        final AdminParser parser = new AdminParser("getusercapabilities");
        commonfunctions(parser, args);
    }

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        String userString = null;
        try {
            parser.ownparse(args);

            // create user obj
            final User usr = new User();

            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);

            userString = compileUserString(usr, nameOrIdSetInt(this.userid, this.username, "user"));

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // rmi interface
            final OXUserInterface oxusr = getUserInterface();

            Set<String> caps = oxusr.getCapabilities(ctx, usr, auth);

            StringBuilder sb = new StringBuilder(512);
            if (null == caps || caps.isEmpty()) {
                sb.append("There are no capabilities set for user ").append(userString).append(" in context ").append(ctx.getId());
            } else {
                final String lf = System.getProperty("line.separator");
                sb.append("Capabilities for user ").append(userString).append(" in context ").append(ctx.getId()).append(":").append(lf);
                for (final String cap : new TreeSet<String>(caps)) {
                    sb.append(cap).append(lf);
                }
            }
            System.out.println(sb.toString());

            sysexit(0);
        } catch (final Exception e) {
            printErrors(userString, ctxid, e, parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        }
    }

    /**
     * Retrieves the user string from the specified {@link User} or the optional string.
     * If the optional string is empty, then the identifier of the specified user is returned.
     * If the identifier is <code>null</code> then the name of the user is returned as a fall-back.
     * 
     * @param user the {@link User}
     * @return The user string
     */
    private String compileUserString(User user, String optionalString) {
        if (Strings.isEmpty(optionalString)) {
            return user.getId() != null ? Integer.toString(user.getId()) : user.getName();
        }
        return optionalString;
    }

    private Set<String> maincall(Context ctx, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, StorageException, NoSuchContextException, InvalidDataException {
        final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
        return oxres.getCapabilities(ctx, auth);
    }

    public static void main(String[] args) {
        new GetUserCapabilities(args);
    }
}
