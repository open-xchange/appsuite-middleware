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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link HashingFileStorageMultithreadingTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashingFileStorageMultithreadingTest extends AbstractHashingFileStorageTest {

    private final int NUM_FILES = 10000;
    private final int NUM_THREADS = 10;

    public void testManyThreads() throws InterruptedException, OXException, IOException {
        List<FileSaverThread> fsThreads = new ArrayList<FileSaverThread>(10);
        for(int i = 0; i < NUM_THREADS; i++) {
            FileSaverThread thread = new FileSaverThread("Thread "+i);
            thread.start();
            fsThreads.add(thread);
        }

        for (FileSaverThread fileSaverThread : fsThreads) {
            fileSaverThread.join();
            fileSaverThread.checkConsistency();
        }
    }

    private class FileSaverThread extends Thread {

        private final String prefix;

        private final Map<String, String> dataMap = new HashMap<String, String>();

        public FileSaverThread(String prefix) {
            this.prefix = prefix;
        }

        public void checkConsistency() throws OXException, IOException {
            for(Map.Entry<String, String> entry: dataMap.entrySet()) {
                String fileId = entry.getKey();
                String data = entry.getValue();

                InputStream file = fs.getFile(fileId);
                InputStream expected = IS(data);

                int d = 0;
                while((d = file.read()) != -1) {
                    assertEquals(d, expected.read());
                }
                assertEquals(-1, expected.read());

            }
        }

        @Override
        public void run() {
            for (int i = 0; i < NUM_FILES; i++) {
                String data = prefix + " " + i;
                try {
                    String file = fs.saveNewFile(IS(data));
                    dataMap.put(file, data);
                } catch (OXException e) {
                    fail(e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    fail(e.getMessage());
                }
            }
        }

        public Map<String, String> getDataMap() {
            return dataMap;
        }
    }
}
