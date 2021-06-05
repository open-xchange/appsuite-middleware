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

import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.DirectorySynchronizer;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;


/**
 * {@link OptimizingDirectorySynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OptimizingDirectorySynchronizer extends DirectorySynchronizer {

    public OptimizingDirectorySynchronizer(SyncSession session, VersionMapper<DirectoryVersion> mapper) {
        super(session, mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> sync() throws OXException {
        IntermediateSyncResult<DirectoryVersion> result = super.sync();
        if (false == result.isEmpty()) {
            int lastResults = 0;
            if (session.isTraceEnabled()) {
                lastResults = result.hashCode();
                session.trace("Sync results before optimizations:\n" + result.toString());
            }
            DirectoryActionOptimizer[] optimizers = {
                new DirectoryRemoveOptimizer(mapper),
                new DirectoryRenameOptimizer(mapper),
                new EmptyDirectoryOptimizer(mapper),
                new DirectoryOrderOptimizer(mapper)
            };
            for (DirectoryActionOptimizer optimizer : optimizers) {
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
