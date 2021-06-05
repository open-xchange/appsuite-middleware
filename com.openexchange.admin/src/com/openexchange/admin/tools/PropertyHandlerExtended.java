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
package com.openexchange.admin.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class PropertyHandlerExtended extends PropertyHandler {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyHandlerExtended.class);

    // The following lines define the property values for the database implementations
    public static final String CONTEXT_STORAGE = "CONTEXT_STORAGE";
    public static final String UTIL_STORAGE = "UTIL_STORAGE";

    private PropertyHandlerExtended() {
        super(null);
    }

    public PropertyHandlerExtended(final Properties sysprops) {
        super(sysprops);
        final StringBuilder configfile = new StringBuilder();
        configfile.append(sysprops.getProperty("openexchange.propdir"));
        configfile.append(File.separatorChar);
        configfile.append("plugin");
        configfile.append(File.separatorChar);
        configfile.append("hosting.properties");
        try {
            addpropsfromfile(configfile.toString());
        } catch (FileNotFoundException e) {
            log.error("Unable to read file: {}", configfile);
        } catch (IOException e) {
            log.error("Problems reading file: {}", configfile);
        }
    }

}
