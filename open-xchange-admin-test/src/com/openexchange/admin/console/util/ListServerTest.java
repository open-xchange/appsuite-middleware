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

package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.server.ListServer;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author cutmasta
 */
public class ListServerTest extends AbstractRMITest {

    private int returnCodeListServerCSV;

    private int returnCodeListServerInvalidCredentials;

    private int returnCodeListServerUnknownOption;

    @Test
    public void testListServer() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException {
        OXUtilInterface utilInterface = getUtilInterface();
        Server[] listServer = utilInterface.listServer("*", superAdminCredentials);

        Assert.assertNotNull(listServer);
    }

    @Test
    public void testListServerCSV() {
        new ListServer(new String[] { "-A", OXADMINMASTER, "-P", MASTER_PW, "-H", getRMIHost(), "--csv" }) {

            @Override
            protected void sysexit(int exitCode) {
                ListServerTest.this.returnCodeListServerCSV = exitCode;
            }
        };

        assertTrue("Listing of server failed with return code: " + returnCodeListServerCSV, returnCodeListServerCSV == 0);
    }

    @Test
    public void testListServerInvalidCredentials() {
        new ListServer(new String[] { "-A", OXADMINMASTER + "_xyzfoobar", "-P", MASTER_PW + "_xyzfoobar" }) {

            @Override
            protected void sysexit(int exitCode) {
                ListServerTest.this.returnCodeListServerInvalidCredentials = exitCode;
            }
        };

        assertTrue(
            "Listing of server failed with return code: " + returnCodeListServerInvalidCredentials,
            returnCodeListServerInvalidCredentials == 0);
    }

    @Test
    public void testListServerUnknownOption() {
        new ListServer(new String[] { "-A", OXADMINMASTER, "-P", MASTER_PW, "-H", getRMIHost(), "--foouknownoption", "bar" }) {

            @Override
            protected void sysexit(int exitCode) {
                ListServerTest.this.returnCodeListServerUnknownOption = exitCode;
            }
        };

        assertTrue(
            "Listing of server failed with return code: " + returnCodeListServerUnknownOption,
            returnCodeListServerUnknownOption == BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION);
    }
}
