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

package com.openexchange.tagging;

import java.util.List;
import junit.framework.TestCase;

/**
 * {@link TaggingSQLBuilderTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TaggingSQLBuilderTest extends TestCase {

    public void testOneTag() {
        SQLStatement statement = buildSql("myTag");
        assertEquals("SELECT * FROM tags WHERE tag = ? ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(1, tags.size());
        assertEquals("myTag", tags.get(0));
    }

    public void testOr() {
        SQLStatement statement = buildSql("myTag OR myOtherTag");
        assertEquals("SELECT * FROM tags WHERE tag = ? OR tag = ? ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(2, tags.size());
        assertEquals("myTag", tags.get(0));
        assertEquals("myOtherTag", tags.get(1));
    }

    public void testAnd() {
        SQLStatement statement = buildSql("myTag AND myOtherTag");
        assertEquals("SELECT * FROM tags WHERE tag = ? AND tag = ? ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(2, tags.size());
        assertEquals("myTag", tags.get(0));
        assertEquals("myOtherTag", tags.get(1));
    }

    public void testNot() {
        SQLStatement statement = buildSql("myTag AND NOT myOtherTag");
        assertEquals("SELECT * FROM tags WHERE tag = ? AND tag != ? ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(2, tags.size());
        assertEquals("myTag", tags.get(0));
        assertEquals("myOtherTag", tags.get(1));
    }

    public void testBrackets() {
        SQLStatement statement = buildSql("myTag AND (myOtherTag OR myThirdTag)");
        assertEquals("SELECT * FROM tags WHERE tag = ? AND ( tag = ? OR tag = ? ) ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(3, tags.size());
        assertEquals("myTag", tags.get(0));
        assertEquals("myOtherTag", tags.get(1));
        assertEquals("myThirdTag", tags.get(2));
    }
    
    public void testNegatedBrackets() {
        SQLStatement statement = buildSql("myTag AND NOT (myOtherTag OR myThirdTag)");
        assertEquals("SELECT * FROM tags WHERE tag = ? AND NOT ( tag = ? OR tag = ? ) ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(3, tags.size());
        assertEquals("myTag", tags.get(0));
        assertEquals("myOtherTag", tags.get(1));
        assertEquals("myThirdTag", tags.get(2));
    }
    
    public void testKrass() {
        SQLStatement statement = buildSql("tag1 OR ( NOT tag2 AND (tag3 OR NOT (NOT tag4 AND tag5) OR NOT tag6))");
        assertEquals("SELECT * FROM tags WHERE tag = ? OR ( tag != ? AND ( tag = ? OR NOT ( tag != ? AND tag = ? ) OR tag != ? ) ) ", statement.getSQLString());
        List<String> tags = statement.getTags();
        assertEquals(6, tags.size());
        for(int i = 1; i <= 6; i++) {
            assertEquals("tag"+i, tags.get(i-1));
        }
    }

    /**
     * @param string
     * @return
     */
    private SQLStatement buildSql(String string) {
        return new TaggingSQLBuilder().build(string);
    }

}
