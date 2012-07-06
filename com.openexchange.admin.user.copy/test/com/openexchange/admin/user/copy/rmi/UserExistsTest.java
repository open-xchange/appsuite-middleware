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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.Test;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.UserExistsException;


/**
 * {@link UserExistsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserExistsTest extends AbstractRMITest {
    
    private OXContextInterface ci;
    private OXUserInterface ui;
    private final OXUserCopyInterface oxu;
    private User admin;
    private Context srcCtx;
    private Context dstCtx;
    private User srcUser;
    private User dstUser;


    /**
     * Initializes a new {@link UserExistsTest}.
     */
    public UserExistsTest() throws Exception {
        super();
        oxu = getUserCopyClient();
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ci = getContextInterface();
        admin = newUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@netline.de");
        srcCtx = ci.create(newContext("UserMoveSourceCtx", 23432545), admin, "all", superAdminCredentials);
        dstCtx = ci.create(newContext("UserMoveDestinationCtx", 23432546), admin, "all", superAdminCredentials);
        ui = getUserInterface();
        final User srcTest = newUser("user", "secret", "Test User", "Test", "User", "test.user@netline.de");
        srcUser = ui.create(srcCtx, srcTest, getCredentials());
        final User dstTest = newUser("user", "secret", "Test User", "Test", "User", "test.user@netline.de");
        dstUser = ui.create(dstCtx, dstTest, getCredentials());
    }
    
    @Override
    public void tearDown() throws Exception {
        ui.delete(srcCtx, srcUser, getCredentials());
        ui.delete(dstCtx, dstUser, getCredentials());
        ci.delete(srcCtx, superAdminCredentials);
        ci.delete(dstCtx, superAdminCredentials);
        super.tearDown();
    }
    
    @Test
    public void testUserExists() throws Exception {
        try {
            oxu.copyUser(srcUser, srcCtx, dstCtx, superAdminCredentials);
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue("No UserExistsException thrown.", e instanceof UserExistsException);
            
        }
    }
    
    private OXUserCopyInterface getUserCopyClient() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXUserCopyInterface) Naming.lookup(getRMIHostUrl() + OXUserCopyInterface.RMI_NAME);
    }

}
