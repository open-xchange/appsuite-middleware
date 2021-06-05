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

package com.openexchange.html.vulntests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;


/**
 * {@link Bug26090VulTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug26090VulTest extends AbstractSanitizing {
     @Test
     public void testDoNotCreateLinkForUnsupportedProtocols() {
        String content = getHtmlService().formatURLs("skype:097711178851337", "commentId");
        String content2 = getHtmlService().formatURLs("javascript:alert(1)", "commentId");
        String content3 = getHtmlService().formatURLs("about:config", "commentId");
        //Create link for mailto
        String content4 = getHtmlService().formatURLs("mailto://myname@bar.tld", "commentId");

        assertEquals("Expected content ", content, "skype:097711178851337");
        assertEquals("Expected content ", content2, "javascript:alert(1)");
        assertEquals("Expected content ", content3, "about:config");
        assertEquals("Expected content ", content4, "<!--commentId <a href=\"mailto://myname@bar.tld\" target=\"_blank\">mailto://myname@bar.tld</a>-->");
    }
}
