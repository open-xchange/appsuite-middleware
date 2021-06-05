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

package com.openexchange.tools.file;

import java.util.Map;

/**
 * This is an adapter for TagFiller. With this adapter you must not implement every method from interface TagFiller. You only have to
 * override methods that are usefull for you.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class TagFillerAdapter implements TagFiller {

    /**
     * This empty string will be returned in all replace methods.
     */
    private static final String EMPTY = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public String replace(final String tag) {
        return EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String replace(final String tag, final Object data) {
        return EMPTY;
    }

    /**
     * This tag filler overrides the method replace(String, Object). It expects a java.util.Map as second parameter. The tags will be
     * searched in the keys of the Map and the method returns the value the key is associated with.
     */
    public static final TagFiller MAP_TAG_FILLER = new TagFillerAdapter() {

        @Override
        public String replace(final String tag, final Object data) {
            String retval = EMPTY;
            if (data instanceof Map) {
                final Map replacements = (Map) data;
                if (replacements.containsKey(tag)) {
                    retval = replacements.get(tag).toString();
                }
            }
            return retval;
        }
    };
}
