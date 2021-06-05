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

package com.openexchange.fragment.properties.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import com.openexchange.fragment.properties.loader.FragmentPropertiesLoader;
import com.openexchange.java.Streams;

/**
 * 
 * {@link PropertiesLoader}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class PropertiesLoader implements FragmentPropertiesLoader {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertiesLoader.class);
    }

    public PropertiesLoader() {
        super();
    }

    @Override
    public Properties load(String name) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(name);
            if (null == is) {
                LoggerHolder.LOG.debug("Could not load property file {} from fragment.", name);
            } else {
                Properties props = new Properties();
                props.load(is);
                return props;
            }
        } catch (IOException e) {
            LoggerHolder.LOG.error("", e);
        } finally {
            Streams.close(is);
        }
        return null;
    }

    @Override
    public InputStream loadResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

}
