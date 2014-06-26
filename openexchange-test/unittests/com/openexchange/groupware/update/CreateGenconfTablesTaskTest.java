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

package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;
import java.sql.SQLException;
import com.openexchange.groupware.update.tasks.CreateGenconfTablesTask;

/**
 * {@link CreateGenconfTablesTaskTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CreateGenconfTablesTaskTest extends UpdateTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        execSafe("DROP TABLE genconf_attributes_strings");
        execSafe("DROP TABLE genconf_attributes_bools");
        execSafe("DROP TABLE sequence_genconf");

        super.tearDown();
    }

    public void testShouldCreateStringAttributeTable() throws OXException {
        executeTask();
        try {
            exec("SELECT cid, id, value, name FROM genconf_attributes_strings");
        } catch (SQLException x) {
            fail("Expected table genconf_attributes_strings, but doesn't seem to exist: "+x.toString());
        }
    }

    public void testShouldCreateBooleanAttributeTable() throws OXException {
        executeTask();
        try {
            exec("SELECT cid, id, value, name FROM genconf_attributes_bools");
        } catch (SQLException x) {
            fail("Expected table genconf_attributes_bools, but doesn't seem to exist: "+x.toString());
        }
    }

    public void testShouldBeRunnableTwice() throws OXException {
        executeTask();
        executeTask();
    }

    public void testShouldCreateSequenceTable() throws OXException {
        executeTask();
        try {
            exec("SELECT cid, id FROM sequence_genconf");
        } catch (SQLException x) {
            fail("Expected table sequence_genconf, but doesn't seem to exist: "+x.toString());
        }
    }

    public void testShouldCreateEntryInSequenceTableForEveryContext() throws OXException, SQLException {
        executeTask();
        assertResult("SELECT 1 FROM sequence_genconf WHERE cid = "+existing_ctx_id);
    }


    private void executeTask() throws OXException {
        new CreateGenconfTablesTask().perform(schema, existing_ctx_id);
    }

}
