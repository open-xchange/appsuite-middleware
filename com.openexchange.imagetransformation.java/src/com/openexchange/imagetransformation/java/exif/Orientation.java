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

package com.openexchange.imagetransformation.java.exif;

/**
 * {@link Orientation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public enum Orientation  {

    /**
     * Value <code>1</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 888888
     * 88
     * 8888
     * 88
     * 88
     * </pre>
     */
    TOP_LEFT(1),

    /**
     * Value <code>2</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 888888
     *     88
     *   8888
     *     88
     *     88
     * </pre>
     */
    TOP_RIGHT(2),

    /**
     * Value <code>3</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     *     88
     *     88
     *   8888
     *     88
     * 888888
     * </pre>
     */
    BOTTOM_RIGHT(3),

    /**
     * Value <code>4</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 88
     * 88
     * 8888
     * 88
     * 888888
     * </pre>
     */
    BOTTOM_LEFT(4),

    /**
     * Value <code>5</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 8888888888
     * 88  88
     * 88
     * </pre>
     */
    LEFT_TOP(5),

    /**
     * Value <code>6</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 88
     * 88  88
     * 8888888888
     * </pre>
     */
    RIGHT_TOP(6),

    /**
     * Value <code>7</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     *         88
     *     88  88
     * 8888888888
     * </pre>
     */
    RIGHT_BOTTOM(7),

    /**
     * Value <code>8</code>; the letter <code>F</code> in the stored image would look like:
     * <p/>
     * <pre>
     * 8888888888
     *     88  88
     *         88
     * </pre>
     */
    LEFT_BOTTOM(8),
    ;

    /**
     * Gets the orientation for a specific value as defined in Exif data.
     *
     * @param value The value to get the orientation for
     * @return The orientation
     * @throws IllegalArgumentException for unknown values
     */
    public static Orientation valueOf(int value) {
        for (Orientation orientation : Orientation.values()) {
            if (orientation.getValue() == value) {
                return orientation;
            }
        }
        throw new IllegalArgumentException("Unknown orienatation value: " + value);
    }

    private final int value;

    /**
     * Initializes a new {@link Orientation}.
     *
     * @param value
     */
    private Orientation(int value) {
        this.value = value;
    }

    /**
     * Gets the Exif oriantation value.
     *
     * @return The value
     */
    public int getValue() {
        return value;
    }

}
