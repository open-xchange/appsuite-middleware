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

package com.openexchange.admin.console.context;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

public class MoveContextDatabase extends ContextAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("movecontextdatabase");
        setOptions(parser);

        String successcontext = null;
        final Database db = new Database();
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            parseAndSetContextName(parser, ctx);

            successcontext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);

            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);

            /*
             * final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));
             * 
             * oxres.moveContextDatabase(ctx, db, mr, auth);
             */
            final int jobId = oxres.moveContextDatabase(ctx, db, auth);

            displayMovedMessage(successcontext, null, "to database " + (db.getId() != null ? db.getId() : db.getName()) + " scheduled as job " + jobId, parser);
            sysexit(0);
        } catch (Exception e) {
            // In this special case the second parameter is not the context id but the database id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(successcontext, db.getId(), e, parser);
        }
    }

    public static void main(final String args[]) {
        new MoveContextDatabase().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        //setMaintenanceReasodIDOption(parser, true);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);
    }

    @Override
    protected String getObjectName() {
        return "move context";
    }
}
