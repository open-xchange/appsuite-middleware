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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.config;

import static com.openexchange.ajax.config.ConfigTools.getTimeZone;
import static com.openexchange.ajax.config.ConfigTools.readSetting;

import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AbstractAJAXTest;

/**
 * Test if the current time is sent successfully by the server.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CurrentTimeTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(CurrentTimeTest.class);

    /**
     * Maximum time difference between server and client. This test fails if
     * a greater difference is detected.
     */
    private static final long MAX_DIFFERENCE = 1000;

    /**
     * Path to the configuration parameter.
     */
    private static final String PATH = "currentTime";

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public CurrentTimeTest(final String name) {
        super(name);
    }

    /**
     * Tests if the current time is sent by the server through the configuration
     * interface.
     */
    public void testCurrentTime() throws Throwable {
        final TimeZone zone = getTimeZone(getWebConversation(), getHostName(),
            getSessionId());
        final String sTime = readSetting(getWebConversation(), getHostName(),
            getSessionId(), PATH);
        long serverTime = Long.parseLong(sTime);
        serverTime -= zone.getOffset(serverTime);
        final long localTime = System.currentTimeMillis();
        LOG.info("Local time: " + System.currentTimeMillis() + " Server time: "
            + serverTime);
        final long difference = Math.abs(localTime - serverTime);
        LOG.info("Time difference: " + difference);
        assertTrue("Too big time difference: ",
            difference < MAX_DIFFERENCE);
    }
}
