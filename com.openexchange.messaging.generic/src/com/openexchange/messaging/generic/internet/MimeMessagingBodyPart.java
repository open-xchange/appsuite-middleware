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

package com.openexchange.messaging.generic.internet;

import javax.mail.internet.MimePart;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MultipartContent;

/**
 * {@link MimeMessagingBodyPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeMessagingBodyPart extends MimeMessagingPart implements MessagingBodyPart {

    /**
     * The multipart parent.
     */
    private MimeMultipartContent parent;

    /**
     * Initializes a new {@link MimeMessagingBodyPart}.
     */
    public MimeMessagingBodyPart() {
        super();
    }

    /**
     * Initializes a new {@link MimeMessagingBodyPart}.
     *
     * @param parent The multipart parent
     */
    public MimeMessagingBodyPart(final MimeMultipartContent parent) {
        super();
        this.parent = parent;
    }

    /**
     * Initializes a new {@link MimeMessagingBodyPart}.
     *
     * @param part The MIME body part
     * @param parent The multipart parent
     */
    protected MimeMessagingBodyPart(final MimePart part, final MimeMultipartContent parent) {
        super(part);
        this.parent = parent;
    }

    /**
     * Sets the multipart parent.
     *
     * @param parent The multipart parent to set
     */
    public void setParent(final MimeMultipartContent parent) {
        this.parent = parent;
    }

    @Override
    public MultipartContent getParent() throws OXException {
        return parent;
    }

}
