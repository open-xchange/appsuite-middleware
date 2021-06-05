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

package com.openexchange.mail.mime.processing;

import com.openexchange.mail.mime.ContentType;

/**
 * {@link TextAndContentType} - Combines a text and content-type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class TextAndContentType {

    private final String text;
    private final ContentType contentType;
    private final boolean html;

    /**
     * Initializes a new {@link TextAndContentType}.
     *
     * @param text The text
     * @param contentType The text's content-type
     * @param html Whether text content is considered to be HTML content
     */
    public TextAndContentType(String text, ContentType contentType, boolean html) {
        super();
        this.text = text;
        this.contentType = contentType;
        this.html = html;
    }

    /**
     * Gets the text
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the content-type
     *
     * @return The content-type
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Checks whether text content is considered to be HTML content
     *
     * @return <code>true</code> if text content is considered to be HTML content; otherwise <code>false</code>
     */
    public boolean isHtml() {
        return html;
    }

}
