/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.groupware.contexts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.impl.LoginInfo;
import com.openexchange.test.AjaxInit;

import junit.framework.TestCase;

/**
 * This test case tests the login process.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginTest extends TestCase {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginTest.class);

    /**
     * How much logins?
     */
    private static final int TRIES = 1;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
    }

    protected void tearDown() throws Exception {
        Init.stopServer();
        super.tearDown();
    }

    public void testLogin() throws Throwable {
        /*final LoginInfo login = LoginInfo.getInstance();
        final String user = AjaxInit.getAJAXProperty("login");
        final String password = AjaxInit.getAJAXProperty("password");
        for (int i = 0; i < TRIES; i++) {
            final long start = System.currentTimeMillis();
            final String[] result = login.handleLoginInfo(user, password);
            LOG.info("Login time: " + ((System.currentTimeMillis() - start) / 1000f)
                + "s");
            assertNotNull("Can't get context identifier.", result[0]);
            assertNotNull("Can't get user identifier.", result[1]);
            assertNotSame("Can't get context identifier.", result[0].length(), 0);
            assertNotSame("Can't get user identifier.", result[1].length(), 0);
            LOG.info("Context identifier: " + result[0]);
            LOG.info("User identifier: " + result[1]);
            if (result.length > 2) {
                LOG.info("1and1 Token: " + result[2]);
            }
        }*/
        //TODO: Fix this, please-with-sugar-on-top
    }
}
