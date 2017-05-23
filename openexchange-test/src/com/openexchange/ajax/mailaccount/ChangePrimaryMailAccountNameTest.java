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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountUpdateRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountUpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link ChangePrimaryMailAccountNameTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ChangePrimaryMailAccountNameTest extends AbstractMailAccountTest{

    /**
     * Tests that the primary mail account name is changeable.
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testUpdateOfPrimaryMailAccountName() throws OXException, IOException, JSONException{
        MailAccountDescription description = getPrimaryAccount();
        String oldName = description.getName();
        String newName = "name_"+System.currentTimeMillis();
        updateMailAccount(description, newName);
        description = getPrimaryAccount();
        assertEquals("The name didn't change but should have!", newName, description.getName());
        // undo change
        updateMailAccount(description, oldName);
    }

    private MailAccountDescription getPrimaryAccount() throws OXException, IOException, JSONException {
        final int[] fields = new int[]{Attribute.ID_LITERAL.getId(), Attribute.NAME_LITERAL.getId()};
        final MailAccountAllResponse response = getClient().execute(new MailAccountAllRequest(fields));

        final List<MailAccountDescription> descriptions = response.getDescriptions();
        assertFalse(descriptions.isEmpty());

        for (final MailAccountDescription description : descriptions) {
            if (description.getId() == 0) {
                return description;
            }
        }
        fail("Did not find the primary mail account in response");
        return null;
    }

    private void updateMailAccount(MailAccountDescription mailAccountDescription, String newName) throws OXException, IOException, JSONException {
        mailAccountDescription.setName(newName);
        MailAccountUpdateResponse response = getClient().execute(new MailAccountUpdateRequest(mailAccountDescription, EnumSet.of(Attribute.NAME_LITERAL)));
        assertFalse("The update operation failed. Message: "+response.getErrorMessage(), response.hasError());
    }

}
