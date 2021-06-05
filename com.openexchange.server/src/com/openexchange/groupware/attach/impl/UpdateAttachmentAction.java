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

package com.openexchange.groupware.attach.impl;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class UpdateAttachmentAction extends AttachmentListQueryAction {

    private List<AttachmentMetadata> oldAttachments;

    @Override
    protected void undoAction() throws OXException {
        if (oldAttachments.size() == 0) {
            return;
        }
        try {
            doUpdates(getQueryCatalog().getUpdate(), oldAttachments, true);
        } catch (OXException e) {
            throw e;
        }
    }

    @Override
    public void perform() throws OXException {
        if (getAttachments().size() == 0) {
            return;
        }
        try {
            doUpdates(getQueryCatalog().getUpdate(), getAttachments(), true);
        } catch (OXException e) {
            throw e;
        }
    }

    public void setOldAttachments(final List<AttachmentMetadata> attachments) {
        this.oldAttachments = attachments;
    }
}
