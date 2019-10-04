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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.test.TestInit;

/**
 * {@link Bug67377Test}
 *
 * Cannot set last modified date when uploading a new file to the infostore module
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug67377Test extends AbstractInfostoreTest {

    @Test
    public void testCreateWithLastModified() throws Exception {
        /*
         * create file with last modified in the past
         */
        Date lastModified = TimeTools.D("two years ago", getClient().getValues().getTimeZone());
        File file = InfostoreTestManager.createFile(getClient().getValues().getPrivateInfostoreFolder(), "Bug67377Test1.txt", "text/plain");
        file.setLastModified(lastModified);
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        /*
         * reload and verify created file
         */
        File reloadedFile = itm.getAction(file.getId());
        assertNotNull(reloadedFile);
        assertEquals(reloadedFile.getLastModified(), lastModified);
    }

    @Test
    public void testCreateWithCreationDate() throws Exception {
        /*
         * create file with creation date in the past
         */
        Date creationDate = TimeTools.D("last year", getClient().getValues().getTimeZone());
        File file = InfostoreTestManager.createFile(getClient().getValues().getPrivateInfostoreFolder(), "Bug67377Test2.txt", "text/plain");
        file.setCreated(creationDate);
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        /*
         * reload and verify created file
         */
        File reloadedFile = itm.getAction(file.getId());
        assertNotNull(reloadedFile);
        assertEquals(reloadedFile.getCreated(), creationDate);
    }

    @Test
    public void testCreateWithCreationAndLastModifiedDate() throws Exception {
        /*
         * create file with creation and last modified date in the past
         */
        Date lastModified = TimeTools.D("two years ago", getClient().getValues().getTimeZone());
        Date creationDate = TimeTools.D("last year", getClient().getValues().getTimeZone());
        File file = InfostoreTestManager.createFile(getClient().getValues().getPrivateInfostoreFolder(), "Bug67377Test3.txt", "text/plain");
        file.setCreated(creationDate);
        file.setLastModified(lastModified);
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        /*
         * reload and verify created file
         */
        File reloadedFile = itm.getAction(file.getId());
        assertNotNull(reloadedFile);
        assertEquals(reloadedFile.getCreated(), creationDate);
        assertEquals(reloadedFile.getLastModified(), lastModified);
    }

}
