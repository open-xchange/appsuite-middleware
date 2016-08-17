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

package com.openexchange.mail.mime;

import junit.framework.TestCase;


/**
 * {@link ContentTypeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentTypeTest extends TestCase {

    /**
     * Initializes a new {@link ContentTypeTest}.
     */
    public ContentTypeTest() {
        super();
    }

    public void testTruncatedNameParameter() {
        try {
            String hdr = "application/pdf; name=The New York Times - Breaking News, World News & Multimedia.loc.pdf";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);
            String name = contentType.getNameParameter();

            assertEquals("Unexpected \"name\" parameter.", "The New York Times - Breaking News, World News & Multimedia.loc.pdf", name);

            assertEquals("Unexpected toString() result.", "application/pdf; name=\"The New York Times - Breaking News, World News & Multimedia.loc.pdf\"", contentType.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testWithCurlyBraces() {
        try {
            String hdr = "{\"application/octet-stream\"}; name=\"6N1911.pdf\"";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);

            assertEquals("Unexpected primary type", "application", contentType.getPrimaryType());
            assertEquals("Unexpected subtype", "octet-stream", contentType.getSubType());
            assertEquals("Unexpected name parameter", "6N1911.pdf", contentType.getNameParameter());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMalformedHeaderValue() {
        try {
            String hdr = "=?windows-1252?q?application/pdf; name=\"blatt8.pdf\"";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);

            assertEquals("Unexpected primary type", "application", contentType.getPrimaryType());
            assertEquals("Unexpected subtype", "pdf", contentType.getSubType());
            assertEquals("Unexpected name parameter", "blatt8.pdf", contentType.getNameParameter());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
