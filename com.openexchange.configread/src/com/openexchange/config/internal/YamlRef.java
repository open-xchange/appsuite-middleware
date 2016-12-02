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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.config.internal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import com.openexchange.config.ConfigurationServices;

/**
 * {@link YamlRef} - A reference for a YAML file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class YamlRef {

    private final File yamlFile;
    private final long lastModified;
    private final byte[] checksum;
    private volatile Object value;

    /**
     * Initializes a new {@link YamlRef}.
     */
    public YamlRef(File yamlFile) {
        super();
        this.yamlFile = yamlFile;
        lastModified = yamlFile.lastModified();
        checksum = ConfigurationServices.getHash(yamlFile);
    }

    /**
     * Optionally gets the parsed YAML value from associated file if already initialized.
     *
     * @return The YAML value or <code>null</code>
     */
    public Object optValue() {
        return value;
    }

    /**
     * Gets the parsed YAML value from associated file
     *
     * @return The YAML value
     * @throws IllegalArgumentException If YAML value cannot be returned
     */
    public Object getValue() {
        Object tmp = value;
        if (null == tmp) {
            synchronized (this) {
                tmp = value;
                if (null == tmp) {
                    try {
                        tmp = ConfigurationServices.loadYamlFrom(yamlFile);
                        value = tmp;
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Failed to load YAML file '" + yamlFile + "'. Reason:" + e.getMessage(), e);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Sets the parsed YAML value if not yet initialized
     *
     * @param parsedValue The parsed value to set
     */
    public void setValueIfAbsent(Object parsedValue) {
        if (null == parsedValue) {
            return;
        }

        Object tmp = value;
        if (null == tmp) {
            synchronized (this) {
                tmp = value;
                if (null == tmp) {
                    value = parsedValue;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * 1 + (int) (lastModified ^ (lastModified >>> 32));
        result = prime * result + Arrays.hashCode(checksum);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YamlRef)) {
            return false;
        }
        YamlRef other = (YamlRef) obj;
        if (lastModified != other.lastModified) {
            return false;
        }
        if (!Arrays.equals(checksum, other.checksum)) {
            return false;
        }
        return true;
    }

}
