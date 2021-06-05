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

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class DeleteEmptySchemas extends DatabaseAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("deleteemptyschema");
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            Credentials auth = credentialsparsing(parser);

            // get rmi ref
            OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            Database db = new Database();
            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);
            parseAndSetSchema(parser, db);
            parseAndSetSchemasToKeep(parser);

            boolean noDbGiven = dbid == null && dbname == null;
            if (noDbGiven) {
                // Neither database ID nor name
                if (db.getScheme() != null) {
                    System.err.println("Either \"" + OPT_NAME_DATABASE_ID_LONG + "\" or \"" + OPT_NAME_DBNAME_LONG + "\" needs to be specified when setting \"" + OPT_NAME_SCHEMA_LONG + "\" option");
                    sysexit(SYSEXIT_INVALID_DATA);
                }

                db = null;
            }

            int numberOfDeletedSchemas = oxutil.deleteEmptySchemas(db, schemasToKeep, auth);
            if (noDbGiven) {
                System.out.println("Successfully deleted " + numberOfDeletedSchemas + " empty schemas");
            } else {
                System.out.println("Successfully deleted " + numberOfDeletedSchemas + " empty schemas from database " + (null == dbid ? dbname : dbid));
            }
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }

    }

    @Override
    protected String getObjectName() {
        return "database schema";
    }

    public static void main(final String args[]) {
        new DeleteEmptySchemas().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser, NeededQuadState.notneeded, "The optional ID of a certain database host. If missing all database hosts are considered");
        setDatabaseNameOption(parser, NeededQuadState.notneeded, "The optional name of a certain database host (as alternative for \"" + OPT_NAME_DATABASE_ID_LONG + "\" option). If missing all database hosts are considered");

        setDatabaseSchemaOption(parser, false);
        setSchemasToKeepOption(parser);
    }
}
