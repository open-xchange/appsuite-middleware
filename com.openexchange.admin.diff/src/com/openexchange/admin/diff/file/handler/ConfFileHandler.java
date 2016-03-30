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

package com.openexchange.admin.diff.file.handler;

import org.apache.commons.io.FilenameUtils;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.impl.CcfHandler;
import com.openexchange.admin.diff.file.handler.impl.ConfHandler;
import com.openexchange.admin.diff.file.handler.impl.NoConfigFileHandler;
import com.openexchange.admin.diff.file.handler.impl.NoExtensionHandler;
import com.openexchange.admin.diff.file.handler.impl.PerfmapHandler;
import com.openexchange.admin.diff.file.handler.impl.PropertyHandler;
import com.openexchange.admin.diff.file.handler.impl.ShHandler;
import com.openexchange.admin.diff.file.handler.impl.TypesHandler;
import com.openexchange.admin.diff.file.handler.impl.XmlHandler;
import com.openexchange.admin.diff.file.handler.impl.YamlHandler;
import com.openexchange.admin.diff.file.type.ConfigurationFileTypes;
import com.openexchange.admin.diff.result.DiffResult;


/**
 * Adds the configuration files to its associated (map) handler
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
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
     * @param diffResult - result to add processing infos
     * @param configurationFile - the file that should be added to processing queue
     */
    public synchronized static void addConfigurationFile(DiffResult diffResult, ConfigurationFile configurationFile) {
        String fileExtension = configurationFile.getExtension();

        if (fileExtension.equalsIgnoreCase("in")) {
            configurationFile.setName(FilenameUtils.removeExtension(configurationFile.getName()));
            configurationFile.setExtension(FilenameUtils.getExtension(configurationFile.getName()));
        }

        if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.CNF.getFileExtension()) ||
            configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.CONF.getFileExtension())) {
            ConfHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.PERFMAP.getFileExtension())) {
            PerfmapHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.PROPERTY.getFileExtension())) {
            PropertyHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.SH.getFileExtension())) {
            ShHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.XML.getFileExtension())) {
            XmlHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.CCF.getFileExtension())) {
            CcfHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.YAML.getFileExtension()) || configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.YML.getFileExtension())) {
            YamlHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.NO_EXTENSION.getFileExtension())) {
            NoExtensionHandler.getInstance().addFile(diffResult, configurationFile);
        } else if (configurationFile.getExtension().equalsIgnoreCase(ConfigurationFileTypes.TYPES.getFileExtension())) {
            TypesHandler.getInstance().addFile(diffResult, configurationFile);
        } else {
            NoConfigFileHandler.getInstance().addFile(diffResult, configurationFile);
        }
    }
}
