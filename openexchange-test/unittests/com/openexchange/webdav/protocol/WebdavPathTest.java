package com.openexchange.webdav.protocol;

import junit.framework.TestCase;

import java.util.Arrays;

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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

public class WebdavPathTest extends TestCase {

    public void testStringConsturctor() {
        WebdavPath path = new WebdavPath("/i/am/a/path");
        assertComponents(path, "i","am","a","path");

        path = new WebdavPath("/i/am//a///path///");
        assertComponents(path, "i","am","a","path");
    }

    public void testStringsConstuctor() {
        WebdavPath path = new WebdavPath("i","am","a","path");
        assertComponents(path, "i","am","a","path");
    }

    public void testAppend(){
        WebdavPath path = new WebdavPath();
        path.append("i","am","a","path");
        assertComponents(path, "i","am","a","path");

        path = new WebdavPath();
        path.append(Arrays.asList("i", "am","a","path"));
        assertComponents(path, "i","am","a","path");

        path = new WebdavPath();
        path.append(new WebdavPath("i", "am","a","path"));
        assertComponents(path, "i","am","a","path");
    }

    public void testToString(){
        WebdavPath path = new WebdavPath("i","am","a","path");
        assertEquals("/i/am/a/path", path.toString());

    }

    public void testToEscapedString() {
        WebdavPath path = new WebdavPath("with/slash","with\\backslash");
        assertEquals("/with\\/slash/with\\\\backslash", path.toEscapedString());
    }

    public void testEquals(){
        WebdavPath path = new WebdavPath("/i/am/a/path");
        WebdavPath path2 = new WebdavPath("i","am","a","path");
        WebdavPath path3 = new WebdavPath();
        assertEquals(path, path2);
        assertEquals(path.hashCode(), path2.hashCode());
        assertFalse(path.equals(path3));
        assertFalse(path.hashCode() == path3.hashCode());
    }

    public void testParent() {
        WebdavPath path = new WebdavPath("/i/am/a/path");
        assertComponents(path.parent(), "i","am","a");
    }

    public void testName() {
        WebdavPath path = new WebdavPath("/i/am/a/path");
        assertEquals("path", path.name());
    }

    public void testStartsWith(){
        WebdavPath path = new WebdavPath("/i/am/a/path");
        WebdavPath path2 = new WebdavPath("/i/am/a");
        WebdavPath path3 = new WebdavPath("/i/am/a/path/below");
        WebdavPath path4 = new WebdavPath("/i/am/elsewhere");
        assertTrue(path.startsWith(path2));
        assertFalse(path.startsWith(path3));
        assertFalse(path.startsWith(path4));
    }

    public void testSubpath() {
        WebdavPath path = new WebdavPath("i","am", "a", "path");
        assertComponents(path.subpath(1),"am","a","path");
        assertComponents(path.subpath(0,2),"i", "am");
    }

    public static void assertComponents(WebdavPath path, String...components) {
        assertEquals(components.length,path.size());
        int i = 0;
        for(String component : path) {
            assertEquals(components[i++], component);
        }
    }

}
