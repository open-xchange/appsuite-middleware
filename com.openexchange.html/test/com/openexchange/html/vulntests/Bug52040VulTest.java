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
 * {@link Bug52040VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug52040VulTest extends AbstractSanitizing {

    public Bug52040VulTest() {
        super();
    }

    @Test
    public void testDroppedGlobalEventHandler() {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head><body>\n" +
            "\n" +
            "<time onafterprint=\"console.log('onafterprint')\" \n" +
            "onbeforeprint=\"console.log('onbeforeprint')\" \n" +
            "onbeforeunload=\"console.log('onbeforeunload')\" \n" +
            "onerror=\"console.log('onerror')\" \n" +
            "onhashchange=\"console.log('onhashchange')\" \n" +
            "onload=\"console.log('onload')\" onmessage=\"console.log('onmessage')\" \n" +
            "onoffline=\"console.log('onoffline')\" ononline=\"console.log('ononline')\" \n" +
            "onpagehide=\"console.log('onpagehide')\" \n" +
            "onpageshow=\"console.log('onpageshow')\" \n" +
            "onpopstate=\"console.log('onpopstate')\" \n" +
            "onresize=\"console.log('onresize')\" onstorage=\"console.log('onstorage')\" \n" +
            "onunload=\"console.log('onunload')\" onblur=\"console.log('onblur')\" \n" +
            "onchange=\"console.log('onchange')\" \n" +
            "oncontextmenu=\"console.log('oncontextmenu')\" \n" +
            "onfocus=\"console.log('onfocus')\" oninput=\"console.log('oninput')\" \n" +
            "oninvalid=\"console.log('oninvalid')\" onreset=\"console.log('onreset')\" \n" +
            "onsearch=\"console.log('onsearch')\" onselect=\"console.log('onselect')\" \n" +
            "onsubmit=\"console.log('onsubmit')\" onkeydown=\"console.log('onkeydown')\" \n" +
            "onkeypress=\"console.log('onkeypress')\" onkeyup=\"console.log('onkeyup')\" \n" +
            "onclick=\"console.log('onclick')\" ondblclick=\"console.log('ondblclick')\" \n" +
            "ondrag=\"console.log('ondrag')\" ondragend=\"console.log('ondragend')\" \n" +
            "ondragenter=\"console.log('ondragenter')\" \n" +
            "ondragleave=\"console.log('ondragleave')\" \n" +
            "ondragover=\"console.log('ondragover')\" \n" +
            "ondragstart=\"console.log('ondragstart')\" ondrop=\"console.log('ondrop')\" \n" +
            "onmousedown=\"console.log('onmousedown')\" \n" +
            "onmousemove=\"console.log('onmousemove')\" \n" +
            "onmouseout=\"console.log('onmouseout')\" \n" +
            "onmouseover=\"console.log('onmouseover')\" \n" +
            "onmouseup=\"console.log('onmouseup')\" \n" +
            "onmousewheel=\"console.log('onmousewheel')\" \n" +
            "onscroll=\"console.log('onscroll')\" onwheel=\"console.log('onwheel')\" \n" +
            "oncopy=\"console.log('oncopy')\" oncut=\"console.log('oncut')\" \n" +
            "onpaste=\"console.log('onpaste')\" onabort=\"console.log('onabort')\" \n" +
            "oncanplay=\"console.log('oncanplay')\" \n" +
            "oncanplaythrough=\"console.log('oncanplaythrough')\" \n" +
            "oncuechange=\"console.log('oncuechange')\" \n" +
            "ondurationchange=\"console.log('ondurationchange')\" \n" +
            "onemptied=\"console.log('onemptied')\" onended=\"console.log('onended')\" \n" +
            "onloadeddata=\"console.log('onloadeddata')\" \n" +
            "onloadedmetadata=\"console.log('onloadedmetadata')\" \n" +
            "onloadstart=\"console.log('onloadstart')\" \n" +
            "onpause=\"console.log('onpause')\" onplay=\"console.log('onplay')\" \n" +
            "onplaying=\"console.log('onplaying')\" \n" +
            "onprogress=\"console.log('onprogress')\" \n" +
            "onratechange=\"console.log('onratechange')\" \n" +
            "onseeked=\"console.log('onseeked')\" onseeking=\"console.log('onseeking')\" \n" +
            "onstalled=\"console.log('onstalled')\" \n" +
            "onsuspend=\"console.log('onsuspend')\" \n" +
            "ontimeupdate=\"console.log('ontimeupdate')\" \n" +
            "onvolumechange=\"console.log('onvolumechange')\" \n" +
            "onwaiting=\"console.log('onwaiting')\" onshow=\"console.log('onshow')\" \n" +
            "ontoggle=\"console.log('ontoggle')\">\n" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. </time>\n" +
            "\n" +
            "</body></html>";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "onerror", "ontoggle", "onafterprint");
    }

}
