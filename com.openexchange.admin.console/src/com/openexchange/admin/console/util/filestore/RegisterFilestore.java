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
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

/**
 *
 * @author d7,cutmasta
 *
 */
public class RegisterFilestore extends FilestoreAbstraction {

    private CLIOption ifNonexistent;

    public void execute(final String[] args2) {
        final AdminParser parser = new AdminParser("registerfilestore");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Filestore fstore = new Filestore();

            parseAndSetFilestorePath(parser, fstore);
            parseAndSetFilestoreSize(parser, fstore);
            parseAndSetFilestoreMaxCtxs(parser, fstore);

            if (parser.hasOption(ifNonexistent)) {
                Filestore[] filestore = oxutil.listFilestore(fstore.getUrl(), auth);
                if (filestore.length == 1) {
                    createMessageForStdout(String.valueOf(filestore[0].getId()), null, "found", parser);
                    sysexit(0);
                }
            }
            displayRegisteredMessage(String.valueOf(oxutil.registerFilestore(fstore, auth).getId()), parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new RegisterFilestore().execute(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setPathOption(parser, NeededQuadState.needed);
        setSizeOption(parser, String.valueOf(OXUtilInterface.DEFAULT_STORE_SIZE));
        setMaxCtxOption(parser, String.valueOf(OXUtilInterface.DEFAULT_STORE_MAX_CTX));

        ifNonexistent = setLongOpt(parser, "if-nonexistent", "If set returns the id of one an existing filestore instead of throwing an error.", false, false);
    }
}
