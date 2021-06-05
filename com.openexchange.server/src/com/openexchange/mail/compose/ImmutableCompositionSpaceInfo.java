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

import java.util.Optional;
import com.openexchange.mail.MailPath;

/**
 * {@link ImmutableCompositionSpaceInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ImmutableCompositionSpaceInfo implements CompositionSpaceInfo {

    private final CompositionSpaceId id;
    private final MailPath mailPath;
    private long lastModified;

    /**
     * Initializes a new {@link ImmutableCompositionSpaceInfo}.
     *
     * @param id The composition space identifier
     * @param mailPath The optional mail path associated with composition space or <code>null</code>
     * @param lastModified The current draft mails last modified date/sequence
     */
    public ImmutableCompositionSpaceInfo(CompositionSpaceId id, MailPath mailPath, long lastModified) {
        super();
        this.id = id;
        this.mailPath = mailPath;
        this.lastModified = lastModified;
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
    public long getLastModified() {
        return lastModified;
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
        builder.append("lastModified=").append(lastModified).append("]");
        return builder.toString();
    }

}
