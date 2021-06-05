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

package com.openexchange.filemanagement;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.exception.OXException;

/**
 * {@link DistributedFileManagement} - Provides access to distributed files in the cluster.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface DistributedFileManagement {

    /**
     * Specifies the path component for a distributed file and is therefore also the name in the URI namespace of the Http Service at which the registration will be mapped.
     */
    public static final String PATH = "/dfm/distributedFiles";

    /**
     * Registers the file associated with given identifier for distributed access among Open-Xchange nodes.
     *
     * @param id The identifier
     * @throws OXException If register attempt fails
     */
    public void register(String id) throws OXException;

    /**
     * Unregisters the file associated with given identifier from distributed access among Open-Xchange nodes.
     *
     * @param id The identifier
     * @throws OXException If unregister attempt fails
     */
    public void unregister(String id) throws OXException;

    /**
     * Gets the binary content of the (distributed) file associated with given identifier.
     *
     * @param id The identifier
     * @return The file's binary content as an input stream
     * @throws OXException If providing binary content fails
     */
    public InputStream get(String id) throws OXException;

    /**
     * Checks if there is such a distributed file associated with given identifier.
     *
     * @param id The identifier
     * @return <code>true</code> if there is such a distributed file; otherwise <code>false</code>
     * @throws OXException If existence check fails
     */
    public boolean exists(String id) throws OXException;

    /**
     * Checks if there is such a distributed file associated with given identifier.
     *
     * @param id The identifier
     * @param timeout The time-out for this operation
     * @param unit The time unit for time-out value
     * @return <code>true</code> if there is such a distributed file; otherwise <code>false</code>
     * @throws OXException If existence check fails
     * @throws TimeoutException If time-out is exceeded
     */
    public boolean exists(String id, long timeout, TimeUnit unit) throws OXException, TimeoutException;

    /**
     * Touches/resets the time stamp of the file associated with given identifier.
     *
     * @param id The identifier
     * @throws OXException If resetting time stamp fails
     */
    public void touch(String id) throws OXException;

    /**
     * Removes the file associated with given identifier from distributed access among Open-Xchange nodes.
     *
     * @param id The identifier
     * @throws OXException If removal fails
     */
    public void remove(String id) throws OXException;

}
