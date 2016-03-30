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
package com.openexchange.admin.console;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

public class HostingCLITest extends AbstractRMITest {

    @After
    public void teardown() throws Exception {
        super.tearDown();
        final ShellExecutor se = new ShellExecutor();
        final ArrayOutput deleteresult = se.executeprocargs(new String[] {
            prefix + "deletecontext", "-c", ctxid, "-A", OXADMINMASTER, "-P",
            MASTER_PW, "-H", getRMIHost() });
        assertTrue("Deleting of context failed", deleteresult.exitstatus==0);
    }

    @Test
    public void testall() throws IOException, InterruptedException {
        final ShellExecutor se = new ShellExecutor();
        final ArrayOutput result = se.executeprocargs(new String[] {
            prefix + "createcontext", "-c", ctxid, "-A", OXADMINMASTER, "-P",
            MASTER_PW, "-N", "test", "-q", "1000", "-u", ADMIN_USER, "-d",
            "admin", "-g", "admin", "-s", "admin", "-e", "xyz@bla.de",
            "-p", ADMIN_PW, "-H", getRMIHost() });
        assertTrue("Creation of context failed: " + result.errOutput, result.exitstatus==0);


        final ArrayOutput listresult = se.executeprocargs(new String[] {
            prefix + "listcontext", "-A", OXADMINMASTER, "-P", MASTER_PW, "--csv", "-H", getRMIHost() });
        assertTrue("Listing of contexts failed", listresult.exitstatus==0);

        // Check right context id
        final int column = getCSVColumnFor(listresult.stdOutput.get(0), "id");
        assertTrue("Couldn't find column heading", column != -1);
        final int row = getCSVRow(listresult.stdOutput, column, ctxid);
        assertTrue("Couldn't find value for context", row != -1);

    }

    private int getCSVColumnFor(final String csvcolumns, final String columnname) {
        final String[] columns = csvcolumns.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(columnname)) {
                return i;
            }
        }
        return -1;
    }

    private int getCSVRow(final ArrayList<String> csvdata, final int column, final String value) {
        for (int i = 0; i < csvdata.size(); i++) {
            final String[] split = csvdata.get(i).split(",");
            if (split[column].equals("\"" + value + "\"")) {
                return i;
            }
        }
        return -1;
    }
}
