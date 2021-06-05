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

package com.openexchange.html;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
