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

package com.openexchange.mail.json.compose.share;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link StoredAttachments} - The metadata for a stored attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class StoredAttachments {

    private final Item folder;
    private final List<Item> attachments;

    /**
     * Initializes a new {@link StoredAttachments}.
     *
     * @param folder The folder
     * @param attachments The attachments
     */
    public StoredAttachments(Item folder, List<Item> attachments) {
        super();
        this.folder = folder;
        this.attachments = attachments == null ? ImmutableList.of() : ImmutableList.copyOf(attachments);
    }

    /**
     * Gets the folder
     *
     * @return The folder
     */
    public Item getFolder() {
        return folder;
    }

    /**
     * Gets the attachments
     *
     * @return The attachments
     */
    public List<Item> getAttachments() {
        return attachments;
    }

}
