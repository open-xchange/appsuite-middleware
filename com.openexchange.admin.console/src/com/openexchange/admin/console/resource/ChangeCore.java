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
package com.openexchange.admin.console.resource;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;

public abstract class ChangeCore extends ResourceAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // id is required
        setIdOption(parser);
        setNameOption(parser, NeededQuadState.eitheror);

        // optional
        setDisplayNameOption(parser, false);
        setAvailableOption(parser, false);
        setDescriptionOption(parser, false);
        setEmailOption(parser, false);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args);

            final Resource res = new Resource();

            parseAndSetResourceId(parser, res);
            parseAndSetResourceName(parser, res);

            successtext = nameOrIdSet(this.resourceid, this.resourcename, "resource");

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXResourceInterface oxres = getResourceInterface();

            parseAndSetMandatoryFields(parser, res);

            maincall(parser, oxres, ctx, res, auth);

            oxres.change(ctx, res, auth);

            displayChangedMessage(successtext, ctxid, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, ctxid, e, parser);
            sysexit(1);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXResourceInterface oxres, final Context ctx, final Resource res, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
