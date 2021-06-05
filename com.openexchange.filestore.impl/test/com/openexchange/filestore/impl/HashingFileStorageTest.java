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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import org.junit.Test;
import com.openexchange.exception.OXException;


/**
 * {@link HashingFileStorageTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashingFileStorageTest extends AbstractHashingFileStorageTest {


         @Test
     public void testLifecycle() throws Exception {
        String data = "I am nice data";
        String fileId = fs.saveNewFile( IS(data) );

        InputStream file = fs.getFile(fileId);
        InputStream compare = IS(data);

        int i = 0;
        while ((i = file.read()) != -1) {
            assertEquals(i, compare.read());
        }
        assertEquals(-1, compare.read());

        file.close();
        compare.close();
    }

         @Test
     public void testListFiles() throws Exception{
        Map<String, Integer> files = new HashMap<String, Integer>();
        for(int i = 0; i < 10; i++) {
            String data = "I am nice data in the file number "+i;
            String fileId = fs.saveNewFile( IS(data) );
            files.put(fileId, I(i));
        }

        SortedSet<String> fileList = fs.getFileList();

        assertEquals(files.size(), fileList.size());

        for(Map.Entry<String, Integer> entry : files.entrySet()) {
            assertTrue(entry.getKey()+" missing in: "+fileList, fileList.contains(entry.getKey()));
        }
    }

         @Test
     public void testRemove() throws Exception {
        Map<String, Integer> files = new HashMap<String, Integer>();
        for(int i = 0; i < 10; i++) {
            String data = "I am nice data in the file number "+i;
            String fileId = fs.saveNewFile( IS(data) );
            files.put(fileId, I(i));
        }

        fs.remove();

        String[] list = tmpFile.list();
        assertTrue(list == null || list.length == 0);
    }

         @Test
     public void testBug34249() throws Exception {
        List<String> files = new ArrayList<String>(100);

        //fill file storage with files
        for (int i = 1; i <= 100; i++) {
            String content = "Content of file " + i;
            files.add(fs.saveNewFile(IS(content)));
        }

        //Remove all from storage
        for (String identifier : files) {
            fs.deleteFile(identifier);
        }

        String[] folders = fs.storage.list();
        assertTrue("Empty folders were not deleted", folders.length == 0);
    }

         @Test
     public void testBug34249WithNonemptyFolders() throws Exception {
        List<String> files = new ArrayList<String>(100);

        //fill file storage with files
        for (int i = 1; i <= 100; i++) {
            String content = "Content of file " + i;
            files.add(fs.saveNewFile(IS(content)));
        }

        //Create file in random folder
        int rnd = new Random(System.currentTimeMillis()).nextInt(99);
        String fileId = files.get(rnd);
        File parent = new File(fs.storage.getAbsolutePath() + File.separatorChar + fileId);
        boolean tmpFile = new File(parent.getParent() + System.currentTimeMillis()).createNewFile();
        assertTrue("Could not create file", tmpFile);

        //Remove all from storage
        for (String identifier : files) {
            fs.deleteFile(identifier);
        }

        String[] folders = fs.storage.list();
        assertTrue("Nonempty folders were deleted", folders.length == 1);
    }

    // Error Cases

         @Test
     public void testReadUnknownID() {
        try {
            fs.getFile("fantasyName");
            fail("Could read unkown file");
        } catch (OXException e) {
        }
    }

         @Test
     public void testDeleteUnknownID() throws Exception {
        assertFalse(fs.deleteFile("fantasyName"));
    }

         @Test
     public void testDeleteUnknownIDs() throws Exception {
        String data = "I am nice data";
        String fileId = fs.saveNewFile( IS(data) );

        Set<String> notDeleted = fs.deleteFiles(new String[]{"file1", fileId, "file2", "file3"});

        assertEquals(3, notDeleted.size());
        assertTrue(notDeleted.contains("file1"));
        assertTrue(notDeleted.contains("file2"));
        assertTrue(notDeleted.contains("file3"));

    }


}
