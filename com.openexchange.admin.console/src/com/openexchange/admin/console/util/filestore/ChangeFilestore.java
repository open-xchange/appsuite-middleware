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

package com.openexchange.admin.console.util.filestore;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

/**
 *
 * @author d7,cutmasta
 *
 */
public class ChangeFilestore extends FilestoreAbstraction {

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("changefilestore");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Filestore fstore = new Filestore();
            parseAndSetFilestoreID(parser, fstore);
            parseAndSetFilestorePath(parser, fstore);
            parseAndSetFilestoreSize(parser, fstore);
            parseAndSetFilestoreMaxCtxs(parser, fstore);

            oxutil.changeFilestore(fstore, auth);

            displayChangedMessage(filestoreid, null, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(filestoreid, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ChangeFilestore().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setFilestoreIDOption(parser, "changed");
        setPathOption(parser, NeededQuadState.notneeded);
        setSizeOption(parser, null);
        setMaxCtxOption(parser, null);
    }
}
