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

package com.openexchange.ajax.framework;

import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.test.AttachmentTestManager;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.ReminderTestManager;
import com.openexchange.test.ResourceTestManager;
import com.openexchange.test.TaskTestManager;

public abstract class AbstractAJAXSession extends AbstractClientSession {

    protected CalendarTestManager catm;
    protected ContactTestManager cotm;
    protected FolderTestManager ftm;
    protected InfostoreTestManager itm;
    protected TaskTestManager ttm;
    protected ReminderTestManager remTm;
    protected ResourceTestManager resTm;
    protected AttachmentTestManager atm;
    protected MailTestManager mtm;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    protected AbstractAJAXSession() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        AJAXClient client = getClient();
        catm = new CalendarTestManager(client);
        cotm = new ContactTestManager(client);
        ftm = new FolderTestManager(client);
        itm = new InfostoreTestManager(client);
        ttm = new TaskTestManager(client);
        atm = new AttachmentTestManager(client);
        mtm = new MailTestManager(client);
        resTm = new ResourceTestManager(client);
        remTm = new ReminderTestManager(client);
    }

}
