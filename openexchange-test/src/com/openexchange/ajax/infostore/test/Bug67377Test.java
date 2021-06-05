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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.File;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestInit;

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
