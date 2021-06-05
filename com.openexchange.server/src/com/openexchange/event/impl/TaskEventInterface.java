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

package com.openexchange.event.impl;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;

/**
 * {@link TaskEventInterface}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public interface TaskEventInterface {

    /**
     * Invoked if a new task is created.
     *
     * @param taskObj The task
     * @param sessionObj The user session
     */
    public void taskCreated(Task taskObj, Session sessionObj);

    /**
     * Invoked if a new task is modified.
     *
     * @param taskObj The task
     * @param sessionObj The user session
     */
    public void taskModified(Task taskObj, Session sessionObj);

    /**
     * Invoked if one of task's participants changed his confirmation status to
     * accepted.
     *
     * @param taskObj
     *            The task
     * @param sessionObj
     *            The user session
     */
    public void taskAccepted(Task taskObj, Session sessionObj);

    /**
     * Invoked if one of task's participants changed his confirmation status to
     * declined.
     *
     * @param taskObj
     *            The task
     * @param sessionObj
     *            The user session
     */
    public void taskDeclined(Task taskObj, Session sessionObj);

    /**
     * Invoked if one of task's participants changed his confirmation status to
     * tentatively accepted
     *
     * @param taskObj
     *            The task
     * @param sessionObj
     *            The user session
     */
    public void taskTentativelyAccepted(Task taskObj, Session sessionObj);

    public void taskDeleted(Task taskObj, Session sessionObj);

}
