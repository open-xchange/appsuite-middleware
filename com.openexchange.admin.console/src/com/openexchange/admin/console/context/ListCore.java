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
import java.util.ArrayList;
import java.util.ServiceLoader;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleListInterface;
import com.openexchange.admin.console.context.extensioninterfaces.PluginException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends ContextAbstraction {

    private ServiceLoader<ContextConsoleListInterface> listsubclasses = null;

    private interface GetterClosureInterface {

        public ArrayList<String> getData(final ContextConsoleListInterface commonex) throws PluginException;
    }

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);

        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);
        setExtensionOptions(parser, ContextConsoleListInterface.class);

        Context[] ctxs = null;
        try {
            Credentials auth = null;
            String pattern = null;
            try {
                parser.ownparse(args);
                auth = credentialsparsing(parser);

                pattern = getSearchPattern(parser);
                parseAndSetExtensions(parser, null, auth);
            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }

            Integer length = null;
            if (parser.hasOption(this.lengthOption)) {
                length = Integer.valueOf((String) parser.getOptionValue(this.lengthOption));
            }

            Integer offset = null;
            if (parser.hasOption(this.offsetOption)) {
                offset = Integer.valueOf((String) parser.getOptionValue(this.offsetOption));
            }

            ctxs = maincall(parser, pattern, auth, length, offset);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }

        if (null != ctxs) {
            try {
                if (null != parser.getOptionValue(this.csvOutputOption)) {
                    precsvinfos(ctxs, parser);
                } else {
                    sysoutOutput(ctxs, parser);
                }
            } catch (InvalidDataException e) {
                printError(null, null, "Invalid data : " + e.getMessage(), parser);
                sysexit(1);
            } catch (RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
        }

        sysexit(0);
    }

    protected abstract String getSearchPattern(final AdminParser parser);

    protected abstract Context[] maincall(final AdminParser parser, final String search_pattern, final Credentials auth, final Integer length, final Integer offset) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, NoSuchContextException;

    @Override
    protected final String getObjectName() {
        return "contexts";
    }

    @Override
    protected ArrayList<String> getCSVDataOfAllExtensions(final Context ctx, final AdminParser parser) {
        return abstractGetter(parser, new GetterClosureInterface() {

            @Override
            public ArrayList<String> getData(final ContextConsoleListInterface commonex) throws PluginException {
                return commonex.getCSVData(ctx);
            }
        });
    }

    @Override
    protected ArrayList<String> getHumanReableDataOfAllExtensions(final Context ctx, final AdminParser parser) {
        return abstractGetter(parser, new GetterClosureInterface() {

            @Override
            public ArrayList<String> getData(final ContextConsoleListInterface commonex) throws PluginException {
                return commonex.getHumanReadableData(ctx);
            }
        });
    }

    @Override
    protected ArrayList<String> getCSVColumnsOfAllExtensions(final AdminParser parser) {
        return abstractGetter(parser, new GetterClosureInterface() {

            @Override
            public ArrayList<String> getData(final ContextConsoleListInterface commonex) {
                return commonex.getColumnNamesCSV();
            }
        });
    }

    @Override
    protected ArrayList<String> getHumanReadableColumnsOfAllExtensions(final AdminParser parser) {
        return abstractGetter(parser, new GetterClosureInterface() {

            @Override
            public ArrayList<String> getData(final ContextConsoleListInterface commonex) {
                return commonex.getColumnNamesHumanReadable();
            }
        });
    }

    private ArrayList<String> abstractGetter(final AdminParser parser, final GetterClosureInterface iface) {
        final ArrayList<String> retval = new ArrayList<String>();
        if (null == this.listsubclasses) {
            this.listsubclasses = ServiceLoader.load(ContextConsoleListInterface.class);
        }
        for (final ContextConsoleListInterface commoniface : this.listsubclasses) {
            try {
                retval.addAll(iface.getData(commoniface));
            } catch (PluginException e) {
                printError(null, null, "Error during initializing extensions: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
        }
        return retval;
    }
}
