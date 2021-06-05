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

import java.util.Comparator;

/**
 * {@link AttachmentComparator} - Ensures that {@link AttachmentOrigin#VCARD vCard} attachment is the last attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentComparator implements Comparator<Attachment> {

    private static final AttachmentComparator INSTANCE = new AttachmentComparator();

    /**
     * Gets the attachment comparator instance, which ensures that a possible {@link AttachmentOrigin#VCARD vCard} attachment is the last
     * attachment.
     *
     * @return The comparator instance
     */
    public static AttachmentComparator getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AttachmentComparator}.
     */
    private AttachmentComparator() {
        super();
    }

    @Override
    public int compare(Attachment a1, Attachment a2) {
        AttachmentOrigin origin1 = a1.getOrigin();
        AttachmentOrigin origin2 = a2.getOrigin();
        return origin1 == AttachmentOrigin.VCARD ? (origin2 == AttachmentOrigin.VCARD ? 0 : 1) : (origin2 == AttachmentOrigin.VCARD ? -1 : 0);
    }

}
