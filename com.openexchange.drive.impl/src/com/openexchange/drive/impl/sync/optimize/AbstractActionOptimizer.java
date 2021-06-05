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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.VersionMapper;

/**
 * {@link AbstractActionOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractActionOptimizer<T extends DriveVersion> implements ActionOptimizer<T> {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractActionOptimizer.class);

    protected final VersionMapper<T> mapper;

    /**
     * Initializes a new {@link AbstractActionOptimizer}.
     *
     * @param mapper The version mapper
     */
    public AbstractActionOptimizer(VersionMapper<T> mapper) {
        super();
        this.mapper = mapper;
    }

    protected static <T extends DriveVersion> boolean matchesByChecksum(T v1, T v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return false;
        } else {
            return null == v1.getChecksum() ? null == v2.getChecksum() : v1.getChecksum().equals(v2.getChecksum());
        }
    }

    protected static <T extends DriveVersion> List<AbstractAction<T>> filterByAction(List<AbstractAction<T>> driveActions, Action action) {
        List<AbstractAction<T>> filteredActions = new ArrayList<AbstractAction<T>>();
        for (AbstractAction<T> driveAction : driveActions) {
            if (null != driveAction && action.equals(driveAction.getAction())) {
                filteredActions.add(driveAction);
            }
        }
        return filteredActions;
    }

}
