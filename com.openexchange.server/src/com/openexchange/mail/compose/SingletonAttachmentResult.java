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

import java.util.Collections;
import java.util.List;

/**
 * {@link SingletonAttachmentResult} - The singleton attachment result implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class SingletonAttachmentResult extends AbstractAttachmentResult {

    private final Attachment attachment;

    /**
     * Initializes a new {@link SingletonAttachmentResult}.
     *
     * @param attachment The attachment as a result of the attachment-related operation
     * @param compositionSpaceInfo The composition space information
     * @throws IllegalArgumentException If attachment or composition space information is <code>null</code>
     */
    public SingletonAttachmentResult(Attachment attachment, CompositionSpaceInfo compositionSpaceInfo) {
        super(compositionSpaceInfo);
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment must not be null");
        }
        this.attachment = attachment;
    }

    @Override
    public List<? extends Attachment> getAttachments() {
        return Collections.singletonList(attachment);
    }

    @Override
    public Attachment getFirstAttachment() {
        return attachment;
    }

}
