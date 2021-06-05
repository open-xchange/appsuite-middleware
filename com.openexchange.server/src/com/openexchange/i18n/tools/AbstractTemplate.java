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
import com.openexchange.i18n.tools.replacement.StringReplacement;

/**
 * This class implements the {@link #render(Locale, String...)} method by
 * mapping it to the {@link #render(Locale, RenderMap)} method and putting all
 * substitutions into a {@link RenderMap}.
 */
public abstract class AbstractTemplate implements Template {

    /**
     * Constructor for subclasses.
     */
    protected AbstractTemplate() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(final Locale locale, final String... substitutions) {
        if (substitutions.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide matching key value pairs");
        }
        final RenderMap m = new RenderMap();
        for (int i = 0; i < substitutions.length; i++) {
            m.put(new StringReplacement(TemplateToken.getByString(substitutions[i++]), substitutions[i]));
        }
        return render(locale, m);
    }
}
