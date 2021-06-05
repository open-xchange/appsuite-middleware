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

public class ListContextByDatabase extends ContextAbstraction {

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);

        setCSVOutputOption(parser);
    }

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("listcontext");

        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Database db = new Database();

            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);

            successtext = nameOrIdSetInt(db.getId(), db.getName(), "database");

            final Credentials auth = credentialsparsing(parser);
            boolean csv = null != parser.getOptionValue(this.csvOutputOption);

            OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);

            boolean first = true;
            int offset = 0;
            int length = 10000;
            Context[] ctxs;
            for (; (ctxs = oxctx.listByDatabase(db, offset, length, auth)).length == length; offset = offset + length) {
                if (first) {
                    if (csv) {
                        precsvinfos(ctxs, parser);
                    } else {
                        sysoutOutput(ctxs, parser);
                    }
                    first = false;
                } else {
                    if (csv) {
                        precsvinfos(ctxs, true, parser);
                    } else {
                        sysoutOutput(ctxs, true, parser);
                    }
                }
            }
            if (first) {
                if (csv) {
                    precsvinfos(ctxs, parser);
                } else {
                    sysoutOutput(ctxs, parser);
                }
                first = false;
            } else {
                if (csv) {
                    precsvinfos(ctxs, true, parser);
                } else {
                    sysoutOutput(ctxs, true, parser);
                }
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListContextByDatabase().execute(args);
    }

    @Override
    protected final String getObjectName() {
        return "contexts for database";
    }
}
