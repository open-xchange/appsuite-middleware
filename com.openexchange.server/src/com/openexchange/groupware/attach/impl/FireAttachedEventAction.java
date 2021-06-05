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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class FireAttachedEventAction extends AttachmentEventAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FireAttachedEventAction.class);

    @Override
    protected void undoAction() throws OXException {
        try {
            fireDetached(getAttachments(), getUser(), getUserConfiguration(), getSession(), getContext(), getProvider());
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw AttachmentExceptionCodes.DETACH_FAILED.create(e);
        }
    }

    @Override
    public void perform() throws OXException {
        final List<AttachmentMetadata> processed = new ArrayList<AttachmentMetadata>();
        try {
            fireAttached(getAttachments(), processed, getUser(), getUserConfiguration(), getSession(), getContext(), getProvider());
        } catch (Exception e) {
            LOG.error("", e);
            try {
                fireDetached(processed, getUser(), getUserConfiguration(), getSession(), getContext(), getProvider());
            } catch (Exception e1) {
                LOG.error("", e);
                throw AttachmentExceptionCodes.UNDONE_FAILED.create(e1);
            }
            if (e instanceof OXException) {
                final OXException aoe = (OXException) e;
                throw aoe;
            }

            throw AttachmentExceptionCodes.ATTACH_FAILED.create(e);
        }
    }
}
