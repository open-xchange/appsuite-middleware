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

import java.util.List;

/**
 * {@link AttachmentResults} - A utility class for attachment result.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AttachmentResults {

    /**
     * Initializes a new {@link AttachmentResults}.
     */
    private AttachmentResults() {
        super();
    }

    /**
     * Creates an empty attachment result.
     *
     * @param compositionSpaceInfo The composition space information
     * @return The attachment result
     * @throws IllegalArgumentException If composition space information is <code>null</code>
     */
    public static AttachmentResult attachmentResultFor(CompositionSpaceInfo compositionSpaceInfo) {
        return new EmptyAttachmentResult(compositionSpaceInfo);
    }

    /**
     * Creates the appropriate attachment result for given attachment.
     *
     * @param attachment The attachment as a result of the attachment-related operation
     * @param compositionSpaceInfo The composition space information
     * @return The attachment result
     * @throws IllegalArgumentException If attachment or composition space information is <code>null</code>
     */
    public static AttachmentResult attachmentResultFor(Attachment attachment, CompositionSpaceInfo compositionSpaceInfo) {
        return new SingletonAttachmentResult(attachment, compositionSpaceInfo);
    }

    /**
     * Creates the appropriate attachment result for given attachments.
     *
     * @param attachments The attachments as a result of the attachment-related operation
     * @param compositionSpaceInfo The composition space information
     * @return The attachment result
     * @throws IllegalArgumentException If attachments are <code>null</code>/empty or given composition space information is <code>null</code>
     * @throws NullPointerException If given attachments contain a <code>null</code> element
     */
    public static AttachmentResult attachmentResultFor(List<? extends Attachment> attachments, CompositionSpaceInfo compositionSpaceInfo) {
        return new DefaultAttachmentResult(attachments, compositionSpaceInfo);
    }

}
