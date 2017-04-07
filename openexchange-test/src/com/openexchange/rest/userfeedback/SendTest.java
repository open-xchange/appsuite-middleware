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

package com.openexchange.rest.userfeedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.restclient.invoker.ApiException;
import com.openexchange.userfeedback.rest.services.SendUserFeedbackService;

/**
 * {@link SendTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class SendTest extends AbstractUserFeedbackTest {

    private JSONArray recipients = null;

    @Override
    protected Application configure() {
        return new ResourceConfig(SendUserFeedbackService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recipients = new JSONArray();
        JSONObject recipient1 = new JSONObject();
        recipient1.put("address", testUser.getLogin());
        recipient1.put("displayName", testUser.getUser());
        recipients.put(recipient1);
    }

    @Test
    public void testSend_everythingFine_returnMessage() {
        try {
            userfeedbackApi.sendMail("default", type, recipients.toString(), new Long(0), new Long(0), "subject", "body");
            assertEquals(200, getRestClient().getStatusCode());
        } catch (ApiException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSend_unknownContextGroup_return404() {
        try {
            userfeedbackApi.sendMail("unknown", type, recipients.toString(), new Long(0), new Long(0), "subject", "body");
            fail();
        } catch (ApiException e) {
            assertEquals(404, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_unknownFeefdbackType_return404() {
        try {
            userfeedbackApi.sendMail("default", "schalke-rating", recipients.toString(), new Long(0), new Long(0), "subject", "body");
            fail();
        } catch (ApiException e) {
            assertEquals(404, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_negativeStart_return404() {
        try {
            userfeedbackApi.sendMail("default", type, recipients.toString(), new Long(-11111), new Long(0), "subject", "body");
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_negativeEnd_return404() {
        try {
            userfeedbackApi.sendMail("default", type, recipients.toString(), new Long(0), new Long(-11111), "subject", "body");
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_endBeforeStart_return404() {
        try {
            userfeedbackApi.sendMail("default", type, recipients.toString(), new Long(222222222), new Long(11111), "subject", "body");
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }
}
