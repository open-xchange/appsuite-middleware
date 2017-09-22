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

package com.openexchange.html;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug55406Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug55406Test extends AbstractSanitizing {

    public Bug55406Test() {
        super();
    }

    @Test
    public void testIntactLinks() throws Exception {
        String content = "<html>\n" +
            "<head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
            "</head>\n" +
            "<body>\n" +
            "<a href=\"https://secure2.onecallnow.com/Attachments/1111_8f2b0dde-995f-40d9-b59b-eefaa3a29384.doc\" target=\"_blank\">1.doc</a><br>\n" +
            "<a href=\"https://secure2.onecallnow.com/Attachments/1111_542841ff-82c8-43d8-8e54-eafa087a377d.doc\" target=\"_blank\">2.doc</a><br>\n" +
            "<a href=\"https://secure2.onecallnow.com/Attachments/1111_ca139c75-711f-4b7d-9b64-5e82dcc817c9.doc\" target=\"_blank\">3.doc</a><br>\n" +
            "<a href=\"https://secure2.onecallnow.com/Attachments/1111_f8cec4aa-6635-4e6e-9e77-4c2459ae628e.doc\" target=\"_blank\">4.doc</a><br>\n" +
            "<a href=\"https://secure2.onecallnow.com/Attachments/1111_ab732165-2282-4966-b427-3f0e0b8b1abb.doc\" target=\"_blank\">5.doc</a><br>\n" +
            "</body>\n" +
            "</html>";

        String sanitized = getHtmlService().sanitize(content, null, false, null, null);
        assertTrue("Unexpected content", sanitized.indexOf("onecallnow") > 0);
    }

}
