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

package com.openexchange.imageconverter.api;

/**
 * {@link ImageConverterPriority}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public enum ImageConverterPriority {

    /**
     * The BACKGROUND priority.
     * The lowest priority and the default one.
     */
    BACKGROUND,

    /**
     * The MEDIUM priority
     * A standard priority, that is a higher priority than BACKGROUND, but not urgent one
     */
    MEDIUM,


    /**
     * The INSTANT priority
     * The highest priority, that is processed preferable to all other priorities
     */
    INSTANT;

    /**
     * @return The lowest available priority
     */
    public static ImageConverterPriority lowest() {
        return BACKGROUND;
    }

    /**
     * @return The highest available priority
     */
    public static ImageConverterPriority highest() {
        return ImageConverterPriority.INSTANT;
    }

    /**
     * @param The priority string to create the {@link ImageConverterPriority} enum from
     * @return
     */
    public static ImageConverterPriority createFrom(final String priorityName) {
        if (null != priorityName) {
            final String lowerPriorityName = priorityName.toLowerCase();

            return lowerPriorityName.equals("instant") ?
                ImageConverterPriority.INSTANT :
                    (lowerPriorityName.equals("medium") ? MEDIUM : BACKGROUND);

        }

        return BACKGROUND;
    }
}
