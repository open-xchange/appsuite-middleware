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

package com.openexchange.file.storage.search;

import static com.openexchange.java.Strings.toLowerCase;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.java.Strings;

/**
 * {@link MetaTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class MetaTerm implements SearchTerm<String> {

    private final String pattern;

    /**
     * Initializes a new {@link MetaTerm}.
     */
    public MetaTerm(final String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void visit(final SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(final Collection<Field> col) {
        if (null != col) {
            col.add(Field.META);
        }
    }

    @Override
    public boolean matches(final File file) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return false;
        }

        final Map<String, Object> meta = file.getMeta();
        if (null == meta || meta.isEmpty()) {
            return false;
        }

        return lookUpMap(toLowerCase(pattern), meta);
    }

    private boolean lookUpMap(final String lookUp, final Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return false;
        }

        for (final Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            if (toLowerCase(key).indexOf(lookUp) >= 0) {
                return true;
            }

            if (lookUpObject(lookUp, entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    private boolean lookUpCollection(final String lookUp, final Collection<Object> col) {
        if (null == col || col.isEmpty()) {
            return false;
        }

        for (final Object object : col) {
            if (lookUpObject(lookUp, object)) {
                return true;
            }
        }

        return false;
    }

    private boolean lookUpObject(final String lookUp, final Object value) {
        if (value instanceof Map) {
            return lookUpMap(lookUp, (Map<String, Object>) value);
        }
        if (value instanceof Collection) {
            return lookUpCollection(lookUp, (Collection<Object>) value);
        }
        if (toLowerCase(value.toString()).indexOf(lookUp) >= 0) {
            return true;
        }
        return false;
    }

}
