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

package com.openexchange.control.consoleold.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.openexchange.control.consoleold.ConsoleException;

/**
 * {@link ValueParser} - Parses passed command-line arguments.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ValueParser {

    final List<ValuePairObject> valuePairObjectList = new ArrayList<ValuePairObject>();

    final List<ValueObject> valueList = new ArrayList<ValueObject>();

    /**
     * Initializes a new {@link ValueParser}.
     *
     * @param args The command-line arguments.
     * @param parameter The names of those parameters which should be considered as name-value-pairs in specified command-line arguments.
     * @throws ConsoleException If parsing the passed arguments fails.
     */
    public ValueParser(final String[] args, final String[] parameter) throws ConsoleException {
        final HashSet<String> parameterSet = new HashSet<String>();

        for (int a = 0; a < parameter.length; a++) {
            parameterSet.add(parameter[a]);
        }

        for (int a = 0; a < args.length; a++) {
            final String param = args[a];
            if (parameterSet.contains(param)) {
                final ValuePairObject valuePairObject = parseValuePair(param, args, a);
                valuePairObjectList.add(valuePairObject);
                a++;
            } else {
                final ValueObject valueObject = new ValueObject(param);
                valueList.add(valueObject);
            }
        }
    }

    private ValuePairObject parseValuePair(final String name, final String[] args, final int pos) throws ConsoleException {
        if (pos >= args.length - 1) {
            throw new ConsoleException("missing value for parameter: " + name);
        }
        final String value = args[pos + 1];
        final ValuePairObject valuePairObject = new ValuePairObject(name, value);
        return valuePairObject;
    }

    /**
     * Gets the name-value-pair arguments.
     *
     * @return The name-value-pair arguments.
     */
    public ValuePairObject[] getValuePairObjects() {
        return valuePairObjectList.toArray(new ValuePairObject[valuePairObjectList.size()]);
    }

    /**
     * Gets the sole values without an associated name.
     *
     * @return The sole values without an associated name.
     */
    public ValueObject[] getValueObjects() {
        return valueList.toArray(new ValueObject[valueList.size()]);
    }
}
