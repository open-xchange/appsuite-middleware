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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link ContentDispositionTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentDispositionTest {

    /**
     * Initializes a new {@link ContentDispositionTest}.
     */
    public ContentDispositionTest() {
        super();
    }

    @Test
    public void testFileNameParameterFromBogusHeaders() {
        try {
            String hdr = "attachment; filename=Jana's application 4 cell phone.rtf";
            String fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Jana's application 4 cell phone.rtf", fileName);

            hdr = "attachment; filename*0=Test - Test.pdf";
            fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Test - Test.pdf", fileName);

            hdr = "filename=\"Scan1852.pdf\"; filename=\"Scan1852.pdf\"";
            fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Scan1852.pdf", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testFileNameParameterFromRFC2231EncodedParameters() {
        try {
            ContentDisposition cd = new ContentDisposition("attachment; filename*0*=iso-2022-jp''%1B%24%42%23%38%37%6E%24%4E%4D%3C%34%29%25%33%25; filename*1*=%69%25%60%3C%42%40%53%1B%28%42%2E%64%6F%63%78");
            assertEquals("Unpexpected \"filename\" parameter.", "\uff18\u6708\u306e\u5915\u520a\u30b3\u30e9\u30e0\u5b9f\u7e3e.docx", cd.getFilenameParameter());
        } catch (OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
