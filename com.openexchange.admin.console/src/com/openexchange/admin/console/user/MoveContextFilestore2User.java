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
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;

public class MoveContextFilestore2User extends UserFilestoreAbstraction {

    public static void main(String args[]) {
        new MoveContextFilestore2User().execute(args);
    }

    // -----------------------------------------------------------------------------------------------

    public void execute(String[] args) {
        final AdminParser parser = new AdminParser("movecontextfilestore2user");
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args);

            User usr = new User();
            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);
            successtext = nameOrIdSetInt(this.userid, this.username, "user");

            Context ctx = contextparsing(parser);
            Credentials auth = credentialsparsing(parser);

            Filestore filestore = parseAndSetFilestoreId(parser);

            long maxQuota = parseAndGetUserQuota(parser);

            // get rmi ref
            OXUserInterface oxusr = getUserInterface();

            int jobId = oxusr.moveFromContextToUserFilestore(ctx, usr, filestore, maxQuota, auth);

            displayMovedMessage(successtext, null, "to user filestore " + filestore.getId() + " scheduled as job " + jobId, parser);
            sysexit(0);
        } catch (Exception e) {
            // In this special case the second parameter is not the context id but the filestore id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(successtext, ctxid, e, parser);
        }
    }

    protected final void displayMovedMessage(final String id, final Integer ctxid, final String text, final AdminParser parser) {
        createMessageForStdout(id, ctxid, text, parser);
    }

    private void setOptions(final AdminParser parser) {
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);

        setDefaultCommandLineOptionsWithoutContextID(parser);

        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        setFilestoreIdOption(parser, true);
        setUserQuotaOption(parser, true);
    }
}
