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

package com.openexchange.filestore.impl;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link ConcurrentSaveDeleteHashingFileStorageTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class ConcurrentSaveDeleteHashingFileStorageTest extends AbstractHashingFileStorageTest {

    private final List<String> FILES = new CopyOnWriteArrayList<String>();

    private final int NUM_FILES_INIT = 100;

    private final int NUM_OPERATIONS = 1000;

    /**
     * Initializes a new {@link ConcurrentSaveDeleteHashingFileStorageTest}.
     */
    public ConcurrentSaveDeleteHashingFileStorageTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 1; i <= NUM_FILES_INIT; i++) {
            String content = "Content of file " + i;
            FILES.add(fs.saveNewFile(IS(content)));
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        fs.remove();
        super.tearDown();
    }

    @Test
    public void testBug34249ConcurrentThreads() throws Exception {
        FsThread save = new FsThread(true);
        FsThread delete = new FsThread(false);
        save.start();
        delete.start();

        try {
            save.join();
            delete.join();
        } catch (InterruptedException e) {
            //
        }

        if (save.hasException()) {
            fail("Exception while saving: " + save.getException().getMessage());
        }
        if (delete.hasException()) {
            fail("Exception while deleting: " + delete.getException().getMessage());
        }

        checkForEmptyFolders(fs.storage);
    }

    private void checkForEmptyFolders(File root) throws Exception {
        if (root.isDirectory()) {
            if (root.listFiles().length == 0) {
                fail("Empty folder found: " + root.getAbsolutePath());
            }
            for (File file : root.listFiles()) {
                checkForEmptyFolders(file);
            }
        }
    }

    private class FsThread extends Thread {

        private boolean exception;

        private final boolean save;

        private Exception ex;

        public FsThread(boolean save) {
            this.save = save;
            exception = false;
            ex = null;
        }

        public Exception getException() {
            return ex;
        }

        public boolean hasException() {
            return exception;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {

            for (int i = 1; i <= NUM_OPERATIONS; i++) {
                if (save) {
                    String content = "New file " + i;
                    try {
                        FILES.add(fs.saveNewFile(IS(content)));
                    } catch (UnsupportedEncodingException e) {
                        exception = false;
                        ex = e;
                        break;
                    } catch (OXException e) {
                        exception = false;
                        ex = e;
                        break;
                    }
                } else {
                    int id = new Random(System.currentTimeMillis()).nextInt(FILES.size());
                    try {
                        fs.deleteFile(FILES.get(id));
                    } catch (OXException e) {
                        exception = true;
                        ex = e;
                        break;
                    }
                }
            }
        }
    }

}
