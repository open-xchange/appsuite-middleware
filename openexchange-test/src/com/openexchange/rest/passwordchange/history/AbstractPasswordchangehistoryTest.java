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

import static org.junit.Assert.assertFalse;
import java.sql.Timestamp;
import org.json.JSONObject;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.passwordchange.history.tracker.PasswordChangeInfo;
import com.openexchange.passwordchange.history.tracker.impl.PasswordChangeInfoImpl;
import com.openexchange.rest.AbstractRestTest;
import com.openexchange.testing.restclient.modules.PasswordchangehistoryApi;

/**
 * {@link AbstractPasswordchangehistoryTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class AbstractPasswordchangehistoryTest extends AbstractRestTest {

    protected static final String ARRAY_NAME = "PasswordChangeHistroy";

    protected PasswordchangehistoryApi pwdhapi;
    protected Long contextID;
    protected Long userID;
    protected Long limit = new Long(1);
    protected Timestamp send;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // API to operate on
        pwdhapi = new PasswordchangehistoryApi(getRestClient());

        // Do a password change
        PasswordChangeUpdateRequest request = new PasswordChangeUpdateRequest(testUser.getPassword(), testUser.getPassword(), true);
        send = new Timestamp(System.currentTimeMillis());
        PasswordChangeUpdateResponse response = getAjaxClient().execute(request);
        assertFalse("Errors in response!", response.hasError());
        assertFalse("Warnings in response!", response.hasWarnings());

        // Get context and user ID
        contextID = new Long(getAjaxClient().getValues().getContextId());
        userID = new Long(getAjaxClient().getValues().getUserId());
    }

    protected PasswordChangeInfo parse(JSONObject data) throws Exception {
        final String ip = filter(data, "ip");
        final String client = filter(data, "client");
        final Timestamp created = Timestamp.valueOf(filter(data, "created"));

        return new PasswordChangeInfoImpl(created, client, ip);
    }

    private String filter(JSONObject data, String name) throws Exception {
        return data.getString(name).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "");
    }
}
