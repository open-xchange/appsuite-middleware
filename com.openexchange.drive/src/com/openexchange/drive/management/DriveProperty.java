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

package com.openexchange.drive.management;

import java.util.Arrays;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;

/**
 * {@link DriveProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DriveProperty implements PropertyListener {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DriveProperty.class));

    protected final String propertyName;
    protected final String defaultValue;
    protected final boolean listenForChanges;

    /**
     * Initializes a new {@link DriveProperty}.
     *
     * @param propertyName
     * @param defaultValue
     * @param listenForChanges
     */
    public DriveProperty(String propertyName, String defaultValue, boolean listenForChanges) {
        super();
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
        this.listenForChanges = listenForChanges;
    }

    /**
     * Registers this property with the configuration service.
     *
     * @param configService The config service
     */
    public void register(ConfigurationService configService) {
        if (listenForChanges) {
            apply(configService.getProperty(propertyName, defaultValue, this));
        } else {
            apply(configService.getProperty(propertyName, defaultValue));
        }
    }

    /**
     * Unregisters this property from the configuration service.
     *
     * @param configService The config service
     */
    public void unregister(ConfigurationService configService) {
        if (listenForChanges) {
            configService.removePropertyListener(propertyName, this);
        }
    }

    private void apply(String value) {
        try {
            set(value);
        } catch (RuntimeException e) {
            LOG.warn("Error applying value \"" + value + "\" for \"" + propertyName + "\", falling back to defaults.", e);
            try {
                set(defaultValue);
            } catch (RuntimeException x) {
                LOG.error(x);
            }
        }
    }

    @Override
    public void onPropertyChange(PropertyEvent event) {
        apply(event.getValue());
    }

    /**
     * Sets the property value.
     *
     * @param value The value in it's string representation
     * @throws OXException
     */
    protected abstract void set(String value);

    /**
     * Parses a byte value including an optional unit.
     *
     * @param value the value to parse
     * @return The parsed number of bytes
     * @throws NumberFormatException If the supplied string is not parsable or greater then <code>Integer.MAX_VALUE</code>
     */
    protected static int parseBytes(String value) throws NumberFormatException {
        StringAllocator numberAllocator = new StringAllocator(8);
        StringAllocator unitAllocator = new StringAllocator(4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c) || '.' == c || '-' == c) {
                numberAllocator.append(c);
            } else if (false == Character.isWhitespace(c)) {
                unitAllocator.append(c);
            }
        }
        double number = Double.parseDouble(numberAllocator.toString());
        if (0 < unitAllocator.length()) {
            String unit = unitAllocator.toString().toUpperCase();
            int exp = Arrays.asList("B", "KB", "MB", "GB").indexOf(unit);
            if (0 <= exp) {
                number *= Math.pow(1024, exp);
            } else {
                throw new NumberFormatException(value);
            }
        }
        if (Integer.MAX_VALUE >= number) {
            return (int)number;
        }
        throw new NumberFormatException(value);
    }

    protected static int[] parseDimensions(String value) {
        int idx = value.indexOf('x');
        if (1 > idx) {
            throw new IllegalArgumentException(value);
        }
        return new int[] { Integer.parseInt(value.substring(0, idx)), Integer.parseInt(value.substring(idx + 1)) };
    }

}
