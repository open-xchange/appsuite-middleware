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

import static com.openexchange.java.Autoboxing.i;
import java.util.Set;
import java.util.TreeSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
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
    public void execute(String[] args) {
        commonfunctions(new AdminParser("getusercapabilities"), args);
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
                for (final String cap : new TreeSet<>(caps)) {
                    sb.append(cap).append(lf);
                }
            }
            System.out.println(sb.toString());

            sysexit(0);
        } catch (Exception e) {
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
            return user.getId() != null ? Integer.toString(i(user.getId())) : user.getName();
        }
        return optionalString;
    }

    public static void main(String[] args) {
        new GetUserCapabilities().execute(args);
    }
}
