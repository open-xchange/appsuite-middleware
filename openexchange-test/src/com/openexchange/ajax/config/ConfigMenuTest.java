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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * This test case tests the AJAX interface of the config system for the AJAX
 * GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenuTest extends AbstractAJAXSession {

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
     */
    public void testReadSettings() throws Throwable {
        final GetRequest request = new GetRequest(Tree.ALL);
        final GetResponse response = getClient().execute(request);
        final String value = response.getString();
        LOG.trace("Settings: " + value);
        assertTrue("Got no value from server.", value.length() > 0);
        new JSONObject(value);
    }

    /**
     * Tests if the timezone of a user can be changed.
     */
    public void testTimeZone() throws Throwable {
        GetRequest getRequest = new GetRequest(Tree.TimeZone);
        GetResponse getResponse = getClient().execute(getRequest);
        String timeZone = getResponse.getString();
        String testTimeZone = "Australia/Hobart";
        SetRequest setRequest = new SetRequest(Tree.TimeZone, testTimeZone);
        try {
            getClient().execute(setRequest);
            getResponse = getClient().execute(getRequest);
            assertEquals("Written timezone isn't returned from server.", testTimeZone, getResponse.getString());
        } finally {
            setRequest = new SetRequest(Tree.TimeZone, timeZone);
            getClient().execute(setRequest);
        }
    }

    /**
     * Tests if the unique identifier of the user can be loaded.
     */
    public void testIdentifier() throws Throwable {
        GetRequest request = new GetRequest(Tree.Identifier);
        GetResponse response = getClient().execute(request);
        final int userId = response.getInteger();
        LOG.trace("UserId: " + userId);
        assertTrue("No valid user identifier", userId > 0);
    }
}
