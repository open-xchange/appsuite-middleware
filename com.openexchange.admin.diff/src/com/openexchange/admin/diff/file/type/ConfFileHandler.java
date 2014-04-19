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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.diff.file.type;

import org.apache.commons.io.FilenameUtils;
import com.openexchange.admin.diff.file.type.impl.CcfHandler;
import com.openexchange.admin.diff.file.type.impl.ConfHandler;
import com.openexchange.admin.diff.file.type.impl.NoConfigFileHandler;
import com.openexchange.admin.diff.file.type.impl.NoExtensionHandler;
import com.openexchange.admin.diff.file.type.impl.PerfmapHandler;
import com.openexchange.admin.diff.file.type.impl.PropertyHandler;
import com.openexchange.admin.diff.file.type.impl.ShHandler;
import com.openexchange.admin.diff.file.type.impl.TypesHandler;
import com.openexchange.admin.diff.file.type.impl.XmlHandler;
import com.openexchange.admin.diff.file.type.impl.YamlHandler;

/**
 * Adds the configuration files to its associated (map) handler
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class ConfFileHandler {

    /**
     * Initializes a new {@link ConfFileHandler}.
     */
    private ConfFileHandler() {
        // prevent instantiation
    }

    /**
     * Adds the given file to get handled
     * 
     * @param fileName - the name of the file to add
     * @param content - the content of the file to add
     * @param isOriginal - boolean to indicate if the file is from the installation folder or from the installation
     */
    public synchronized static void addConfigurationFile(String fileName, String content, boolean isOriginal) {
        String usedFilename = fileName;
        String fileExtension = FilenameUtils.getExtension(usedFilename);

        if (fileExtension.equalsIgnoreCase("in")) {
            fileExtension = FilenameUtils.getExtension(FilenameUtils.getBaseName(usedFilename));
            usedFilename = FilenameUtils.removeExtension(fileName);
        }

        if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.CNF.getFileExtension()) || fileExtension.equalsIgnoreCase(ConfigurationFileTypes.CONF.getFileExtension())) {
            ConfHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.PERFMAP.getFileExtension())) {
            PerfmapHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.PROPERTY.getFileExtension())) {
            PropertyHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.SH.getFileExtension())) {
            ShHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.XML.getFileExtension())) {
            XmlHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.CCF.getFileExtension())) {
            CcfHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.YAML.getFileExtension()) || fileExtension.equalsIgnoreCase(ConfigurationFileTypes.YML.getFileExtension())) {
            YamlHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.NO_EXTENSION.getFileExtension())) {
            NoExtensionHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else if (fileExtension.equalsIgnoreCase(ConfigurationFileTypes.TYPES.getFileExtension())) {
            TypesHandler.getInstance().addFile(usedFilename, content, isOriginal);
        } else {
            NoConfigFileHandler.getInstance().addFile(usedFilename, content, isOriginal);
        }
    }
}
