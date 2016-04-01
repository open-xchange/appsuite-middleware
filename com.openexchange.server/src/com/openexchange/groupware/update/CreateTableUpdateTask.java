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

package com.openexchange.groupware.update;

import java.sql.Connection;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;


/**
 * {@link CreateTableUpdateTask} - Wraps an existing {@link CreateTableService} instance as an update task.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CreateTableUpdateTask implements UpdateTaskV2 {

    private final CreateTableService create;
    private final String[] dependencies;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link CreateTableUpdateTask} from specified arguments.
     * <p>
     * This is the legacy constructor for maintaining the former constructor declaration.
     *
     * @param create The create-table service
     * @param dependencies The dependencies to preceding update tasks
     * @param version The version number; no more used
     * @param databaseService The database service
     */
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies, int version, DatabaseService databaseService) {
        this(create, dependencies, databaseService);
    }

    /**
     * Initializes a new {@link CreateTableUpdateTask} from specified arguments.
     *
     * @param create The create-table service
     * @param dependencies The dependencies to preceding update tasks
     * @param databaseService The database service
     */
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies, DatabaseService databaseService) {
        super();
        this.create = create;
        this.dependencies = dependencies;
        this.databaseService = databaseService;
    }

    @Override
    public TaskAttributes getAttributes() {
        // Creating Tables is blocking and schema level.
        return new Attributes();

    }

    @Override
    public String[] getDependencies() {
        return dependencies;
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        Connection con = null;
        try {
            con = getConnection(contextId);
            create.perform(con);
        } finally {
            releaseConnection(contextId, con);
        }
    }

    private void releaseConnection(final int contextId, final Connection con) {
        databaseService.backForUpdateTask(contextId, con);
    }

    private Connection getConnection(final int contextId) throws OXException {
        return databaseService.getForUpdateTask(contextId);
    }

}
