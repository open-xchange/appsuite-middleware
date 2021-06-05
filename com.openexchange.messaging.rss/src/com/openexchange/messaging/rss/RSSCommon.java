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

package com.openexchange.messaging.rss;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.session.Session;


/**
 * {@link RSSCommon}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSCommon {

    protected Session session;
    protected int accountId;

    public RSSCommon(final int accountId, final Session session) {
        this.accountId = accountId;
        this.session = session;
    }

    protected static final String EMPTY = "";

    protected void checkFolder(final String folder) throws OXException {
        if (!EMPTY.equals(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(accountId),
                RSSMessagingService.ID,
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
    }
}
