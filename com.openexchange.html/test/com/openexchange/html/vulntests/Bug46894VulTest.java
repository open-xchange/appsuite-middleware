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

import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;

/**
 * {@link Bug46894VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug46894VulTest extends AbstractSanitizing {

    public Bug46894VulTest() {
        super();
    }

     @Test
     public void testScriptTagSanitizing() {
        String content = "<!DOCTYPE html>\n" + 
            "<html><body>\n" + 
            "<a href=\"data:text/html,<script>alert(document.domain);</script>\">click me</a>\n" + 
            "<a href=\"data:text/html,<script>alert(document.cookie);</script>\">click me</a>";

        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "<script", "alert");
    }
}
