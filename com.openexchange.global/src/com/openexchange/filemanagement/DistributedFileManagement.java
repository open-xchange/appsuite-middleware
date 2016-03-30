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
