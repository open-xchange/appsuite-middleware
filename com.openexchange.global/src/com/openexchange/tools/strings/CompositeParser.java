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

package com.openexchange.tools.strings;

import java.util.Collection;


/**
 * {@link CompositeParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class CompositeParser implements StringParser {

    @Override
    public <T> T parse(String s, Class<T> t) {
        if (s == null) {
            return null;
        }
        Collection<StringParser> parsers = getParsers();
        for (StringParser stringParser : parsers) {
            T value = stringParser.parse(s, t);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    protected abstract Collection<StringParser> getParsers();

}
