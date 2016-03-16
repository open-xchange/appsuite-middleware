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

package com.openexchange.filestore.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
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
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 1; i <= NUM_FILES_INIT; i++) {
            String content = "Content of file " + i;
            FILES.add(fs.saveNewFile(IS(content)));
        }
    }

    @Override
    public void tearDown() throws Exception {
        fs.remove();
        super.tearDown();
    }

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
