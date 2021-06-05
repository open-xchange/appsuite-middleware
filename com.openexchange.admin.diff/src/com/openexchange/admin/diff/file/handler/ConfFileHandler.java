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
