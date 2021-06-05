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

/**
 * {@link ProblemSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
public interface ProblemSolver {

    /**
     * Performs the solve operation for the specified {@link Entity}.
     * 
     * @param entity The entity to perform the solve operation for
     * @param problems A set with troublesome object identifiers for which the solve operation should apply
     * @throws OXException if an error is occurred
     */
    public void solve(Entity entity, Set<String> problems) throws OXException;

    /**
     * Returns a brief description of the solver
     * 
     * @return a brief description of the solver
     */
    String description();
}
