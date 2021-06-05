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

package com.openexchange.admin.console.util.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author d7,cutmasta
 *
 */
public class UnblockDatabase extends DatabaseAbstraction {

    public void execute(final String[] args2) {

        final AdminParser parser = new AdminParser("unblockdatabase");

        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Database db = new Database();

            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);
            parseAndSetSchema(parser, db);
            boolean hasSchema = db.getScheme() != null;

            successtext = nameOrIdSet(this.dbid, this.dbname, "database");

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
            Database[] unblockedSchemas = oxutil.unblockDatabase(db, auth);

            if (hasSchema) {
                overridingObjectName = "schema";
                successtext = db.getScheme();
                if (unblockedSchemas.length == 0) {
                    displayAlreadyUnblockedMessage(successtext, parser);
                } else {
                    displayUnblockedMessage(successtext, parser);
                }
            } else {
                if (unblockedSchemas.length == 0) {
                    displayAlreadyUnblockedMessage(successtext, parser);
                } else {
                    displayUnblockedMultipleMessage(successtext, unblockedSchemas, parser);
                }
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    private void sysoutOutput(Database[] databases) throws InvalidDataException, URISyntaxException {
        ArrayList<ArrayList<String>> data = new ArrayList<>(databases.length);
        for (Database database : databases) {
            data.add(makeStandardData(database));
        }

        doOutput(new String[] { "r", "l", "l", "l" }, new String[] { "id", "name", "hostname", "scheme" }, data);
    }

    private ArrayList<String> makeStandardData(Database db) throws URISyntaxException {
        ArrayList<String> rea_data = new ArrayList<>();

        rea_data.add(db.getId().toString());

        if (null != db.getName()) {
            rea_data.add(db.getName());
        } else {
            rea_data.add(null);
        }

        if (null != db.getUrl()) {
            rea_data.add(new URI(db.getUrl().substring("jdbc:".length())).getHost());
        } else {
            rea_data.add(null);
        }

        if (null != db.getScheme()) {
            rea_data.add(db.getScheme().toString());
        } else {
            rea_data.add(null);
        }

        return rea_data;
    }

    private String overridingObjectName;

    @Override
    protected String getObjectName() {
        return null == overridingObjectName ? super.getObjectName() : overridingObjectName;
    }

    private final void displayUnblockedMessage(String id, AdminParser parser) {
        createMessageForStdout(id, null, "unblocked", parser);
    }

    private final void displayUnblockedMultipleMessage(String id, Database[] databases, AdminParser parser) throws Exception {
        createMessageForStdout("unblocked the following schemas from database " + id + ":", parser);
        createLinefeedForStdout(parser);
        sysoutOutput(databases);
    }

    private final void displayAlreadyUnblockedMessage(String id, AdminParser parser) {
        createMessageForStdout(id, null, "is already unblocked", parser);
    }

    public static void main(final String args[]) {
        new UnblockDatabase().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        // oxadmin,oxadmin passwd
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);

        setDatabaseSchemaOption(parser);
    }
}
