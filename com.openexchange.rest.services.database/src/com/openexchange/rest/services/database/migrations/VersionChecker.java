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

package com.openexchange.rest.services.database.migrations;

import java.sql.Connection;
import com.openexchange.exception.OXException;


/**
 * The {@link VersionChecker} handles migration metadata about a schema in relation to an external service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface VersionChecker {
    
    /**
     * Checks whether this connections schema can be considered up-to-date.
     * @param id An id identifying the schema the connection is connected to. Used for caching.
     * @param con The connection to the schema
     * @param module The module for which the version should be checked
     * @param versionId The wished for versionId
     * @return null, if the versionId matches the requested version, or the current versionId for this schema, if it doesn't match
     */
    String isUpToDate(Object id, Connection con, String module, String versionId) throws OXException;
    
    /**
     * Set the version for this schema and module from the oldVersionId to the newVersionId
     * @param con The connection to the schema
     * @param module The module for which the version should be incremented
     * @param oldVersionId The versionId this schema is presumed to be in
     * @param newVersionId The versionId this schema shall be migrated to
     * @return null, if the current schema matches the wanted oldVersionId, the current version otherwise. When a non-null value is returned, the version was not incremented.
     */
    String updateVersion(Connection con, String module, String oldVersionId, String newVersionId) throws OXException;
    
    /**
     * Try locking this schema
     * @param con A connection to the schema
     * @param module The module for which this lock is relevant
     * @param now The current time, usually System.currentTimeMillis() is a good value here. Used to expire stale locks
     * @param expires The timestamp when the lock should expire. System.currentTimeMillis() + the grace period. 
     * @return true, if the schema could be locked, false otherwise
     */
    boolean lock(Connection con, String module, long now, long expires) throws OXException;

    /**
     * Forces a schema/module combination to be unlocked.
     * @param con The connection to the schema to be unlocked
     * @param module The module specifier.
     */
    void unlock(Connection con, String module) throws OXException;
    
    /**
     * Update the expiry on a lock to denote it is still valid
     * @param con A connection to the schema
     * @param module The module name
     * @param expires The new expiry date
     * @return true, if the lock could be extended, false if the schema is not locked
     */
    boolean touchLock(Connection con, String module, long expires) throws OXException;

}
