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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskSorter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UpdateTaskSorter {

    private static final DependencyChecker[] CHECKERS = { new DependenciesResolvedChecker() };

    public UpdateTaskSorter() {
        super();
    }

    public List<UpdateTaskV2> sort(String[] executed, List<UpdateTaskV2> toExecute) throws OXException {
        List<UpdateTaskV2> retval = new ArrayList<UpdateTaskV2>(toExecute.size());
        boolean found = true;
        while (!toExecute.isEmpty() && found) {
            found = false;
            Iterator<UpdateTaskV2> iter = toExecute.iterator();
            while (iter.hasNext() && !found) {
                UpdateTaskV2 task = iter.next();
                UpdateTaskV2[] retvalA = retval.toArray(new UpdateTaskV2[retval.size()]);
                UpdateTaskV2[] toExecuteA = toExecute.toArray(new UpdateTaskV2[toExecute.size()]);
                for (int i = 0; i < CHECKERS.length && !found; i++) {
                    found = CHECKERS[i].check(task, executed, retvalA, toExecuteA);
                }
                if (found) {
                    retval.add(task);
                    iter.remove();
                }
            }
        }
        if (!toExecute.isEmpty()) {
            throw UpdateExceptionCodes.UNRESOLVABLE_DEPENDENCIES.create(Strings.join(executed, ","), Strings.join(retval, ","), Strings.join(toExecute, ","));
        }
        return retval;
    }
}
