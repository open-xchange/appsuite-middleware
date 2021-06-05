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

package com.openexchange.mail.dataobjects;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;

/**
 * {@link MailStructure} - Represents a mail structure
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailStructure {

    private final ContentType contentType;

    private final List<MailStructure> subStructures;

    /**
     * Initializes a new {@link MailStructure}
     */
    public MailStructure() {
        super();
        contentType = new ContentType();
        subStructures = new ArrayList<MailStructure>(4);
    }

    /**
     * Initializes a new {@link MailStructure}
     *
     * @param contentType The content type
     */
    public MailStructure(ContentType contentType) {
        super();
        this.contentType = new ContentType();
        this.contentType.setContentType(contentType);
        subStructures = new ArrayList<MailStructure>(4);
    }

    /**
     * Gets the content type
     *
     * @return The content type
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Sets the content type
     *
     * @param contentType The content type
     */
    public void setContentType(ContentType contentType) {
        this.contentType.setContentType(contentType);
    }

    /**
     * Adds a sub-structure
     *
     * @param mailStructure The sub-structure to add
     */
    public void addSubStructure(MailStructure mailStructure) {
        subStructures.add(mailStructure);
    }

    /**
     * Gets this mail structure's sub-structures
     *
     * @return The sub-structures
     */
    public MailStructure[] getSubStructures() {
        return subStructures.toArray(new MailStructure[subStructures.size()]);
    }

    /**
     * Generates the corresponding mail structure for specified mail message.
     *
     * @param mail The mail message
     * @return The corresponding mail structure
     * @throws OXException If a mail error occurs
     */
    public static MailStructure getMailStructure(MailMessage mail) throws OXException {
        return getMailStructure0(mail);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(contentType.getBaseType().toString());
        final MailStructure[] subStructures = getSubStructures();
        if (subStructures.length > 0) {
            sb.append(" (").append(subStructures[0].toString());
            for (int i = 1; i < subStructures.length; i++) {
                sb.append(", ").append(subStructures[i].toString());
            }
            sb.append(')');
        }
        return sb.toString();
    }

    private static MailStructure getMailStructure0(MailPart part) throws OXException {
        final MailStructure retval;
        final ContentType ct = part.getContentType();
        if (ct.isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
            final MailStructure multi = new MailStructure(ct);
            final int count = part.getEnclosedCount();
            for (int i = 0; i < count; i++) {
                multi.addSubStructure(getMailStructure0(part.getEnclosedMailPart(i)));
            }
            retval = multi;
        } else if (ct.isMimeType(MimeTypes.MIME_MESSAGE_RFC822)) {
            final MailStructure nested = new MailStructure(ct);
            nested.addSubStructure(getMailStructure0((MailMessage) part.getContent()));
            retval = nested;
        } else {
            retval = new MailStructure(ct);
        }
        return retval;
    }
}
