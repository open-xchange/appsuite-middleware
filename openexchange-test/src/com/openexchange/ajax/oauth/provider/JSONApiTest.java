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

package com.openexchange.ajax.oauth.provider;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.oauth.provider.actions.AllRequest;
import com.openexchange.ajax.oauth.provider.actions.AllResponse;
import com.openexchange.ajax.oauth.provider.actions.RevokeRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantView;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * {@link JSONApiTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class JSONApiTest extends AbstractOAuthTest {

    /**
     * Initializes a new {@link JSONApiTest}.
     * @throws OXException
     */
    public JSONApiTest() throws OXException {
        super(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE)); // scope for first grant
    }

    @Test
    public void testAllAndRevoke() throws Exception {
        new OAuthClient(User.User1, clientApp.getId(), clientApp.getSecret(), clientApp.getRedirectURIs().get(0), Scope.newInstance(AppointmentActionFactory.OAUTH_READ_SCOPE, AppointmentActionFactory.OAUTH_WRITE_SCOPE));
        AllResponse allResponse = ajaxClient.execute(new AllRequest());
        List<GrantView> grantViews = allResponse.getGrantViews();
        GrantView expected = null;
        for (GrantView grant : grantViews) {
            if (grant.getClient().getId().equals(clientApp.getId())) {
                expected = grant;
                break;
            }
        }
        Assert.assertNotNull(expected);
        Assert.assertEquals(3, expected.getScope().size());

        // revoke access for application
        ajaxClient.execute(new RevokeRequest(expected.getClient().getId()));

        // assert it does not appear anymore in all response
        allResponse = ajaxClient.execute(new AllRequest());
        grantViews = allResponse.getGrantViews();
        for (GrantView grant : grantViews) {
            Assert.assertFalse(grant.getClient().getId().equals(clientApp.getId()));
        }
    }

}
