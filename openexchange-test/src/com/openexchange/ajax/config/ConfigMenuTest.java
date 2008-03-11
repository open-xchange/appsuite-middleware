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

package com.openexchange.ajax.config;

import static com.openexchange.ajax.config.ConfigTools.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.tree.TimeZone;

/**
 * This test case tests the AJAX interface of the config system for the AJAX
 * GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenuTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ConfigMenuTest.class);

    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public ConfigMenuTest(final String name) {
        super(name);
    }

    /**
     * Tests if the settings can be read from the server.
     * @throws Throwable if an error occurs.
     */
    public void testReadSettings() throws Throwable {
        final String value = readSetting(getWebConversation(), getHostName(),
            getSessionId(), "");
        LOG.info("Settings: " + value);
        assertTrue("Got no value from server.", value.length() > 0);
    }

    /**
     * Tests if the timezone of a user can be changed.
     * @throws Throwable if an error occurs.
     */
    public void testTimeZone() throws Throwable {
        final String timeZone = readSetting(getWebConversation(), getHostName(),
            getSessionId(), TimeZone.NAME);
        final String testTimeZone = "Australia/Hobart";
        storeSetting(getWebConversation(), getHostName(), getSessionId(),
            TimeZone.NAME, testTimeZone);
        assertEquals("Written timezone isn't returned from server.",
            testTimeZone, readSetting(getWebConversation(), getHostName(),
                getSessionId(), TimeZone.NAME));
        storeSetting(getWebConversation(), getHostName(), getSessionId(),
            TimeZone.NAME, timeZone);
    }

    /**
     * Tests if the unique identifier of the user can be loaded.
     * @throws Throwable if an error occurs.
     */
    public void testIdentifier() throws Throwable {
        final int userId = getUserId(getWebConversation(), getHostName(),
            getSessionId());
        LOG.trace("UserId: " + userId);
        assertTrue("No valid user identifier", userId > 0);
    }
}
