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

package com.openexchange.ajax.mail.categories;

import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link AllRequestCategoryParameterTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class AllRequestCategoryParameterTest extends AbstractMailCategoriesTest {

    private static final int[] COLUMNS = new int[] { 102, 600, 601, 602, 603, 604, 605, 606, 607, 608, 610, 611, 614, 652 };

    /**
     * Initializes a new {@link AllRequestCategoryParameterTest}.
     * 
     * @param name
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    public AllRequestCategoryParameterTest(String name) throws OXException, IOException, JSONException {
        super(name);
    }

    @Test
    public void testAllRequest() throws Exception {

        MailTestManager manager = new MailTestManager(client, false);
        getClient().execute(new NewMailRequest(getInboxFolder(), EML, -1, true));
        String origin = values.getInboxFolder();
        MailMessage[] messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should contain the old mail.", messages.length == 1);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain no email.", messages.length == 0);

        TestMail mail = new TestMail(getFirstMailInFolder(origin));
        manager.moveToCategory(mail, CAT_1);

        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category shouldn't contain the mail any more.", messages.length == 0);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain the mail now.", messages.length == 1);
    }

}
