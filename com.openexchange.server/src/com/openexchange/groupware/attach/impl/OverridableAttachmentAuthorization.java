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

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.service.ServicePriorityConflictException;
import com.openexchange.tools.service.SpecificServiceChooser;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OverridableAttachmentAuthorization}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OverridableAttachmentAuthorization implements AttachmentAuthorization {

    private final SpecificServiceChooser<AttachmentAuthorization> chooser;

    public OverridableAttachmentAuthorization(SpecificServiceChooser<AttachmentAuthorization> chooser) {
        super();
        this.chooser = chooser;
    }

    @Override
    public void checkMayAttach(ServerSession session, int folderId, int objectId) throws OXException {
        getDelegate(folderId, session.getContextId()).checkMayAttach(session, folderId, objectId);
    }

    @Override
    public void checkMayDetach(ServerSession session, int folderId, int objectId) throws OXException {
        getDelegate(folderId, session.getContextId()).checkMayDetach(session, folderId, objectId);
    }

    @Override
    public void checkMayReadAttachments(ServerSession session, int folderId, int objectId) throws OXException {
        getDelegate(folderId, session.getContextId()).checkMayReadAttachments(session, folderId, objectId);
    }

    private AttachmentAuthorization getDelegate(int folderId, int contextId) throws OXException {
        try {
            AttachmentAuthorization attachmentAuthorization = chooser.choose(contextId, folderId);
            if (null == attachmentAuthorization) {
                throw ServiceExceptionCode.absentService(AttachmentAuthorization.class);
            }
            return attachmentAuthorization;
        } catch (ServicePriorityConflictException e) {
            throw AttachmentExceptionCodes.SERVICE_CONFLICT.create(I(contextId), I(folderId));
        }
    }
}
