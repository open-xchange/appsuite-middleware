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

package com.openexchange.security.manager.configurationReader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;

/**
 * {@link ConfigurationFileParser} Loads all of the configurations in the security-manager.list that
 * will require directory access
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class ConfigurationFileParser {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationFileParser.class);
    }

    private static final String SECURITY_FILE_SUFFIX = ".list";
    private static final String SECURITY_FOLDER = "security";

    private final ConfigurationService configService;

    /**
     * Initializes a new {@link ConfigurationFileParser}.
     *
     * @param configService The configuration service to use
     */
    public ConfigurationFileParser(ConfigurationService configService) {
        super();
        this.configService = configService;
    }

    private static final Pattern SPLIT = Pattern.compile("\r?\n");

    /**
     * Reads the data from a file, add each valid line to list.
     *
     * @param file File to read
     * @param list List to store valid lines
     */
    private void parseFile(File file, List<String> list) {
        try {
            String data = ConfigurationServices.readFile(file);
            if (data == null) {
                return;
            }

            String[] lines = SPLIT.split(data, 0);
            for (String line : lines) {
                line = line.trim();
                if (line.indexOf("#") != 0 && !line.isEmpty()) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            LoggerHolder.LOG.error("Can't read file: {}", file, e);
            return;
        }
    }

    /**
     * Reads through the security directory and returns a listing of configuration options.
     *
     * @return Listing of configurations found in the files
     */
    public List<String> getConfigList() {
        File folder = configService.getDirectory(SECURITY_FOLDER);
        if (null == folder || folder.exists() == false || folder.isDirectory() == false) {
            return Collections.emptyList();
        }

        File[] files = folder.listFiles();
        if (null == files) {
            return Collections.emptyList();
        }

        List<String> list = new LinkedList<>();
        for (File file : files) {
            if (file.getName().endsWith(SECURITY_FILE_SUFFIX)) {
                parseFile(file, list);
            }
        }
        return list;
    }

}
