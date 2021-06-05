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

package com.openexchange.ajax.mail;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.actions.DeleteRequest;

/**
 * {@link MailCleaner}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class MailCleaner {

    private TestMail mail;
    private AJAXClient client;

    public TestMail getMail() {
        return mail;
    }

    public void setMail(TestMail mail) {
        this.mail = mail;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    /**
     * Initializes a new {@link MailCleaner}.
     * 
     * @param mail
     * @param client
     */
    public MailCleaner(TestMail mail, AJAXClient client) {
        this.client = client;
        this.mail = mail;
    }

    public void cleanUp() throws Exception {
        if (null != mail) {
            DeleteRequest request = new DeleteRequest(mail, true);
            request.ignoreError();
            getClient().execute(request);
        }
    }

}
