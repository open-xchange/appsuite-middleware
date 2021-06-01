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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link List}s all contexts
 *
 */
public class List extends ListCore {

    public List(final String[] args2) {
        final AdminParser parser = new AdminParser("listcontext");
        commonfunctions(parser, args2);
    }

    @SuppressWarnings("unused")
    public static void main(final String args[]) {
        new List(args);
    }

    @Override
    protected Context[] maincall(AdminParser parser, String search_pattern, Credentials auth, final Integer length, final Integer offset) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException {
        OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
        boolean csv = null != parser.getOptionValue(this.csvOutputOption);

        boolean first = true;
        int len = (length == null) ? 10000 : i(length);
        int off = (offset == null) ? 0 : i(offset);
        Context[] ctxs;
        for (; (ctxs = oxctx.list(search_pattern, off, len, auth)).length == len; off = off + len) {
            if (first) {
                if (csv) {
                    precsvinfos(ctxs, parser);
                } else {
                    sysoutOutput(ctxs, parser);
                }
                first = false;
            } else {
                if (csv) {
                    precsvinfos(ctxs, true, parser);
                } else {
                    sysoutOutput(ctxs, true, parser);
                }
            }
        }
        if (first) {
            if (csv) {
                precsvinfos(ctxs, parser);
            } else {
                sysoutOutput(ctxs, parser);
            }
            first = false;
        } else {
            if (csv) {
                precsvinfos(ctxs, true, parser);
            } else {
                sysoutOutput(ctxs, true, parser);
            }
        }

        return null;
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        setSearchPatternOption(parser);
        setLengthOption(parser);
        setOffsetOption(parser);
    }

    @Override
    protected String getSearchPattern(AdminParser parser) {
        String pattern = (String) parser.getOptionValue(this.searchOption);

        if (null == pattern) {
            pattern = "*";
        }
        return pattern;
    }
}
