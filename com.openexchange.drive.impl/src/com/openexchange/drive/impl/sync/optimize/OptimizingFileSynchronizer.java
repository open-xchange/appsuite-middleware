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

package com.openexchange.drive.impl.sync.optimize;

import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.FileSynchronizer;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;


/**
 * {@link OptimizingFileSynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OptimizingFileSynchronizer extends FileSynchronizer {

    public OptimizingFileSynchronizer(SyncSession session, VersionMapper<FileVersion> mapper, String path) {
        super(session, mapper, path);
    }

    @Override
    public IntermediateSyncResult<FileVersion> sync() throws OXException {
        IntermediateSyncResult<FileVersion> result = super.sync();
        if (false == result.isEmpty()) {
            int lastResults = 0;
            if (session.isTraceEnabled()) {
                lastResults = result.hashCode();
                session.trace("Sync results before optimizations:\n" + result.toString());
            }
            FileActionOptimizer[] optimizers = {
                new FileRenameOptimizer(mapper),
                new EmptyFileOptimizer(mapper),
                new FileCopyOptimizer(mapper),
                new FileMultipleUploadsOptimizer(mapper),
                new FileDelayMetadataDownloadOptimizer(mapper),
                new FileOrderOptimizer(mapper),
                new FileInlineMetadataOptimizer(mapper)
            };
            for (FileActionOptimizer optimizer : optimizers) {
                result = optimizer.optimize(session, result);
                if (session.isTraceEnabled()) {
                    int currentResults = result.hashCode();
                    if (currentResults != lastResults) {
                        lastResults = currentResults;
                        session.trace("Sync results after optimizations of " + optimizer.getClass().getSimpleName() + ":\n" + result.toString());
                    }
                }
            }
        }
        return result;
    }

}
