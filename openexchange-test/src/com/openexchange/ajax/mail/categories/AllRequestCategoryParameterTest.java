/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.mail.categories;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link AllRequestCategoryParameterTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class AllRequestCategoryParameterTest extends AbstractMailCategoriesTest {

    @Test
    public void testAllRequest() throws Exception {
        MailTestManager manager = new MailTestManager(getAjaxClient(), false);
        getAjaxClient().execute(new NewMailRequest(getInboxFolder(), EML, -1, true));
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
