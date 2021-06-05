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

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListQuotaCore extends ContextAbstraction {

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setCSVOutputOption(parser);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        Quota[] quotas = null;
        try {
            Context ctx = null;
            Credentials auth = null;
            try {
                parser.ownparse(args);
                ctx = contextparsing(parser);
                auth = credentialsparsing(parser);
            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
            quotas = maincall(parser, ctx, auth);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }

        try {
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(quotas);
            } else {
                if (null == quotas || quotas.length <= 0) {
                    System.out.println("No quota limits specified in database for context " + ctxid);
                } else {
                    System.out.println("Listing quota limits specified in database for context " + ctxid);
                    sysoutOutput(quotas);
                }
            }
        } catch (InvalidDataException e) {
            printError(null, null, "Invalid data : " + e.getMessage(), parser);
            sysexit(1);
        } catch (RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }

        sysexit(0);
    }

    protected abstract Quota[] maincall(AdminParser parser, Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, NoSuchContextException;

    @Override
    protected final String getObjectName() {
        return "quotas";
    }

}
