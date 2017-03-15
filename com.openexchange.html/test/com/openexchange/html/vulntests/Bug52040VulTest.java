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
