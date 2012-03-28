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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.admin.user.copy.rmi;

import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import junit.framework.Assert;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractTest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


public class UserCopyTest extends AbstractTest {

    private static final String PASSWORD = "secret";
    private static final String OXADMINMASTER = "oxadminmaster";

    @Test
    public final void testMoveUser() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User(1);
        final Context src = new Context(I(1));
        final Context dest = new Context(I(2));
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        oxu.copyUser(user, src, dest, auth);
    }

    @Test
    public final void testMoveUserNoUser() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final Context src = new Context(I(1));
        final Context dest = new Context(I(2));
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(null, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("The given source user object is null", e.getMessage());
        }
    }
    
    @Test
    public final void testMoveUserNoUserId() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User();
        final Context src = new Context(I(1));
        final Context dest = new Context(I(2));
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(user, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("The given source user object has no id", e.getMessage());
        }
    }
    
    @Test
    public final void testMoveUserNoSrcContext() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User(1);
        final Context src = null;
        final Context dest = new Context(I(2));
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(user, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid source context data object", e.getMessage());
        }
    }
    
    @Test
    public final void testMoveUserNoSrcContextId() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User(1);
        final Context src = new Context();
        final Context dest = new Context(I(2));
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(user, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid source context data object", e.getMessage());
        }
    }
    
    @Test
    public final void testMoveUserNoDestContext() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User(1);
        final Context src = new Context(I(1));
        final Context dest = null;
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(user, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid destination context data object", e.getMessage());
        }
    }
    
    @Test
    public final void testMoveUserNoDestContextId() throws Throwable {
        final OXUserCopyInterface oxu = getUserMoveClient();
        final User user = new User(1);
        final Context src = new Context(I(1));
        final Context dest = new Context();
        final Credentials auth = new Credentials(OXADMINMASTER, PASSWORD);
        try {
            oxu.copyUser(user, src, dest, auth);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid destination context data object", e.getMessage());
        }
    }
    
    private OXUserCopyInterface getUserMoveClient() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXUserCopyInterface) Naming.lookup(getRMIHostUrl()+ OXUserCopyInterface.RMI_NAME);
    }
}
