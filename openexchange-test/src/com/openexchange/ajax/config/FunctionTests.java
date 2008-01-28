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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * This class contains tests for added funtionalities of the configuration tree. 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FunctionTests extends AbstractAJAXSession {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(FunctionTests.class);

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public FunctionTests(final String name) {
        super(name);
    }

    /**
     * Tests if the idle timeout for uploaded files is sent to the GUI.
     * @throws Throwable if an exception occurs.
     */
    public void testMaxUploadIdleTimeout() throws Throwable {
        final int value = ConfigTools.get(getClient(), new GetRequest(
            Tree.MaxUploadIdleTimeout)).getInteger();
        LOG.info("Max upload idle timeout: " + value);
        assertTrue("Got no value for the maxUploadIdleTimeout configuration "
            + "parameter.", value > 0);
    }

    /**
     * Maximum time difference between server and client. This test fails if
     * a greater difference is detected.
     */
    private static final long MAX_DIFFERENCE = 1000;

    /**
     * Tests if the current time is sent by the server through the configuration
     * interface.
     */
    public void testCurrentTime() throws Throwable {
        final AJAXClient client = getClient();
        final Date sTime = client.getValues().getServerTime();
        final long localTime = System.currentTimeMillis();
        LOG.info("Local time: " + System.currentTimeMillis() + " Server time: "
            + sTime.getTime());
        final long difference = Math.abs(localTime - sTime.getTime());
        LOG.info("Time difference: " + difference);
        assertTrue("Too big time difference: ", difference < MAX_DIFFERENCE);
    }

    /**
     * Tests if the server gives the context identifier.
     */
    public void testContextID() throws Throwable {
        final int value = ConfigTools.get(getClient(), new GetRequest(
            Tree.ContextID)).getInteger();
        LOG.info("Context identifier: " + value);
        assertTrue("Got no value for the contextID configuration parameter.",
            value > 0);
    }
}
