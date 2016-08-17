/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
