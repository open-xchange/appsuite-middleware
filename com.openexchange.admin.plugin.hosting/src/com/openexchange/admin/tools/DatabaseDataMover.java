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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.admin.tools;

import java.util.Vector;
import java.util.concurrent.Callable;

public class DatabaseDataMover implements Callable<Vector> {

    private int context_id = -1;

    private int database_id = -1;

    private int reason_id = -1;

    /**
     * 
     */
    public DatabaseDataMover(final int context_id, final int database_id, final int reason_id) {
        this.context_id = context_id;
        this.database_id = database_id;
        this.reason_id = reason_id;
    }

    public Vector call() {
        Vector<String> ret = new Vector<String>();
        // FIXME: Use new interfaces here
        // OXContext_MySQL oxcox;
//        try {
            // oxcox = new OXContext_MySQL();
            // ret = oxcox.moveDatabaseContext(context_id, database_id,
            // reason_id);

            ret.clear();
            ret.add("OK");
            ret.add("Successfully moved");
//        } catch (SQLException e) {
//            log.error("Error copying database", e);
//            ret.add("ERROR");
//            ret.add("" + e.getMessage());
//        } catch (PoolException e) {
//            log.error("Error copying database", e);
//            ret.add("ERROR");
//            ret.add("" + e.getMessage());
//        } catch (TargetDatabaseException e) {
//            log.error("Error copying database", e);
//            ret.add("ERROR");
//            ret.add("" + e.getMessage());
//        } catch (DatabaseContextMappingException e) {
//            log.error("Error copying database", e);
//            ret.add("ERROR");
//            ret.add("" + e.getMessage());
//        }

        return ret;
    }

}
