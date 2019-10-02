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

package com.openexchange.security.manager.configurationReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;

/**
 * {@link ConfigurationFileParser} Loads all of the configurations in the security-manager.list that
 * will require directory access
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class ConfigurationFileParser {

    private static final String SECURITY_FILE_SUFFIX = ".list";
    private static final String SECURITY_FOLDER = "security";
    private final ConfigurationService configService;

    public ConfigurationFileParser(ConfigurationService configService) {
        this.configService = configService;
    }

    /**
     * Read the data from a file, add each valid line to list
     *
     * @param file  File to read
     * @param list  List to store valid lines
     */
    private void parseFile (File file, ArrayList<String> list) {
        String data = configService.getText(file.getName());
        if (data != null) {
            String[] lines = data.split("\n");
            for (String line: lines) {
                line = line.trim();
                if (line.indexOf("#") != 0 && !line.isEmpty()) {
                    list.add(line);
                }
            }
        }
    }

    /**
     * Read through the security directory and return List of configuration options
     *
     * @return List of configurations found in the files
     * @throws IOException
     */
    public List<String> getConfigList () throws IOException {
        File folder = configService.getDirectory(SECURITY_FOLDER);
        ArrayList<String> list = new ArrayList<String> ();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(SECURITY_FILE_SUFFIX)) {
                    parseFile(file, list);
                }
            }
        }
        return list;
    }



}
