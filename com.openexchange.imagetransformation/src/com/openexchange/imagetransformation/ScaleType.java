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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.imagetransformation;

import com.openexchange.java.Strings;

/**
 * {@link ScaleType}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public enum ScaleType {

    /**
     * The "cover" scale type, specifying the minimum target dimensions. The source image will be resized in a way that the resulting
     * image covers the target resolution entirely, with the original aspect ratio being preserved.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "cover", will result in the
     * picture being resized to 267x200 pixels.
     */
    COVER("cover"),

    /**
     * The "contain" scale type, specifying the maximum target dimensions. The source image will be resized in a way that the resulting
     * image fits into the target resolution entirely, with the original aspect ratio being preserved.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "contain", will result in the
     * picture being resized to 200x150 pixels.
     */
    CONTAIN("contain"),

    /**
     * The "containForceDimension" scale type, specifying the maximum target dimensions. The source image will be resized in a way that the resulting
     * image fits into the target resolution entirely, with the original aspect ratio being preserved while smaller sides get padded to fit exact dimension.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "contain", will result in the
     * picture being first resized to 200x150 pixels, then height gets padded by 25 pixels per side resulting in exactly 200x200 pixels.
     */
    CONTAIN_FORCE_DIMENSION("containforcedimension"),

    /**
     * The "auto" scale type
     */
    AUTO("auto");

    private final String keyword;

    private ScaleType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Gets the keyword
     *
     * @return The keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Gets the scale type for given keyword.
     *
     * @param keyword The keyword
     * @return The associated scale type or <code>null</code>
     */
    public static ScaleType getType(String keyword) {
        if (keyword == null) {
            return AUTO;
        }
        keyword = Strings.asciiLowerCase(keyword.trim());
        if (keyword.equals(COVER.getKeyword())) {
            return COVER;
        } else if (keyword.equals(CONTAIN.getKeyword())) {
            return CONTAIN;
        } else if (keyword.equals(CONTAIN_FORCE_DIMENSION.getKeyword())) {
            return CONTAIN_FORCE_DIMENSION;
        } else {
            return AUTO;
        }
    }
}
