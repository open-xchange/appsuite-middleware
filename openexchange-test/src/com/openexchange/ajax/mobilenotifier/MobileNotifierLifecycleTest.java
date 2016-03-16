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

package com.openexchange.ajax.mobilenotifier;

import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierSubscribeRequest;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierUnsubscribeRequest;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierUpdateTokenRequest;


/**
 * {@link MobileNotifierLifecycleTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierLifecycleTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link MobileNotifierLifecycleTest}.
     * @param name
     */
    public MobileNotifierLifecycleTest(String name) {
        super(name);
    }

    public void testSubscriptionLifecycle() throws Exception {
        String startToken = "ADE9219010FD3DEAD9211384129";
        String serviceId = "gcm";
        String providerId = "mail";
        String newToken = "ACCCDDDDEEEAAA000111155";

        MobileNotifierSubscribeRequest msReq = new MobileNotifierSubscribeRequest(serviceId, providerId, startToken, true);
        AbstractAJAXResponse msResp = client.execute(msReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

        MobileNotifierUpdateTokenRequest utReq = new MobileNotifierUpdateTokenRequest(serviceId, startToken, newToken, true);
        msResp = null;
        msResp = client.execute(utReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

        MobileNotifierUnsubscribeRequest musReq = new MobileNotifierUnsubscribeRequest(serviceId, providerId, newToken, true);
        msResp = null;
        msResp = client.execute(musReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

    }

    private void assertEmptyJson(AbstractAJAXResponse resp) {
        if(resp.getData() instanceof JSONObject) {
            JSONObject jObj = (JSONObject) resp.getData();
            assertTrue("Returning JSON data is not empty: " + jObj.toString(), jObj.isEmpty());
        } else {
            assertFalse("Returning data is not a json response", true);
        }

    }
}
