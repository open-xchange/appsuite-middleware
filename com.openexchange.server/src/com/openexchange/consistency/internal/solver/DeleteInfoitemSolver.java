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

import java.text.MessageFormat;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;

/**
 * {@link DeleteInfoitemSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DeleteInfoitemSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteInfoitemSolver.class);

    private final DatabaseImpl database;

    public DeleteInfoitemSolver(final DatabaseImpl database) {
        this.database = database;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        // Now we go through the set an delete each superfluous entry:
        for (final String identifier : problems) {
            try {
                database.startTransaction();
                database.startDBTransaction();
                database.setRequestTransactional(true);
                final int[] numbers = database.removeDocument(identifier, entity.getContext());
                database.commit();
                if (numbers[0] == 1) {
                    LOG.info(MessageFormat.format("Have to change infostore version number for entry: {0}", identifier));
                }
                if (numbers[1] == 1) {
                    LOG.info(MessageFormat.format("Deleted entry {0} from infostore_documents.", identifier));
                }
            } catch (OXException e) {
                LOG.error("{}", e.getMessage(), e);
                try {
                    database.rollback();
                    return;
                } catch (OXException e1) {
                    LOG.debug("{}", e1.getMessage(), e1);
                }
            } finally {
                try {
                    database.finish();
                } catch (OXException e) {
                    LOG.debug("{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String description() {
        return "delete infoitem";
    }
}
