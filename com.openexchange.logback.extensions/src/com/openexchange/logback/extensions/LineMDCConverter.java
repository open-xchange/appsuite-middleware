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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.logback.extensions;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;
import java.util.Map;
import java.util.TreeMap;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * {@link LineMDCConverter} - Output each key-value-pair contained in MDC in a separate line.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LineMDCConverter extends MDCConverter {

    private String key;
    private String defaultValue = "";

    /**
     * Initializes a new {@link LineMDCConverter}.
     */
    public LineMDCConverter() {
        super();
    }

    @Override
    public void start() {
        final String[] keyInfo = extractDefaultReplacement(getFirstOption());
        key = keyInfo[0];
        if (keyInfo[1] != null) {
            defaultValue = keyInfo[1];
        }
        super.start();
    }

    @Override
    public void stop() {
        key = null;
        super.stop();
    }

    @Override
    public String convert(final ILoggingEvent event) {
        final Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

        if (mdcPropertyMap == null) {
            return defaultValue;
        }

        if (key == null) {
            return outputMDCForAllKeys(mdcPropertyMap);
        }

        final String value = event.getMDCPropertyMap().get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * if no key is specified, return all the values present in the MDC, in the format "k1=v1, k2=v2, ..."
     */
    private String outputMDCForAllKeys(final Map<String, String> mdcPropertyMap) {
        final String ls = System.getProperty("line.separator");
        final StringBuilder buf = new StringBuilder(1250);
        for (final Map.Entry<String, String> entry : new TreeMap<String, String>(mdcPropertyMap).entrySet()) {
            // format: key0=value0\nkey1=value1
            buf.append(' ').append(entry.getKey()).append('=').append(entry.getValue()).append(ls);
        }
        return buf.toString();
    }

}
