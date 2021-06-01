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

import org.junit.Test;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.junit.Assert;

/**
 * {@link Bug54232Test}
 *
 * file names are case sensitive
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug54232Test extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link Bug54232Test}.
     *
     * @param name The test name
     */
    public Bug54232Test() {
        super();
    }

    @Test
    public void testCreateCaseConflictingFiles() throws Exception {
        /*
         * create first file
         */
        File file1 = itm.createFileOnServer(getClient().getValues().getPrivateInfostoreFolder(), "test.txt", "text/plain");
        File reloadedFile1 = itm.getAction(file1.getId());
        /*
         * try to create another file with same name, ignoring case
         */
        File file2 = itm.createFileOnServer(getClient().getValues().getPrivateInfostoreFolder(), "Test.txt", "text/plain");
        File reloadedFile2 = itm.getAction(file2.getId());
        Assert.assertFalse(reloadedFile1.getFileName().equalsIgnoreCase(reloadedFile2.getFileName()));
    }

    @Test
    public void testRenameCaseConflictingFiles() throws Exception {
        /*
         * create first file
         */
        File file1 = itm.createFileOnServer(getClient().getValues().getPrivateInfostoreFolder(), "test.txt", "text/plain");
        File reloadedFile1 = itm.getAction(file1.getId());
        /*
         * create another file
         */
        File file2 = itm.createFileOnServer(getClient().getValues().getPrivateInfostoreFolder(), "test2.txt", "text/plain");
        File reloadedFile2 = itm.getAction(file2.getId());
        /*
         * rename 2nd file to same name, ignoring case
         */
        DefaultFile file2Update = new DefaultFile();
        file2Update.setId(reloadedFile2.getId());
        file2Update.setFileName("Test.txt");
        itm.updateAction(file2Update, new File.Field[] { File.Field.FILENAME }, reloadedFile2.getLastModified());
        reloadedFile2 = itm.getAction(file2.getId());
        Assert.assertFalse(reloadedFile1.getFileName().equalsIgnoreCase(reloadedFile2.getFileName()));
    }

}
