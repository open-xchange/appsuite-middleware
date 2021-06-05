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
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.tools.service.ServicePriorityConflictException;
import com.openexchange.tools.service.SpecificServiceChooser;

/**
 * {@link OverridableAttachmentListener}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OverridableAttachmentListener implements AttachmentListener {

    private final SpecificServiceChooser<AttachmentListener> chooser;

    public OverridableAttachmentListener(SpecificServiceChooser<AttachmentListener> chooser) {
        this.chooser = chooser;
    }

    @Override
    public long attached(AttachmentEvent e) throws Exception {
        AttachmentListener delegate = getDelegate(e);
        return delegate == null ? -1 : delegate.attached(e);
    }

    @Override
    public long detached(AttachmentEvent e) throws Exception {
        AttachmentListener delegate = getDelegate(e);
        return delegate == null ? -1 : delegate.detached(e);
    }

    private AttachmentListener getDelegate(AttachmentEvent e) throws OXException {
        int contextId = e.getContext().getContextId();
        int folderId = e.getFolderId();
        try {
            return chooser.choose(contextId, folderId);
        } catch (ServicePriorityConflictException e1) {
            throw AttachmentExceptionCodes.SERVICE_CONFLICT.create(I(contextId), I(folderId));
        }
    }
}
