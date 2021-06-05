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
 * {@link MailTypeStrategy}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public interface MailTypeStrategy {

    /**
     * Set the values of a given mail so that it conforms to one of the content types of mail.
     *
     * @param mail Mail that is going to be changed
     */
    public void sanitize(TestMail mail) throws JSONException;

    /**
     * checks whether this strategy is responsible for this mail, usually be checking the content type.
     *
     * @param mail Mail that is going to be changed
     * @return true if so, false otherwise
     */
    public boolean isResponsibleFor(TestMail mail) throws JSONException;
}
