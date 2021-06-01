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

import java.sql.SQLException;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class DeleteAttachmentAction extends AttachmentListQueryAction {

    @Override
    protected void undoAction() throws OXException {
        if (getAttachments().size() == 0) {
            return;
        }
        try {
            doUpdates(new Update(getQueryCatalog().getDelete("del_attachment", getAttachments())) {
                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                }
            });
            doUpdates(getQueryCatalog().getInsert(), getAttachments(), false);
        } catch (OXException e) {
            throw e;
        }
    }

    @Override
    public void perform() throws OXException {
        if (getAttachments().size() == 0) {
            return;
        }
        final Date delDate = new Date();
        final UpdateBlock[] updates = new UpdateBlock[getAttachments().size() + 1];
        int i = 0;
        for (final AttachmentMetadata m : getAttachments()) {
            updates[i++] = new Update(getQueryCatalog().getInsertIntoDel()) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, m.getId());
                    stmt.setLong(2, delDate.getTime());
                    stmt.setInt(3, getContext().getContextId());
                    stmt.setInt(4, m.getAttachedId());
                    stmt.setInt(5, m.getModuleId());
                }

            };
        }
        updates[i++] = new Update(getQueryCatalog().getDelete("prg_attachment", getAttachments())) {
            @Override
            public void fillStatement() throws SQLException {
                stmt.setInt(1, getContext().getContextId());
            }
        };
        try {
            doUpdates(updates);
        } catch (OXException e) {
            throw e;
        }
    }
}
