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

package com.openexchange.user.copy.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.UserCopyExceptionCodes;

/**
 * {@link CopyTaskSorter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CopyTaskSorter {

    CopyTaskSorter() {
        super();
    }

    List<CopyUserTaskService> sort(final List<CopyUserTaskService> toSort) throws OXException {
        // Copy passed list of tasks because multiple iterators are used on this list which may have been modified in between.
        final List<CopyUserTaskService> copy = new ArrayList<CopyUserTaskService>(toSort);
        final List<CopyUserTaskService> retval = new ArrayList<CopyUserTaskService>(copy.size());
        boolean found = true;
        while (found && !copy.isEmpty()) {
            found = false;
            final Iterator<CopyUserTaskService> iter = copy.iterator();
            while (!found && iter.hasNext()) {
                final CopyUserTaskService task = iter.next();
                found = checkDependencies(task.getAlreadyCopied(), retval);
                if (found) {
                    retval.add(task);
                    iter.remove();
                }
            }
        }
        if (!copy.isEmpty()) {
            throw UserCopyExceptionCodes.UNRESOLVABLE_DEPENDENCIES.create(Strings.join(retval, ","), Strings.join(copy, ","));
        }
        return retval;
    }

    private boolean checkDependencies(final String[] dependencies, final List<CopyUserTaskService> enqueued) {
        if (dependencies.length == 0) {
            return true;
        }
        for (final String dependency : dependencies) {
            boolean found = false;
            for (final CopyUserTaskService task : enqueued) {
                final String cn = task.getClass().getName();
                if (cn.equals(dependency)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
