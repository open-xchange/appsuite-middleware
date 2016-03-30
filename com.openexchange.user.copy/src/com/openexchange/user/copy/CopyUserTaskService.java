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

package com.openexchange.user.copy;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link CopyUserTaskService}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface CopyUserTaskService {

    /**
     * @return a string array listing the CopyUserTaskService implementation class names that need to be executed before the current
     * implementation can be executed.
     */
    String[] getAlreadyCopied();

    /**
     * @return name of the copied objects returned by the {@link #copyUser(Map)} method. Use the class name of the generic type of the
     * {@link ObjectMapping}.
     */
    String getObjectName();

    /**
     * This method is called to copy some specific user data to another context. Use the given {@link ObjectMapping} as much as possible to
     * increase performance.
     *
     * @param copied map that contains information to ease the task to copy some specific user data.
     * @return a map containing information to ease other copy tasks work.
     * @throws UserCopyException if copying some specific data fails. The {@link #done(boolean)} method is then called with
     * <code>true</code>.
     */
    ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException;

    /**
     * This method is called in reverse order as the {@link #copyUser(Map)} methods of all tasks after all tasks are executed. Ensure that
     * temporary resources put into the {@link ObjectMapping} are freed in this method. This method is also called in reverse task order if
     * some task fails with a {@link UserCopyException}. In this case the given failed attribute is <code>true</code>.
     * @param copied map that contains information to ease the task to copy some specific user data.
     * @param failed <code>false</code> if all tasks are executed successfully.
     */
    void done(Map<String, ObjectMapping<?>> copied, boolean failed);
}
