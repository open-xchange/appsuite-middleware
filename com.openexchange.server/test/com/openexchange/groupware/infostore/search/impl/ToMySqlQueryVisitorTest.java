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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.search.impl;

import java.util.regex.Pattern;
import junit.framework.TestCase;
import com.openexchange.groupware.infostore.search.DescriptionTerm;
import com.openexchange.groupware.infostore.search.FileNameTerm;


/**
 * {@link ToMySqlQueryVisitorTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ToMySqlQueryVisitorTest extends TestCase {

    private final static Pattern PATTERN1 = Pattern.compile("AND[ \t\r\n\f]*OR");
    private final static Pattern PATTERN2 = Pattern.compile("[ ]{2,}");

    /**
     * Initializes a new {@link ToMySqlQueryVisitorTest}.
     */
    public ToMySqlQueryVisitorTest() {
        super();
    }

    public void testSqlPattern() {

        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", true, true);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("UPPER(infostore_document.description) LIKE UPPER('%bluber blah_foo%')"));

        dtz = new DescriptionTerm("bluber blah?foo", false, true);
        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE '%bluber blah_foo%'"));

        dtz = new DescriptionTerm("*bluber %blah?foo*", false, false);
        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE '%bluber \\%blah_foo%'"));

        dtz = new DescriptionTerm("bluber_blah", false, false);
        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description = 'bluber_blah'"));

        dtz = new DescriptionTerm("bluber_blah", false, true);
        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description LIKE '%bluber\\_blah%'"));

        dtz = new DescriptionTerm("bluber\\blah", false, false);
        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query: " + result, result.endsWith("infostore_document.description = 'bluber\\\\blah'"));

    }

    public void testFolders() {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(new int[] { 119 }, new int[] {120}, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.getMySqlQuery();
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore.folder_id = 119 OR (infostore.folder_id = 120 AND"
            + " infostore.created_by = 1) AND infostore_document.description LIKE '%bluber blah_foo%'"));
    }

    public void testWithoutAllFolders() {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(null, new int[] {120}, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.getMySqlQuery();
        assertFalse("Invalid SQL query", PATTERN1.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND (infostore.folder_id = 120 AND"
            + " infostore.created_by = 1) AND infostore_document.description LIKE '%bluber blah_foo%'"));

        visitor = new ToMySqlQueryVisitor(new int[0], new int[] {120}, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Invalid SQL query", PATTERN1.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND (infostore.folder_id = 120 AND"
            + " infostore.created_by = 1) AND infostore_document.description LIKE '%bluber blah_foo%'"));
    }

    public void testWithoutOwnFolders() {
        DescriptionTerm dtz = new DescriptionTerm("*bluber blah?foo*", false, false);
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(new int[] { 119 }, null, 1, 1, "SELECT field01");
        visitor.visit(dtz);
        String result = visitor.getMySqlQuery();
        assertFalse("Invalid SQL query", PATTERN1.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore.folder_id = 119 AND infostore_document.description LIKE '%bluber blah_foo%'"));

        visitor = new ToMySqlQueryVisitor(new int[] { 119 }, new int[0], 1, 1, "SELECT field01");
        visitor.visit(dtz);
        result = visitor.getMySqlQuery();
        assertFalse("Invalid SQL query", PATTERN1.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.endsWith("AND infostore.folder_id = 119 AND infostore_document.description LIKE '%bluber blah_foo%'"));
    }

    private String EXPECTED = "SELECT field01 FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = 1 AND infostore.folder_id = 119 OR (infostore.folder_id = 120 AND infostore.created_by = 1) AND UPPER(infostore_document.filename) LIKE UPPER('%test123%') LIMIT 0,5";
    public void testWithLimit() {
        FileNameTerm term = new FileNameTerm("test123");
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(new int[] { 119 }, new int[] { 120 }, 1, 1, "SELECT field01", 0, 5);
        visitor.visit(term);
        String result = visitor.getMySqlQuery();
        assertFalse("Invalid SQL query", PATTERN1.matcher(result).matches());
        assertFalse("Unneccessary whitespaces in query", PATTERN2.matcher(result).matches());
        assertTrue("Unexpected SQL query", result.equals(EXPECTED));
    }

}
