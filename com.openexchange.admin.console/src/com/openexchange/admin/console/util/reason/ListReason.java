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
package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 *
 * @author d7,cutmasta
 *
 */
public class ListReason extends ReasonAbstraction {

    public ListReason(final String[] args2) {

        final AdminParser parser = new AdminParser("listreason");
        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String pattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                pattern = (String) parser.getOptionValue(this.searchOption);
            }

            final MaintenanceReason[] mrs = oxutil.listMaintenanceReason(pattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(mrs);
            } else {
                sysoutOutput(mrs);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListReason(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(final MaintenanceReason[] mrs) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }

        //doOutput(new String[] { "3r", "72l" }, new String[] { "Id", "Text" }, data);
        doOutput(new String[] { "r", "l" }, new String[] { "Id", "Text" }, data);
    }

    private void precsvinfos(final MaintenanceReason[] mrs) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("text");

        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }

        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(final MaintenanceReason mr) {
        final ArrayList<String> rea_data = new ArrayList<String>();
        rea_data.add(mr.getId().toString());
        rea_data.add(mr.getText());

        return rea_data;
    }

    @Override
    protected final String getObjectName() {
        return "reasons";
    }
}
