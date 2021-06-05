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
 * {@link NoopStoredAttachmentsControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class NoopStoredAttachmentsControl implements StoredAttachmentsControl {

    private final List<Item> attachments;
    private final Item folder;
    private final ShareTarget folderTarget;

    /**
     * Initializes a new {@link NoopStoredAttachmentsControl}.
     *
     * @param attachments The stored attachments
     * @param folder The folder
     * @param folderTarget The share target pointing to the folder/directory containing the attachments
     */
    public NoopStoredAttachmentsControl(List<Item> attachments, Item folder, ShareTarget folderTarget) {
        super();
        this.attachments = attachments;
        this.folder = folder;
        this.folderTarget = folderTarget;
    }

    @Override
    public Item getFolder() {
        return folder;
    }

    @Override
    public List<Item> getAttachments() {
        return attachments;
    }

    @Override
    public ShareTarget getFolderTarget() {
        return folderTarget;
    }

    @Override
    public void commit() throws OXException {
        // Nothing
    }

    @Override
    public void rollback() throws OXException {
        // Nothing
    }

    @Override
    public void finish() throws OXException {
        // Nothing
    }

}
