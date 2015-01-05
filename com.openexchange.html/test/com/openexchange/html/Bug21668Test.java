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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * {@link Bug21668Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21668Test extends AbstractSanitizing {

    @Test
    public void testPlainTextQuoting() {
        String htmlContent = "<p style=\"margin: 0;\"><span><span>Foo bar</span></span></p>" +
            "<div style=\"margin: 5px 0px 5px 0px;\">" +
            "<br/>On April 1, 2015 at 11:20 AM &#34;Foo Bar&#34; &#60;foo@bar.invalid&#62; wrote:" +
            "<br/>" +
            "<br/> " +
            "<div style=\"position: relative;\">" +
            "<blockquote style=\"margin-left: 0px; padding-left: 10px; border-left: solid 1px blue;\">" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent nisl diam, egestas " +
            "<br/> vulputate bibendum at, ornare pulvinar odio." +
            "<br/> Ut vulputate purus purus, ac congue ipsum mattis vel. Suspendisse potenti." +
            "<br/> Duis nec nunc lectus. Suspendisse maximus fermentum egestas. Nulla sed mauris " +
            "<br/> nec lorem blandit finibus quis vel velit. Cras lacinia non neque sed pellentesque. " +
            "<br/> Nullam id eleifend ante. Integer vitae sagittis ante. Morbi egestas ligula sed " +
            "<br/> consectetur ornare. Integer sit amet mauris nisl. In porttitor, ligula at tincidunt " +
            "<br/> fermentum, sapien leo facilisis arcu, vel finibus ligula ex vitae neque. Sed finibus " +
            "<br/> mollis ultrices." +
            "</blockquote>" +
            "</div>" +
            "</div>" +
            "<p style=\"margin: 0px;\">&#160;</p>" +
            "<p style=\"margin: 0px;\">Morbi hendrerit aliquet laoreet. Nulla semper, diam ac ullamcorper rhoncus, felis lectus ullamcorper turpis, tincidunt euismod massa nibh sit amet dui. Nunc tristique ante a ex sollicitudin pulvinar.</p>" +
            "<p style=\"margin: 0px;\">&#160;</p> " +
            "<p style=\"margin: 0px;\">Cras aliquet laoreet ligula at laoreet. Sed facilisis viverra elit nec lobortis. Quisque et faucibus purus. Fusce efficitur est vel mi sodales gravida. Aenean hendrerit rutrum condimentum. </p>" +
            "<p style=\"margin: 0px;\">&#160;</p>";
        String actual = getHtmlService().html2text(htmlContent, false);

        String expected = "Foo bar\r\n\r\n\r\n" +
            "On April 1, 2015 at 11:20 AM \"Foo Bar\" <foo@bar.invalid> wrote:\r\n\r\n\r\n" +
            "> Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent nisl diam, egestas\r\n" +
            ">  vulputate bibendum at, ornare pulvinar odio.\r\n" +
            ">  Ut vulputate purus purus, ac congue ipsum mattis vel. Suspendisse potenti.\r\n" +
            ">  Duis nec nunc lectus. Suspendisse maximus fermentum egestas. Nulla sed mauris\r\n" +
            ">  nec lorem blandit finibus quis vel velit. Cras lacinia non neque sed pellentesque.\r\n" +
            ">  Nullam id eleifend ante. Integer vitae sagittis ante. Morbi egestas ligula sed\r\n" +
            ">  consectetur ornare. Integer sit amet mauris nisl. In porttitor, ligula at tincidunt\r\n" +
            ">  fermentum, sapien leo facilisis arcu, vel finibus ligula ex vitae neque. Sed finibus\r\n" +
            ">  mollis ultrices.\r\n" +
            "> \r\n\r\n \r\n" +
            "Morbi hendrerit aliquet laoreet. Nulla semper, diam ac ullamcorper rhoncus, felis lectus ullamcorper turpis, tincidunt euismod massa nibh sit amet dui. Nunc tristique ante a ex sollicitudin pulvinar.\r\n \r\n" +
            "Cras aliquet laoreet ligula at laoreet. Sed facilisis viverra elit nec lobortis. Quisque et faucibus purus. Fusce efficitur est vel mi sodales gravida. Aenean hendrerit rutrum condimentum.\r\n \r\n";

        assertEquals("Quoted HTML text was not parsed to quoted plain-text correctly", expected, actual);
    }
}
