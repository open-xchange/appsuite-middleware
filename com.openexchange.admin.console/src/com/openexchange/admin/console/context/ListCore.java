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
            } catch (final RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
            ctxs = maincall(parser, pattern, auth);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }

        try {
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(ctxs, parser);
            } else {
                sysoutOutput(ctxs, parser);
            }
        } catch (final InvalidDataException e) {
            printError(null, null, "Invalid data : " + e.getMessage(), parser);
            sysexit(1);
        } catch (final RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }

        sysexit(0);
    }

    protected abstract String getSearchPattern(final AdminParser parser);

    protected abstract Context[] maincall(final AdminParser parser, final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, NoSuchContextException;

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
