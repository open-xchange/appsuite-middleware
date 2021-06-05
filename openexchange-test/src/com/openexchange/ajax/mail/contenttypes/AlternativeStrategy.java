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

package com.openexchange.ajax.mail.contenttypes;

import org.json.JSONException;
import com.openexchange.ajax.mail.TestMail;

/**
 * {@link AlternativeStrategy} - sanitizes a test mail of content-type "alternative",
 * cloning the mail text as first attachment and so on.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AlternativeStrategy implements MailTypeStrategy {

    @Override
    public void sanitize(TestMail mail) throws JSONException {
        if (mail.getBody() == null && mail.getAttachment() != null) {
            String text = mail.getAttachment().get(0).getString("content");
            if (text.contains("<body>")) {
                text = text.split("<body>")[1];
                text = text.split("</body>")[0];
            }
            text = text.trim();
            mail.setBody(text);
            mail.setContentType(MailContentType.ALTERNATIVE.toString());
            //TODO: Create text as HTML attachment when in doubt?
        }
    }

    @Override
    public boolean isResponsibleFor(TestMail mail) {
        return MailContentType.ALTERNATIVE.toString().equalsIgnoreCase(mail.getContentType());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
