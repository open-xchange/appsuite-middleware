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

package com.openexchange.ajax.requesthandler.jobqueue;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Job} - A dispatcher job to perform.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface Job {

    /**
     * Checks whether this job is trackable by watcher.
     *
     * @return <code>true</code> if trackable; otherwise <code>false</code>
     */
    boolean isTrackable();

    /**
     * Gets the optional job key.
     *
     * @return The key or <code>null</code>
     */
    JobKey getOptionalKey();

    /**
     * Gets the associated user session
     *
     * @return The session
     */
    ServerSession getSession();

    /**
     * Gets the request data
     *
     * @return The request data
     */
    AJAXRequestData getRequestData();

    /**
     * Performs this job.
     *
     * @return The result yielded from given request
     * @throws OXException If an error occurs
     */
    AJAXRequestResult perform() throws OXException;
}
