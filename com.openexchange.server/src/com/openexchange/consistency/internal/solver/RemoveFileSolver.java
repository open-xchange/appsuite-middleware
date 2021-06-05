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

package com.openexchange.consistency.internal.solver;

import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;

/**
 * {@link RemoveFileSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RemoveFileSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveFileSolver.class);

    private final FileStorage storage;

    public RemoveFileSolver(final FileStorage storage) {
        super();
        this.storage = storage;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        try {
            for (final String identifier : problems) {
                try {
                    if (storage.deleteFile(identifier)) {
                        LOG.info("Deleted identifier: {}", identifier);
                    }
                } catch (Exception e) {
                    LOG.debug("{}", e.getMessage(), e);
                }
            }
            /*
             * Afterwards we recreate the state file because it could happen that that now new free file slots are available.
             */
            storage.recreateStateFile();
        } catch (OXException e) {
            LOG.error("{}", e.getMessage(), e);
        }
    }

    @Override
    public String description() {
        return "delete file";
    }
}
