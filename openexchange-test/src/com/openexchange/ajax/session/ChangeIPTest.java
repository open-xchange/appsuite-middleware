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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.ChangeIPRequest;
import com.openexchange.ajax.session.actions.ChangeIPResponse;
import com.openexchange.ajax.session.actions.RefreshSecretRequest;
import com.openexchange.ajax.session.actions.RefreshSecretResponse;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link ChangeIPTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ChangeIPTest extends AbstractAJAXSession {

    private AJAXClient client;

    public ChangeIPTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    @Override
    protected void tearDown() throws Exception {
        client = null;
        super.tearDown();
    }

    public void testIPChange() throws Throwable {
        String ipAdress = "192.168.123.321";
        final ChangeIPRequest request1 = new ChangeIPRequest(ipAdress, false);
        final ChangeIPResponse response1 = client.execute(request1);
        assertFalse("Change IP response contains an exception.", response1.hasError());
        assertTrue("Change IP response contains wrong data.", response1.hasCorrectResponse());
        final RefreshSecretRequest request2 = new RefreshSecretRequest(false);
        final RefreshSecretResponse response2 = client.execute(request2);
        assertTrue("Refresh request should be denied because of wrong IP.", response2.hasError());
        final OXException e = response2.getException();
        assertEquals("Wrong exception message.", SessionExceptionCodes.WRONG_CLIENT_IP.getPrefix(), e.getPrefix());
        assertEquals("Wrong exception message.", Category.CATEGORY_PERMISSION_DENIED, e.getCategory());
        assertEquals("Wrong exception message.", SessionExceptionCodes.WRONG_CLIENT_IP.getNumber(), e.getCode());
    }
}
