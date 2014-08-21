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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.database.migration;

import java.util.List;
import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor;
import com.openexchange.exception.OXException;

/**
 * Interface that defines the execution of database migration tasks
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public interface DBMigrationExecutorService {

    /**
     * Execute database migration based on the given {@link DatabaseChangeLog} file
     *
     * @param databaseChangeLog - file to execute
     * @throws OXException
     */
    public void execute(DatabaseChangeLog databaseChangeLog) throws OXException;

    /**
     * Execute database migration based on the given file name
     *
     * @param fileName - String with the name of the file to execute
     * @throws OXException
     */
    public void execute(String fileName) throws OXException;

    /**
     * Execute database migration based on the given filename. If {@link CustomSqlChange} or {@link CustomTaskChange} are desired to be used
     * add additional {@link ResourceAccessor} via parameter 'additionalAccessors' so that this classes can be found. Provide
     * <code>null</code> in case you are using xml files no additional accessor is required.
     *
     * @param fileName - String with the name of the file to execute
     * @param additionalAccessors - additional {@link ResourceAccessor}s to be able to read custom classes
     * @throws OXException
     */
    public void execute(String fileName, List<ResourceAccessor> additionalAccessors) throws OXException;

    public void rollback(int numberOfChangeSets) throws OXException;

    /**
     * Executes a rollback to the given tag
     *
     * @param fileName - String with the name of the file where to find the rollback tag
     * @param changeSetTag - changeset tag to roll back to
     * @return boolean - true if rollback was successful. Otherwise false
     * @throws OXException
     */
    public boolean rollback(String fileName, String changeSetTag) throws OXException;
}
