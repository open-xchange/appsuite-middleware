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

package com.openexchange.ajax.mailaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.MailAccountData;
import com.openexchange.testing.httpclient.models.MailAccountUpdateResponse;
import com.openexchange.testing.httpclient.models.MailAccountsResponse;
import com.openexchange.testing.httpclient.modules.MailaccountApi;

/**
 * {@link ChangePrimaryMailAccountNameTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ChangePrimaryMailAccountNameTest extends AbstractAPIClientSession {

    private MailaccountApi accountApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        accountApi = new MailaccountApi(getApiClient());
    }

    /**
     * Tests that the primary mail account name is changeable.
     *
     * @throws ApiException
     */
    @Test
    public void testUpdateOfPrimaryMailAccountName() throws ApiException {
        MailAccountData primAccount = getPrimaryAccount();
        String oldName = primAccount.getName();
        String newName = "name_"+System.currentTimeMillis();
        updateMailAccount(primAccount, newName);
        primAccount = getPrimaryAccount();
        assertEquals("The name didn't change but should have!", newName, primAccount.getName());
        // undo change
        updateMailAccount(primAccount, oldName);
    }

    private MailAccountData getPrimaryAccount() throws ApiException {
        final String columns = Attribute.ID_LITERAL.getId()+","+Attribute.NAME_LITERAL.getId();
        MailAccountsResponse response = accountApi.getAllAccounts(getSessionId(), columns);

        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> data = (ArrayList<ArrayList<Object>>) response.getData();

        for (final ArrayList<Object> account : data) {
            if (account.size()>0 && account.get(0).equals(0)) {
                MailAccountData result = new MailAccountData();
                result.setId((Integer) account.get(0));
                result.setName((String) account.get(1));
                return result;
            }
        }
        fail("Did not find the primary mail account in response");
        return null;
    }

    private void updateMailAccount(MailAccountData account, String newName) throws ApiException {
        account.setName(newName);
        MailAccountUpdateResponse updateAccount = accountApi.updateAccount(getSessionId(), account);
        assertNull(updateAccount.getErrorDesc(), updateAccount.getError());
        assertNotNull(updateAccount.getData());
    }

}
