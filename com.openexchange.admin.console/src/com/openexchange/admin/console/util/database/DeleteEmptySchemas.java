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

    public DeleteEmptySchemas(final String[] args2) {
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
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }

    }

    @Override
    protected String getObjectName() {
        return "database schema";
    }

    public static void main(final String args[]) {
        new DeleteEmptySchemas(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser, NeededQuadState.notneeded, "The optional ID of a certain database host. If missing all database hosts are considered");
        setDatabaseNameOption(parser, NeededQuadState.notneeded, "The optional name of a certain database host (as alternative for \"" + OPT_NAME_DATABASE_ID_LONG + "\" option). If missing all database hosts are considered");

        setDatabaseSchemaOption(parser, false);
        setSchemasToKeepOption(parser);
    }
}
