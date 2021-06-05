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

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;

public class GetAccessCombinationName extends UserAbstraction {

    public static void main(final String[] args) {
        new GetAccessCombinationName().execute(args);
    }

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);
    }

    public void execute(final String[] args) {
        final AdminParser parser = new AdminParser("getaccesscombinationnameforuser");
        // set all needed options in our parser
        setOptions(parser);
        String successtext = null;
        try {
            parser.ownparse(args);

            // create user obj
            final User usr = new User();

            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);

            successtext = nameOrIdSetInt(this.userid, this.username, "user");

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // rmi interface
            final OXUserInterface oxusr = getUserInterface();

            // printout access name
            System.out.println(oxusr.getAccessCombinationName(ctx, usr, auth));

            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, ctxid, e, parser);
        }
    }

}
