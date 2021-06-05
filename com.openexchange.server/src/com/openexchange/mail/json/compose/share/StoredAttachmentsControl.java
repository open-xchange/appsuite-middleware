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
import com.openexchange.exception.OXException;
import com.openexchange.share.ShareTarget;

/**
 * {@link StoredAttachmentsControl} - The control for stored attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface StoredAttachmentsControl {

    /**
     * Gets the folder
     *
     * @return The folder
     */
    Item getFolder();

    /**
     * Gets the attachments
     *
     * @return The attachments
     */
    List<Item> getAttachments();

    /**
     * Gets the share target for the folder
     *
     * @return The share target for the folder
     */
    ShareTarget getFolderTarget();

    /**
     * Commits the attachment store operation.
     *
     * @throws OXException If commit fails
     */
    void commit() throws OXException;

    /**
     * Rolls-back the attachment store operation.
     *
     * @throws OXException If roll-back fails
     */
    void rollback() throws OXException;

    /**
     * Performs possible clean-up operations after a commit/roll-back.
     *
     * @throws OXException If clean-up fails
     */
    void finish() throws OXException;

}
