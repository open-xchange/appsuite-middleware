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

package com.openexchange.file.storage;

import java.util.Date;

/**
 * {@link GenericMethodSupport}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GenericMethodSupport {

    protected <T> T get(final int i, @SuppressWarnings("unused") final Class<T> klass, final Object... args) {
        if (i >= args.length) {
            return null;
        }
        return (T) args[i];
    }

    protected File md(final int i, final Object... args) {
        return get(i, File.class, args);
    }

    protected File md(final Object... args) {
        return md(0, args);
    }

    protected String string(final int i, final Object... args) {
        if (args[i] == null || args.length <= i) {
            return null;
        }
        return args[i].toString();
    }

    protected int integer(final int i, final Object... args) {
        if (args[i] == null || args.length <= i) {
            return -1;
        }
        final Object o = args[i];
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return Integer.parseInt(o.toString());
    }

    /**
     * Parses a boolean value out of a specific element in the supplied generic parameter list.
     *
     * @param i The index of the element in the parameter list
     * @param args The generic parameter list
     * @return <code>true</code> if the <code>i</code>th element in the supplied parameter list is or can be parsed to {@link Boolean#TRUE}, <code>false</code>, otherwise
     */
    protected boolean bool(final int i, final Object... args) {
        if (null == args || i >= args.length || null == args[i]) {
            return false;
        }
        if (Boolean.class.isInstance(args[i])) {
            return ((Boolean) args[i]).booleanValue();
        }
        return Boolean.parseBoolean(String.valueOf(args[i]));
    }

    protected Date date(final int i, final Object... args) {
        if (args[i] == null || args.length <= i) {
            return null;
        }
        final Object o = args[i];

        if (o instanceof Date) {
            return (Date) o;
        }

        return new Date(coerceToLong(o));
    }

    protected long longValue(final int i, final Object... args) {
        if (args[i] == null || args.length <= i) {
            return -1;
        }
        return coerceToLong(args[i]);
    }

    private long coerceToLong(final Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        return Long.parseLong(o.toString());
    }

    protected double doubleValue(final int i, final Object... args) {
        if (args[i] == null || args.length <= i) {
            return -1;
        }
        return coerceToLong(args[i]);
    }
}
