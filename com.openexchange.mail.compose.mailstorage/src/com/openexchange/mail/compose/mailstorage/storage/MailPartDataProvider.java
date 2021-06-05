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

package com.openexchange.mail.compose.mailstorage.storage;

import java.io.InputStream;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.dataobjects.MailPart;


/**
 * {@link MailPartDataProvider} - A data provider backed by a mail part.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailPartDataProvider implements DataProvider {

    private final MailPart part;

    /**
     * Initializes a new {@link MailPartDataProvider}.
     *
     * @param part The mail part
     */
    public MailPartDataProvider(MailPart part) {
        super();
        this.part = Objects.requireNonNull(part);
    }

    @Override
    public InputStream getData() throws OXException {
        return part.getInputStream();
    }

}
