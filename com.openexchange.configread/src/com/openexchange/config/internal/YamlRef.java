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

package com.openexchange.config.internal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.startup.StaticSignalStartedService;
import com.openexchange.startup.StaticSignalStartedService.State;

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
        try {
            checksum = ConfigurationServices.getHash(yamlFile);
        } catch (IllegalStateException e) {
            Throwable cause = e.getCause();
            String message = "Failed to load YAML file '" + yamlFile + "'. Reason: " + (null == cause ? e.getMessage() : cause.getMessage());
            StaticSignalStartedService.getInstance().setState(State.INVALID_CONFIGURATION, e, message);
            throw e;
        }
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
                        String message = "Failed to load YAML file '" + yamlFile + "'. Reason: " + e.getMessage();
                        StaticSignalStartedService.getInstance().setState(State.INVALID_CONFIGURATION, e, message);
                        throw new IllegalArgumentException(message, e);
                    } catch (RuntimeException e) {
                        StaticSignalStartedService.getInstance().setState(State.INVALID_CONFIGURATION, e, e.getMessage());
                        throw e;
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
