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
