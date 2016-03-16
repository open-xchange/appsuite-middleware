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
