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

package com.openexchange.messaging.twitter;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.twitter.Status;

/**
 * {@link TwitterMultipartContent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMultipartContent implements MultipartContent {

    /**
     * Gets a newly created instance of {@link TwitterMultipartContent}.
     *
     * @param status The twitter status to turn into a multipart/alternative content
     * @return A newly created instance of {@link TwitterMultipartContent}
     */
    public static TwitterMultipartContent newInstance(final Status status) throws OXException {
        final TwitterMultipartContent tmp = new TwitterMultipartContent();
        tmp.parts[0] = new TwitterMessagingBodyPart(status, false, tmp);
        tmp.parts[1] = new TwitterMessagingBodyPart(status, true, tmp);
        return tmp;
    }

    private final MessagingBodyPart[] parts;

    private TwitterMultipartContent() {
        super();
        parts = new MessagingBodyPart[2];
    }

    @Override
    public int getCount() {
        return parts.length;
    }

    @Override
    public MessagingBodyPart get(final int index) {
        return parts[index];
    }

    @Override
    public String getSubType() {
        return "alternative";
    }

}
