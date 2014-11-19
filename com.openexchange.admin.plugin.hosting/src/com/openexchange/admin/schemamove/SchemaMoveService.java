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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.admin.schemamove;

import java.util.Map;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.exception.OXException;

/**
 * {@link SchemaMoveService} - The service providing methods to move a schema to another database.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SchemaMoveService {

    /**
     * Disables the denoted schema.
     * <ul>
     * <li>Checks required preconditions</li>
     * <li>Determines affected contexts</li>
     * <li>Disables active contexts and decorates them with a certain reason identifier</li>
     * <li>Distribute changes contexts in cluster</li>
     * <li>Terminate active sessions in cluster</li>
     * </ul>
     *
     * @param schemaName The schema name
     * @throws OXException If operation fails
     * @throws TargetDatabaseException
     */
    void disableSchema(String schemaName) throws OXException, TargetDatabaseException;

    /**
     * Returns the database access information that are necessary to establish a connection to given schema's database.
     * <p>
     * The returned map contains may contain:
     * <ul>
     * <li><code>"url"</code></li>
     * <li><code>"db_scheme"</code></li>
     * <li><code>"driver"</code></li>
     * <li><code>"login"</code></li>
     * <li><code>"name"</code></li>
     * <li><code>"password"</code></li>
     * </ul>
     *
     * @param schemaName The schema name
     * @return The database access information
     * @throws OXException If operation fails
     */
    Map<String, String> getDbAccessInfoForSchema(String schemaName) throws OXException;

    void enableSchema(String schemaName) throws OXException;
}
