/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            } catch (final OXException e) {
                while (!executed.isEmpty()) {
                    executed.pop().done(copied, true);
                }
                throw e;
            } catch (final Exception e) {
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
