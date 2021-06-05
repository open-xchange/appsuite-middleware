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

package com.openexchange.admin.diff.file.handler.impl;

import java.util.List;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.writer.DiffMatchPatchWriter;
import com.openexchange.admin.diff.result.writer.DiffWriter;



/**
 * Handler for .types configuration files
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class TypesHandler extends AbstractFileHandler {

    private volatile static TypesHandler instance;

    private TypesHandler() {
        ConfigDiff.register(this);
    }

    public static synchronized TypesHandler getInstance() {
        if (instance == null) {
            synchronized (TypesHandler.class) {
                if (instance == null) {
                    instance = new TypesHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        DiffWriter diffMatchPatchWriter = new DiffMatchPatchWriter();
        diffMatchPatchWriter.addOutputToDiffResult(diffResult, lOriginalFiles, lInstalledFiles);

        return diffResult;
    }
}