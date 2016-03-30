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

package com.openexchange.admin.reseller.console;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 */
public class ListRestrictions extends ResellerAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
    }

    /**
     *
     */
    public ListRestrictions() {
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final ListRestrictions list = new ListRestrictions();
        list.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("listrestrictions");

        setOptions(parser);

        HashSet<Restriction> res = null;
        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);

            final OXResellerInterface rsi = getResellerInterface();

            res = OXResellerTools.array2HashSet(rsi.getAvailableRestrictions(auth));
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
        try {
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                // map user data to corresponding module access
                precsvinfos(res);
            } else {
                sysoutOutput(res);
            }
        } catch (final InvalidDataException e) {
            printError(null, null, "Invalid data : " + e.getMessage(), parser);
            sysexit(1);
        } catch (final RuntimeException e) {
            printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }

    private void sysoutOutput(final HashSet<Restriction> restrictions) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Restriction res : restrictions) {
            data.add(makeStandardData(res));
        }

        // doOutput(new String[] { "3r", "30l", "30l", "14l" },
        doOutput(new String[] { "l" }, new String[] { "Name" }, data);
    }

    private void precsvinfos(final HashSet<Restriction> adminlist) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("name");

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final Restriction admin : adminlist) {
            data.add(makeDataForCsv(admin));
        }
        doCSVOutput(columns, data);
    }

    /**
     * Generate data which can be processed by the csv output method.
     *
     * @param group
     * @param members
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    private ArrayList<String> makeDataForCsv(final Restriction res) {
        final ArrayList<String> admin_data = makeStandardData(res);

        return admin_data;
    }

    private ArrayList<String> makeStandardData(final Restriction res) {
        final ArrayList<String> admin_data = new ArrayList<String>();

        final String name = res.getName();
        if (name != null && name.trim().length() > 0) {
            admin_data.add(name);
        } else {
            admin_data.add(null); // name
        }
        return admin_data;
    }

    @Override
    protected String getObjectName() {
        return "restrictions";
    }
}
