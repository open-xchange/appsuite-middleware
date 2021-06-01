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
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link TrainTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class TrainTest extends AbstractMailCategoriesTest {

    @Test
    public void testTrain() throws Exception {
        MailTestManager manager = new MailTestManager(getAjaxClient(), false);
        getAjaxClient().execute(new NewMailRequest(getInboxFolder(), EML, -1, true));
        String origin = values.getInboxFolder();
        manager.trainCategory(CAT_1, false, true, getSendAddress());
        MailMessage[] messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should still contain the old email!", messages.length == 1);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain no email!", messages.length == 0);
        getAjaxClient().execute(new NewMailRequest(null, EML, -1, true));
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should still contain only the old email!", messages.length == 1);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should now contain the new mail!", messages.length == 1);
    }

    @Test
    public void testReorganize() throws Exception {
        MailTestManager manager = new MailTestManager(getAjaxClient(), false);
        getAjaxClient().execute(new NewMailRequest(getInboxFolder(), EML, -1, true));
        String origin = values.getInboxFolder();
        manager.trainCategory(CAT_1, true, false, getSendAddress());
        MailMessage[] messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should contain no mails now!", messages.length == 0);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain the mail now!", messages.length == 1);
    }

    @Test
    public void testDuplicateTrain() throws Exception {
        MailTestManager manager = new MailTestManager(getAjaxClient(), false);
        getAjaxClient().execute(new NewMailRequest(getInboxFolder(), EML, -1, true));
        String origin = values.getInboxFolder();
        manager.trainCategory(CAT_1, false, true, getSendAddress());
        manager.trainCategory(CAT_2, false, true, getSendAddress());
        MailMessage[] messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should still contain the old email!", messages.length == 1);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain no email!", messages.length == 0);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_2);
        assertTrue("Category 2 should contain no email!", messages.length == 0);
        getAjaxClient().execute(new NewMailRequest(null, EML, -1, true));
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_GENERAL);
        assertTrue("General category should still contain only the old email!", messages.length == 1);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_1);
        assertTrue("Category 1 should contain no email!", messages.length == 0);
        messages = manager.listMails(origin, COLUMNS, 610, Order.DESCENDING, true, CAT_2);
        assertTrue("Category 2 should contain the new mail!", messages.length == 1);
    }
}
