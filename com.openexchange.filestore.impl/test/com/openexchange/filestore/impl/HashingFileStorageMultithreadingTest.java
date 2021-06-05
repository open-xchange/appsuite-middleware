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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link HashingFileStorageMultithreadingTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashingFileStorageMultithreadingTest extends AbstractHashingFileStorageTest {

    private final int NUM_FILES = 10000;
    private final int NUM_THREADS = 10;

         @Test
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
                while ((d = file.read()) != -1) {
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
    }
}
