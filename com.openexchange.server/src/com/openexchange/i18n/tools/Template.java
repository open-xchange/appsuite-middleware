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

package com.openexchange.i18n.tools;

import java.util.Locale;

/**
 * A template is some text with place holders that are filled using the
 * RenderMap or the substitutions.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Template {

    /**
     * Renders this template with given render map
     *
     * @param renderMap The render map
     * @param locale The template will be rendered in the given locale.
     * @return The rendered template
     */
    String render(Locale locale, RenderMap renderMap);

    /**
     * Renders this template with given string array
     *
     * @param substitutions a string array with the replacements in the order of
     *            place holders.
     * @param locale The template will be rendered in the given locale.
     * @return The rendered template
     */
    String render(Locale locale, String... substitutions);
}
