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

package com.openexchange.rest.passwordchange.history;

import static org.junit.Assert.fail;
import java.sql.Timestamp;
import static org.junit.Assert.assertEquals;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.passwordchange.history.rest.api.PasswordChangeHistoryREST;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ListTest extends AbstractPasswordchangehistoryTest {

    private static final String ARRAY_NAME = "PasswordChangeHistroy";

    private Long contextID;
    private Long userID;
    private Long limit = new Long(1);
    private Long send;

    @Override
    protected Application configure() {
        return new ResourceConfig(PasswordChangeHistoryREST.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Do a password change
        send = System.currentTimeMillis();
        PasswordChangeUpdateRequest request = new PasswordChangeUpdateRequest(testUser.getPassword(), testUser.getPassword(), true);
        getAjaxClient().execute(request);

        // Get context and user ID
        contextID = Integer.toUnsignedLong(getAjaxClient().getValues().getContextId());
        userID = Integer.toUnsignedLong(getAjaxClient().getValues().getUserId());
    }

    @Test
    public void testLimit() throws Exception {

        String retval = pwdhapi.list(contextID, userID, limit);
        JSONObject json = new JSONObject(retval);
        JSONArray array = json.getJSONArray(ARRAY_NAME);
        assertEquals("More than one element! Limitation did not work..", 1, array.asList().size());
    }

    @Test
    public void testTime() throws Exception {

        String retval = pwdhapi.list(contextID, userID, 0l);
        JSONObject json = new JSONObject(retval);
        JSONArray array = json.getJSONArray(ARRAY_NAME);

        for (int i = 0; i < array.length(); i++) {
            JSONArray info = array.getJSONArray(i);            
            Long lastModified = Long.parseLong(info.getString(0));

            if ((lastModified - send) > 10) {
                return;
            }
        }
        fail("Did not find any timestamp near the transmitting timestamp");
    }

}
