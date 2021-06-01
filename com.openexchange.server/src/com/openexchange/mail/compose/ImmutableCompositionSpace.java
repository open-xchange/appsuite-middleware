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

package com.openexchange.mail.compose;

import java.util.Date;
import java.util.Optional;
import com.openexchange.mail.MailPath;

/**
 * {@link ImmutableCompositionSpace}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class ImmutableCompositionSpace implements CompositionSpace {

    private final CompositionSpaceId id;
    private final MailPath mailPath;
    private final Message message;
    private final long lastModified;
    private final ClientToken clientToken;

    /**
     * Initializes a new {@link ImmutableCompositionSpace}.
     *
     * @param id The composition space identifier
     * @param mailPath The optional mail path associated with composition space or <code>null</code>
     * @param message The message
     * @param lastModified The last-modified time stamp; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @param clientToken The client token
     */
    public ImmutableCompositionSpace(CompositionSpaceId id, MailPath mailPath, Message message, long lastModified, ClientToken clientToken) {
        super();
        this.id = id;
        this.mailPath = mailPath;
        this.message = message;
        this.lastModified = lastModified;
        this.clientToken = clientToken;
    }

    @Override
    public CompositionSpaceId getId() {
        return id;
    }

    @Override
    public Optional<MailPath> getMailPath() {
        return Optional.ofNullable(mailPath);
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public ClientToken getClientToken() {
        return clientToken;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (mailPath != null) {
            builder.append("mailPath=").append(mailPath).append(", ");
        }
        if (message != null) {
            builder.append("message=").append(message).append(", ");
        }
        builder.append("lastModified=").append(new Date(lastModified)).append(", ");
        builder.append("clientToken=").append(clientToken.toString()).append("]");
        return builder.toString();
    }

}
