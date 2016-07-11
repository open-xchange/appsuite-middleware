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
 * @author d7,cutmasta
 *
 */
public class UnblockDatabase extends DatabaseAbstraction {

    public UnblockDatabase(final String[] args2) {

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
                    displayAlreadyUnblockedMessage(successtext, null, parser);
                } else {
                    displayUnblockedMessage(successtext, null, parser);
                }
            } else {
                displayUnblockedMessage(successtext, null, parser);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    private String overridingObjectName;

    @Override
    protected String getObjectName() {
        return null == overridingObjectName ? super.getObjectName() : overridingObjectName;
    }

    private final void displayUnblockedMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "unblocked", parser);
    }

    private final void displayAlreadyUnblockedMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "already unblocked", parser);
    }

    public static void main(final String args[]) {
        new UnblockDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        // oxadmin,oxadmin passwd
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);

        setDatabaseSchemaOption(parser);
    }
}
