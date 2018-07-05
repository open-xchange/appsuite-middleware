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

package com.openexchange.ajax.session;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.ChangeIPRequest;
import com.openexchange.ajax.session.actions.ChangeIPResponse;
import com.openexchange.ajax.session.actions.RefreshSecretRequest;
import com.openexchange.ajax.session.actions.RefreshSecretResponse;

/**
 * {@link ChangeIPTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ChangeIPTest extends AbstractAJAXSession {

    /**
     * Through noipcheck.cnf a whitelist for localhost is set. The server and the tests are run on the same machine.
     * Therefore the RefreshSecret request should 'reset' the ip address and should not fail.
     *
     * A 'negative' test can not be done. If you change the config for noipcheck.cnf the client can no longer log out.
     * After changing the IP the client needs to provide the new IP to log itself out or change the IP anew.
     * We can not set the IP of the AJAXClient. So there would be a resource leak that could interact with other tests.
     * So no 'negative' case testing ...
     *
     * @throws Throwable In case of failed test
     */
    @Test
    public void testIPChange() throws Throwable {
        String ipAdress = "192.168.123.321";
        final ChangeIPRequest request1 = new ChangeIPRequest(ipAdress, true);
        final ChangeIPResponse response1 = getClient().execute(request1);
        assertFalse("Change IP response contains an exception.", response1.hasError());
        assertTrue("Change IP response contains wrong data.", response1.hasCorrectResponse());
        final RefreshSecretRequest request2 = new RefreshSecretRequest(true);
        final RefreshSecretResponse response2 = getClient().execute(request2);
        assertFalse("Refresh request should pass, because localhost is inside whitelisted ip-range.", response2.hasError());
    }
}
