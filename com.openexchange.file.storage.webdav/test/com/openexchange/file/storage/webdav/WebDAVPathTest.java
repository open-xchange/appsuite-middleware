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

package com.openexchange.file.storage.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import org.junit.Test;

/**
 * {@link WebDAVPathTest}
 *
 * Tests operations on WebDAV paths
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class WebDAVPathTest {

    @Test
    public void testPathsFromURIs() {
        URI uri = URI.create("/users/888/files/");
        WebDAVPath path = new WebDAVPath(uri);
        assertTrue(path.isCollection());
        assertEquals("files", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/"), path.getParent().toURI());

        uri = URI.create("/users/888/files/test.txt");
        path = new WebDAVPath(uri);
        assertFalse(path.isCollection());
        assertEquals("test.txt", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/files/"), path.getParent().toURI());

        uri = URI.create("/users/888/files/m%c3%bctze.txt");
        path = new WebDAVPath(uri);
        assertFalse(path.isCollection());
        assertEquals("mütze.txt", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/files/"), path.getParent().toURI());

        uri = URI.create("/users/888/files/kleine%20wurst.txt");
        path = new WebDAVPath(uri);
        assertFalse(path.isCollection());
        assertEquals("kleine wurst.txt", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/files/"), path.getParent().toURI());

        uri = URI.create("/users/888/files/Gro%C3%9Fe%20Wurst.txt");
        path = new WebDAVPath(uri);
        assertFalse(path.isCollection());
        assertEquals("Große Wurst.txt", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/files/"), path.getParent().toURI());

        uri = URI.create("/users/888/files/New%20Folder/New%20Folder/");
        path = new WebDAVPath(uri);
        assertTrue(path.isCollection());
        assertEquals("New Folder", path.getName());
        assertEquals(uri, path.toURI());
        assertEquals(URI.create("/users/888/files/New%20Folder/"), path.getParent().toURI());

    }

}
