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

package com.openexchange.ajax.requesthandler;


/**
 * {@link SecureContentWrapper} is a wrapper for result objects.
 *
 * If used in combination with "secureContentApiResponse" as the content type the response will be converted to a SecureContentResponse.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class SecureContentWrapper {

    public static final String CONTENT_TYPE="secureContentApiResponse";
    private final Object content;
    private final String format;


    /**
     * Initializes a new {@link SecureContentWrapper}.
     */
    public SecureContentWrapper(Object content, String format) {
        super();
        this.content=content;
        this.format=format;
    }

    /**
     * Gets the content
     *
     * @return The content
     */
    public Object getContent() {
        return content;
    }



    /**
     * Gets the format
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }

}
