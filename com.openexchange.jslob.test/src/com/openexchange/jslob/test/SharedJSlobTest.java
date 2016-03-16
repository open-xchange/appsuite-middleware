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

package com.openexchange.jslob.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobService;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;
import com.openexchange.test.osgi.OSGiTest;

/**
 * {@link SharedJSlobTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SharedJSlobTest implements OSGiTest {

    private static volatile JSlobService jslobService;

    @Test
    public void testSharedJSlob() throws OXException, JSONException {
        Session session = new SimSession();
        assertNotNull("JSlob service was null", jslobService);
        JSlob jslob = jslobService.getShared("sharedjslob", session);
        assertNotNull("jslob was null", jslob);
        assertEquals("JSONObect's length was not 2", 2, jslob.getJsonObject().length());
        assertTrue("JSONObject has not key test1", jslob.getJsonObject().has("test1"));
        assertTrue("JSONObject has not key test2", jslob.getJsonObject().has("test2"));
        assertTrue("Key test1 was not true", jslob.getJsonObject().getBoolean("test1"));
        assertEquals("Key test2 was not -1", -1, jslob.getJsonObject().getInt("test2"));
    }

    public static void setJSlobService(JSlobService service) {
        jslobService = service;
    }

    @Override
    public Class<?>[] getTestClasses() {
        return new Class<?>[] { SharedJSlobTest.class };
    }

}
