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

package com.openexchange.mail.compose.impl.storage.db;

import java.util.Date;
import java.util.UUID;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.MessageDescription;

/**
 * {@link CompositionSpaceContainer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CompositionSpaceContainer {

    /**
     * Initializes a new instance of <code>CompositionSpaceContainer</code> from given <code>CompositionSpaceDescription</code> instance.
     *
     * @param compositionSpaceDesc The <code>CompositionSpaceDescription</code> instance to initialize from
     * @return The resulting instance of <code>CompositionSpaceContainer</code>
     */
    public static CompositionSpaceContainer fromCompositionSpaceDescription(CompositionSpaceDescription compositionSpaceDesc) {
        CompositionSpaceContainer retval = new CompositionSpaceContainer();
        retval.setMessage(compositionSpaceDesc.getMessage());
        retval.setUuid(compositionSpaceDesc.getUuid());
        retval.setLastModified(compositionSpaceDesc.getLastModifiedDate());
        return retval;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private UUID uuid;
    private MessageDescription message;
    private Date lastModified;

    public CompositionSpaceContainer() {
        super();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public MessageDescription getMessage() {
        return message;
    }

    public void setMessage(MessageDescription message) {
        this.message = message;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
