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

package com.openexchange.client.onboarding;

/**
 * <a href="https://fortawesome.github.io/Font-Awesome/">Font Awesome</a> implementation of an {@link Icon}, based on a byte array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class FontAwesomeIcon implements Icon {

    private static final long serialVersionUID = 8201486116083192771L;

    private final String[] names;

    /**
     * Initializes a new {@link FontAwesomeIcon}.
     */
    public FontAwesomeIcon(String... names) {
        super();
        this.names = names;
    }

    /**
     * Gets the names for the <a href="https://fortawesome.github.io/Font-Awesome/">Font Awesome</a> images.
     *
     * @return The names
     */
    public String[] getNames() {
        return names;
    }

    @Override
    public String getMimeType() {
        return "image/svg+xml";
    }

    @Override
    public long getSize() {
        return -1L;
    }

    @Override
    public byte[] getData() {
        return null;
    }

    @Override
    public IconType getType() {
        return IconType.FONT_AWESOME;
    }

}
