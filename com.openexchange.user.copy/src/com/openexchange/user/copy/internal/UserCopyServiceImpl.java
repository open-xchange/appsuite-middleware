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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.UserCopyService;

/**
 * {@link UserCopyServiceImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UserCopyServiceImpl implements UserCopyService {

    // Adding and removal can take place concurrently to reading the list for execution.
    private final List<CopyUserTaskService> tasks = new CopyOnWriteArrayList<CopyUserTaskService>();

    public UserCopyServiceImpl() {
        super();
    }

    @Override
    public int copyUser(final int srcCtxId, final int dstCtxId, final int userId) throws OXException {
        final List<CopyUserTaskService> toExecute = new CopyTaskSorter().sort(tasks);
        final Map<String, ObjectMapping<?>> copied = new HashMap<String, ObjectMapping<?>>();
        copied.put(Constants.CONTEXT_ID_KEY, new ObjectMapping<Integer>() {
            @Override
            public Integer getSource(final int id) {
                return I(srcCtxId);
            }
            @Override
            public Integer getDestination(final Integer source) {
                return I(dstCtxId);
            }
            @Override
            public Set<Integer> getSourceKeys() {
                final Set<Integer> keySet = new HashSet<Integer>(1);
                keySet.add(I(srcCtxId));

                return keySet;
            }});

        copied.put(Constants.USER_ID_KEY, new ObjectMapping<Integer>() {
            @Override
            public Integer getSource(final int id) {
                return I(userId);
            }
            @Override
            public Integer getDestination(final Integer source) {
                return null;
            }
            @Override
            public Set<Integer> getSourceKeys() {
                final Set<Integer> keySet = new HashSet<Integer>(1);
                keySet.add(I(userId));

                return keySet;
            }});
        final Stack<CopyUserTaskService> executed = new Stack<CopyUserTaskService>();
        for (final CopyUserTaskService task : toExecute) {
            try {
                final ObjectMapping<?> copiedObjects = task.copyUser(copied);
                executed.push(task);
                if (null != copiedObjects) {
                    copied.put(task.getObjectName(), copiedObjects);
                }
            } catch (OXException e) {
                while (!executed.isEmpty()) {
                    executed.pop().done(copied, true);
                }
                throw e;
            } catch (Exception e) {
                while (!executed.isEmpty()) {
                    executed.pop().done(copied, true);
                }
                throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
            }
        }
        while (!executed.isEmpty()) {
            executed.pop().done(copied, false);
        }
        return i(new CopyTools(copied).getDestinationUserId());
    }

    public void addTask(final CopyUserTaskService task) {
        tasks.add(task);
    }

    public void removeTask(final CopyUserTaskService task) {
        tasks.remove(task);
    }

    public int getTaskCount() {
        return tasks.size();
    }
}
