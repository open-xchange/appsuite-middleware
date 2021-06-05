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

package com.openexchange.chronos.ical.ical4j.extensions;

import java.util.List;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.util.Strings;

/**
 * {@link Feature}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class Feature extends Parameter {

    public static final String PARAMETER_NAME = "FEATURE";

    private static final long serialVersionUID = 3422082158531021828L;

    private final List<String> values;

    /**
     * Initializes a new {@link Feature}.
     * 
     * @param values The parameter values
     */
    public Feature(List<String> values) {
        super(PARAMETER_NAME, ParameterFactoryImpl.getInstance());
        this.values = values;
    }

    @Override
    protected boolean isQuotable() {
        return false;
    }

    @Override
    public final String getValue() {
        if (null == values || values.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder().append(quoteAsNeeded(values.get(0)));
        for (int i = 1; i < values.size(); i++) {
            stringBuilder.append(',').append(quoteAsNeeded(values.get(i)));
        }
        return stringBuilder.toString();
    }

    private static String quoteAsNeeded(String value) {
        if (null == value) {
            return "";
        }
        if (Strings.PARAM_QUOTE_PATTERN.matcher(value).find()) {
            return Strings.quote(value);
        }
        return value;
    }

}
