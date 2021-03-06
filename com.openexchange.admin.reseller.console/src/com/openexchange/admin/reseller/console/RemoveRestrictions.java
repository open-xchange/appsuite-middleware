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

package com.openexchange.admin.reseller.console;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * @author choeger
 */
public class RemoveRestrictions extends ResellerAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
    }

    /**
     *
     */
    public RemoveRestrictions() {
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final RemoveRestrictions remove = new RemoveRestrictions();
        remove.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("removerestrictions");

        setOptions(parser);

        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);

            final OXResellerInterface rsi = getResellerInterface();

            rsi.removeDatabaseRestrictions(auth);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }
}
