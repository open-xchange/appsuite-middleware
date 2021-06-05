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

package com.openexchange.proxy;

import java.util.regex.Pattern;

/**
 * {@link ImageContentTypeRestriction} - A {@link ContentTypeRestriction} for images. <code>"Content-Type"</code> header must match pattern
 * <code>"image/*"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageContentTypeRestriction extends ContentTypeRestriction {

    private static final ImageContentTypeRestriction INSTANCE = new ImageContentTypeRestriction();

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     */
    public static ImageContentTypeRestriction getInstance() {
        return INSTANCE;
    }

    /**
     * The regex pattern for <code>"image/*"</code>.
     */
    private final Pattern pattern;

    /**
     * Initializes a new {@link ImageContentTypeRestriction}.
     */
    private ImageContentTypeRestriction() {
        super();
        pattern = Pattern.compile(wildcardToRegex("image/*"));
    }

    @Override
    public boolean allow(final Response response) {
        final Header header = response.getResponseHeader(CONTENT_TYPE);
        final String lcValue;
        {
            final String value = header.getValue();
            if (null == value) {
                /*
                 * Content-Type header missing
                 */
                return false;
            }
            lcValue = com.openexchange.java.Strings.toLowerCase(value).trim();
        }
        if (pattern.matcher(lcValue).matches()) {
            return true;
        }
        /*
         * No match found
         */
        return false;
    }

    @Override
    public String getDescription() {
        return "Content-Type header must match: image/*";
    }
}
