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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.AbstractOXException;


/**
 * {@link CreateTableUpdateTask}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CreateTableUpdateTask implements UpdateTaskV2 {

    private CreateTableService create;
    private String[] dependencies;
    private int version;
    private DatabaseService databaseService;
    
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies, int version, DatabaseService databaseService) {
        super();
        this.create = create;
        this.dependencies = dependencies;
        this.version = version;
        this.databaseService = databaseService;
    }

    public TaskAttributes getAttributes() {
        // Creating Tables is blocking and schema level.
        return new Attributes(); 
        
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public void perform(PerformParameters params) throws AbstractOXException {
        perform(params.getSchema(), params.getContextId());
    }

    public int addedWithVersion() {
        return version;
    }

    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.HIGH.priority;
    }

    public void perform(Schema schema, int contextId) throws AbstractOXException {
        Connection con = null;
        try {
            con = getConnection(contextId);
            create.perform(con);
        } catch (AbstractOXException x) {
            throw x;
        } finally {
            releaseConnection(contextId, con);
        }
    }

    private void releaseConnection(int contextId, Connection con) {
        databaseService.backForUpdateTask(contextId, con);
    }

    private Connection getConnection(int contextId) throws DBPoolingException {
        return databaseService.getForUpdateTask(contextId);
    }

}
