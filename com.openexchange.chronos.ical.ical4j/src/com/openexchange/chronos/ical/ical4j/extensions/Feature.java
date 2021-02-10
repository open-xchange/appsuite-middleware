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
