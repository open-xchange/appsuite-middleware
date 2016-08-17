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

package com.openexchange.snippet.mime;

import java.util.Set;
import junit.framework.TestCase;


/**
 * {@link MimeSnippetManagementTest}
 */
public class MimeSnippetManagementTest extends TestCase {

    /**
     * Initializes a new {@link MimeSnippetManagementTest}.
     */
    public MimeSnippetManagementTest() {
        super();
    }

    public void testForBug38201() {
        String htmlContent = "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\">\n" +
            "<a style=\"text-decoration:none\" href=\"http://www.testfirma.test\" >\n" +
            "<img src=\"https://my.cool.image/as34/foo.png\" alt=\"TESTfirma\" height=\"73\" width=\"360\" border=\"0\">\n" +
            "</a>\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica, Arial, sans-serif; font-size: 10px; line-height: 12px; color: #212121;\"><span style=\"font-weight: bold; display: inline;\" >testmann</span>\n" +
            "<span style=\"display: inline;\" >/</span> <span style=\"display: inline;\" >CEO</span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<a href=\"mailto:test@test.de\" style=\"text-decoration: none; display: inline;\">test@test.de</a><span style=\"display: inline;\" > / </span><span style=\"display: inline;\" >123456789</span></p>\n" +
            "<p style=\"font-family: Helvetica, Arial, sans-serif; font-size: 10px; line-height: 12px\">\n" +
            "<span style=\"font-weight: bold; display: inline;\" >TESTfirma</span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<span style=\"display: inline;\" >Office: </span> <span style=\"display: inline;\" >456789</span>\n" +
            "<span style=\"display: inline;\" >/ Fax: </span> <span style=\"display: inline;\" >879564521</span>\n" +
            "<span style=\"display: inline;\" ><br></span> <span style=\"display: inline;\" >Teststraße 12</span>\n" +
            "<span ></span> <span style=\"\" ></span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<a href=\"http://www.testfirma.test\" style=\"text-decoration: none; display: inline;\">http://www.testfirma.test</a>\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\">\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\" >\n" +
            "<a href=\"https://https://my.cool.image?utm_source=email&amp;utm_medium=sig&amp;utm_campaign=email%20banner\">\n" +
            "<img src=\"https://my.cool.image/as34/banner.png\" alt=\"signature.biz\" height=\"35\" width=\"300\" border=\"0\">\n" +
            "</a>\n" +
            "</p>\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 9px; line-height: 12px;\" ></p>";

        Set<String> contentIDs = MimeSnippetManagement.extractContentIDs(htmlContent);
        assertNotNull(contentIDs);
        assertTrue(contentIDs.isEmpty());
    }

}
