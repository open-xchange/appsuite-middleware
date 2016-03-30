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

package com.openexchange.html.internal.css;

import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandler;
import com.openexchange.java.StringBufferStringer;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * Simple unit tests for {@link CSSMatcher}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ThreadPools.class })
public class CSSMatcherTest {

    @Test
    public void testDropComments() {
        try {
            String css = "{ font-size: 1px; line-height: 1px; margin: 0; padding: 0; } /* blah */";
            String droppedComments = CSSMatcher.dropComments(css);
            Assert.assertEquals("Unexpected CSS snippet", "{ font-size: 1px; line-height: 1px; margin: 0; padding: 0; }", droppedComments.trim());

            css = "body, table /* Some comment */{ font-size: 9pt; font-family: 'Courier New'; font-style: normal; }";
            droppedComments = CSSMatcher.dropComments(css);
            Assert.assertEquals("Unexpected CSS snippet", "body, table { font-size: 9pt; font-family: 'Courier New'; font-style: normal; }", droppedComments.trim());

            css = "/* blah */ body, table /* Some comment */{ font-size: 9pt; font-family: 'Courier New'; font-style: normal; } /* blubb */";
            droppedComments = CSSMatcher.dropComments(css);
            Assert.assertEquals("Unexpected CSS snippet", "body, table { font-size: 9pt; font-family: 'Courier New'; font-style: normal; }", droppedComments.trim());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCheckCSS_CSSwithNamespaceAndBodyWildcard_returnPreparedForMail() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "\n" + "            /* +++++++++++++++++++++ RESET +++++++++++++++++++++ */\n" + "            @namespace \"http://www.w3.org/1999/xhtml\";\n" + "\n" + "\n" + "            @namespace svg \"http://www.w3.org/2000/svg\";\n" + "\n" + "\n" + "            body * {\n" + "                font-size: 1px;\n" + "                line-height: 1px;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            abbr, address, article, aside, audio, b, blockquote, body, canvas, caption, cite, code, dd, del, details, dfn, div, dl, dt, em, embed, fieldset, figcaption, figure, font, footer, form, h1, h2, h3, h4, h5, h6, header, hgroup, html, i, iframe, img, ins, kbd, label, legend, li, mark, menu, nav, object, object, ol, p, pre, q, samp, section, small, span, strong, sub, summary, sup, table, tbody, td, tfoot, th, thead, time, tr, ul, var, video {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                border: 0;\n" + "                /*font-size: 100%; */\n" + "                font: inherit;\n" + "            }\n" + "\n" + "\n" + "            a, img, a img, iframe, form, fieldset, abbr, acronym, object, applet, table {\n" + "                border: 0 none transparent;\n" + "            }\n" + "\n" + "\n" + "            q {\n" + "                quotes: \"\" \"\";\n" + "            }\n" + "\n" + "\n" + "            ul, ol, dir, menu {\n" + "                list-style: none;\n" + "            }\n" + "\n" + "\n" + "            img {\n" + "                display: block;\n" + "                border: 0 none;\n" + "                outline: none;\n" + "                text-decoration: none;\n" + "                -ms-interpolation-mode: bicubic;\n" + "            }\n" + "\n" + "\n" + "            table td {\n" + "                border-collapse: collapse;\n" + "            }\n" + "\n" + "\n" + "            hr {\n" + "                display: block;\n" + "                height: 1px;\n" + "                border: 0;\n" + "                border-top: 1px solid #ccc;\n" + "                margin: 1em 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            table, tbody, td, th, tr, p, a, img {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            strong, b {\n" + "                font-weight: bold;\n" + "            }\n" + "\n" + "\n" + "            /* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */\n" + "            html {\n" + "                direction: ltr;\n" + "                text-align: left;\n" + "                writing-mode: lr-tb;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                /* START: Always force scrollbar in non-IE */\n" + "                overflow-y: scroll;\n" + "            }\n" + "\n" + "\n" + "            body {\n" + "                -webkit-text-size-adjust: none;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                font-size: 10px;\n" + "                font-size: 1rem;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-arialregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.svg#ff-arialregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "                font-stretch: normal;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-helveticaregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.svg#ff-helveticaregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "            }\n" + "\n" + "\n" + "            .ff-arialregular, .ff-arial, .ff-helvetica {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "            }\n" + "\n" + "\n" + "            font {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                font-weight: normal;\n" + "                font-size: 12px;\n" + "                font-weight: normal;\n" + "                font-variant: normal;\n" + "            }\n" + "\n" + "\n" + "            a:link, a:visited {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #000066;\n" + "            }\n" + "\n" + "\n" + "            a:hover, a:active {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #666666;\n" + "            }\n" + "\n" + "\n" + "            .ReadMsgBody {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            .ExternalClass {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            * .ExternalClass {\n" + "                line-height: 100%;\n" + "            }\n" + "\n" + "\n" + "            *[class].bgcolor_KV {\n" + "                background-color: #411d05 !important;\n" + "            }\n" + "\n" + "\n" + "            @media only screen and (max-width: 580px) {\n" + "                .ff-arialregular {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                }\n" + "                font {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                    font-weight: normal;\n" + "                    font-size: 12px;\n" + "                    font-weight: normal;\n" + "                    font-variant: normal;\n" + "                }\n" + "                body {\n" + "                    background-color: #e7e7e7 !important;\n" + "                }\n" + "                \n" + "                *[class].h {\n" + "                    display: none !important;\n" + "                    visibility: none !important;\n" + "                    mso-hide: all !important;\n" + "                    font-size: 1px !important;\n" + "                    line-height: 1px !important;\n" + "                    max-height: 0;\n" + "                    padding: 0;\n" + "                    margin: 0;\n" + "                    height: 1px;\n" + "                    width: 1px;\n" + "                }\n" + "                *[class].w {\n" + "                    display: block !important;\n" + "                    visibility: visible !important;\n" + "                }\n" + "                *[class].fl {\n" + "                    float: left !important;\n" + "                }\n" + "                *[class].w1 {\n" + "                    width: 1px !important;\n" + "                }\n" + "                *[class].w13 {\n" + "                    width: 13px !important;\n" + "                }\n" + "                *[class].w21 {\n" + "                    width: 21px !important;\n" + "                }\n" + "                *[class].w27 {\n" + "                    width: 27px !important;\n" + "                }\n" + "                *[class].w32 {\n" + "                    width: 32px !important;\n" + "                }\n" + "                *[class].w85 {\n" + "                    width: 85px !important;\n" + "                }\n" + "                *[class].w119 {\n" + "                    width: 119px !important;\n" + "                }\n" + "                *[class].w122 {\n" + "                    width: 122px !important;\n" + "                }\n" + "                *[class].w138 {\n" + "                    width: 138px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w239 {\n" + "                    width: 239px !important;\n" + "                }\n" + "                *[class].w250 {\n" + "                    width: 250px !important;\n" + "                }\n" + "                 *[class].w260 {\n" + "                    width: 260px !important;\n" + "                }\n" + "                *[class].w290 {\n" + "                    width: 290px !important;\n" + "                }\n" + "                *[class].w295 {\n" + "                    width: 295px !important;\n" + "                }\n" + "                *[class].w305 {\n" + "                    width: 305px !important;\n" + "                }\n" + "                *[class].w310 {\n" + "                    width: 310px !important;\n" + "                }\n" + "                *[class].w315 {\n" + "                    width: 315px !important;\n" + "                }\n" + "                *[class].w320 {\n" + "                    width: 320px !important;\n" + "                }\n" + "                *[class].autoh {\n" + "                    height: auto !important;\n" + "                }\n" + "                *[class].h15 {\n" + "                    height: 15px !important;\n" + "                }\n" + "                *[class].h16 {\n" + "                    height: 16px !important;\n" + "                }\n" + "                *[class].h24 {\n" + "                    height: 24px !important;\n" + "                }\n" + "                *[class].h25 {\n" + "                    height: 25px !important;\n" + "                }\n" + "                *[class].h27 {\n" + "                    height: 27px !important;\n" + "                }\n" + "                *[class].h46 {\n" + "                    height: 46px !important;\n" + "                }\n" + "                *[class].h47 {\n" + "                    height: 47px !important;\n" + "                }\n" + "                *[class].h75 {\n" + "                    height: 75px !important;\n" + "                }\n" + "                *[class].h76 {\n" + "                    height: 76px !important;\n" + "                }\n" + "                *[class].h146 {\n" + "                    height: 146px !important;\n" + "                }\n" + "                *[class].h159 {\n" + "                    height: 159px !important;\n" + "                }\n" + "                *[class].h331 {\n" + "                    height: 331px !important;\n" + "                }\n" + "                *[class].w85h27 {\n" + "                    background-size: 85px 27px !important;\n" + "                    height: 27px !important;...";

        CSSMatcher.checkCSS(cssBuffer.append(css), null, "test", true);

        final String saneCss = cssBuffer.toString().trim();
        final String[] lines = saneCss.split("\r?\n");

        final String line0 = lines[0].replaceAll("\\s+", " ");
        String expectedLine0 = "@namespace \"http://www.w3.org/1999/xhtml\";   @namespace svg \"http://www.w3.org/2000/svg\";   #test  * { font-size: 1px; line-height: 1px; margin: 0; padding: 0; }".replaceAll("\\s+", " ");
        assertTrue("Unexpected CSS in line0! Expected to start with: " + expectedLine0 + ", but was " + line0, line0.trim().startsWith(expectedLine0));

        final String line1 = lines[1].replaceAll("\\s+", " ");
        String expectedLine1 = "#test abbr , #test address , #test article , #test aside".replaceAll("\\s+", " ");
        assertTrue("Unexpected CSS in line1! Expected to start with: " + expectedLine1 + ", but was " + line1, line1.trim().startsWith(expectedLine1));

        final String line10 = lines[10].replaceAll("\\s+", " ");
        String expectedLine10 = "#test html { direction: ltr;".replaceAll("\\s+", " ");
        assertTrue("Unexpected CSS in line10! Expected to start with: " + expectedLine10 + ", but was " + line10.trim(), line10.trim().startsWith(expectedLine10));
    }

    @Test
    public void testCheckCSS_CSSwithoutPrefix_returnPreparedForMail() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "\n" + "            /* +++++++++++++++++++++ RESET +++++++++++++++++++++ */\n" + "            @namespace \"http://www.w3.org/1999/xhtml\";\n" + "\n" + "\n" + "            @namespace svg \"http://www.w3.org/2000/svg\";\n" + "\n" + "\n" + "            body * {\n" + "                font-size: 1px;\n" + "                line-height: 1px;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            abbr, address, article, aside, audio, b, blockquote, body, canvas, caption, cite, code, dd, del, details, dfn, div, dl, dt, em, embed, fieldset, figcaption, figure, font, footer, form, h1, h2, h3, h4, h5, h6, header, hgroup, html, i, iframe, img, ins, kbd, label, legend, li, mark, menu, nav, object, object, ol, p, pre, q, samp, section, small, span, strong, sub, summary, sup, table, tbody, td, tfoot, th, thead, time, tr, ul, var, video {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                border: 0;\n" + "                /*font-size: 100%; */\n" + "                font: inherit;\n" + "            }\n" + "\n" + "\n" + "            a, img, a img, iframe, form, fieldset, abbr, acronym, object, applet, table {\n" + "                border: 0 none transparent;\n" + "            }\n" + "\n" + "\n" + "            q {\n" + "                quotes: \"\" \"\";\n" + "            }\n" + "\n" + "\n" + "            ul, ol, dir, menu {\n" + "                list-style: none;\n" + "            }\n" + "\n" + "\n" + "            img {\n" + "                display: block;\n" + "                border: 0 none;\n" + "                outline: none;\n" + "                text-decoration: none;\n" + "                -ms-interpolation-mode: bicubic;\n" + "            }\n" + "\n" + "\n" + "            table td {\n" + "                border-collapse: collapse;\n" + "            }\n" + "\n" + "\n" + "            hr {\n" + "                display: block;\n" + "                height: 1px;\n" + "                border: 0;\n" + "                border-top: 1px solid #ccc;\n" + "                margin: 1em 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            table, tbody, td, th, tr, p, a, img {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            strong, b {\n" + "                font-weight: bold;\n" + "            }\n" + "\n" + "\n" + "            /* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */\n" + "            html {\n" + "                direction: ltr;\n" + "                text-align: left;\n" + "                writing-mode: lr-tb;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                /* START: Always force scrollbar in non-IE */\n" + "                overflow-y: scroll;\n" + "            }\n" + "\n" + "\n" + "            body {\n" + "                -webkit-text-size-adjust: none;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                font-size: 10px;\n" + "                font-size: 1rem;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-arialregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.svg#ff-arialregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "                font-stretch: normal;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-helveticaregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.svg#ff-helveticaregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "            }\n" + "\n" + "\n" + "            .ff-arialregular, .ff-arial, .ff-helvetica {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "            }\n" + "\n" + "\n" + "            font {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                font-weight: normal;\n" + "                font-size: 12px;\n" + "                font-weight: normal;\n" + "                font-variant: normal;\n" + "            }\n" + "\n" + "\n" + "            a:link, a:visited {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #000066;\n" + "            }\n" + "\n" + "\n" + "            a:hover, a:active {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #666666;\n" + "            }\n" + "\n" + "\n" + "            .ReadMsgBody {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            .ExternalClass {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            * .ExternalClass {\n" + "                line-height: 100%;\n" + "            }\n" + "\n" + "\n" + "            *[class].bgcolor_KV {\n" + "                background-color: #411d05 !important;\n" + "            }\n" + "\n" + "\n" + "            @media only screen and (max-width: 580px) {\n" + "                .ff-arialregular {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                }\n" + "                font {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                    font-weight: normal;\n" + "                    font-size: 12px;\n" + "                    font-weight: normal;\n" + "                    font-variant: normal;\n" + "                }\n" + "                body {\n" + "                    background-color: #e7e7e7 !important;\n" + "                }\n" + "                \n" + "                *[class].h {\n" + "                    display: none !important;\n" + "                    visibility: none !important;\n" + "                    mso-hide: all !important;\n" + "                    font-size: 1px !important;\n" + "                    line-height: 1px !important;\n" + "                    max-height: 0;\n" + "                    padding: 0;\n" + "                    margin: 0;\n" + "                    height: 1px;\n" + "                    width: 1px;\n" + "                }\n" + "                *[class].w {\n" + "                    display: block !important;\n" + "                    visibility: visible !important;\n" + "                }\n" + "                *[class].fl {\n" + "                    float: left !important;\n" + "                }\n" + "                *[class].w1 {\n" + "                    width: 1px !important;\n" + "                }\n" + "                *[class].w13 {\n" + "                    width: 13px !important;\n" + "                }\n" + "                *[class].w21 {\n" + "                    width: 21px !important;\n" + "                }\n" + "                *[class].w27 {\n" + "                    width: 27px !important;\n" + "                }\n" + "                *[class].w32 {\n" + "                    width: 32px !important;\n" + "                }\n" + "                *[class].w85 {\n" + "                    width: 85px !important;\n" + "                }\n" + "                *[class].w119 {\n" + "                    width: 119px !important;\n" + "                }\n" + "                *[class].w122 {\n" + "                    width: 122px !important;\n" + "                }\n" + "                *[class].w138 {\n" + "                    width: 138px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w239 {\n" + "                    width: 239px !important;\n" + "                }\n" + "                *[class].w250 {\n" + "                    width: 250px !important;\n" + "                }\n" + "                 *[class].w260 {\n" + "                    width: 260px !important;\n" + "                }\n" + "                *[class].w290 {\n" + "                    width: 290px !important;\n" + "                }\n" + "                *[class].w295 {\n" + "                    width: 295px !important;\n" + "                }\n" + "                *[class].w305 {\n" + "                    width: 305px !important;\n" + "                }\n" + "                *[class].w310 {\n" + "                    width: 310px !important;\n" + "                }\n" + "                *[class].w315 {\n" + "                    width: 315px !important;\n" + "                }\n" + "                *[class].w320 {\n" + "                    width: 320px !important;\n" + "                }\n" + "                *[class].autoh {\n" + "                    height: auto !important;\n" + "                }\n" + "                *[class].h15 {\n" + "                    height: 15px !important;\n" + "                }\n" + "                *[class].h16 {\n" + "                    height: 16px !important;\n" + "                }\n" + "                *[class].h24 {\n" + "                    height: 24px !important;\n" + "                }\n" + "                *[class].h25 {\n" + "                    height: 25px !important;\n" + "                }\n" + "                *[class].h27 {\n" + "                    height: 27px !important;\n" + "                }\n" + "                *[class].h46 {\n" + "                    height: 46px !important;\n" + "                }\n" + "                *[class].h47 {\n" + "                    height: 47px !important;\n" + "                }\n" + "                *[class].h75 {\n" + "                    height: 75px !important;\n" + "                }\n" + "                *[class].h76 {\n" + "                    height: 76px !important;\n" + "                }\n" + "                *[class].h146 {\n" + "                    height: 146px !important;\n" + "                }\n" + "                *[class].h159 {\n" + "                    height: 159px !important;\n" + "                }\n" + "                *[class].h331 {\n" + "                    height: 331px !important;\n" + "                }\n" + "                *[class].w85h27 {\n" + "                    background-size: 85px 27px !important;\n" + "                    height: 27px !important;...";

        CSSMatcher.checkCSS(cssBuffer.append(css), null, true);

        final String saneCss = cssBuffer.toString().trim();
        final String[] lines = saneCss.split("\r?\n");

        final String line0 = lines[0];
        String expectedLine0 = "/* +++++++++++++++++++++ RESET +++++++++++++++++++++ */ @namespace \"http://www.w3.org/1999/xhtml\";   @namespace svg \"http://www.w3.org/2000/svg\";   body * { font-size: 1px; line-height: 1px;";
        assertTrue("Unexpected CSS in line0! Expected to start with: " + expectedLine0 + ", but was " + line0, line0.trim().startsWith(expectedLine0.trim()));

        final String line1 = lines[1];
        String expectedLine1 = "abbr, address, article, aside, audio, b, blockquote,";
        assertTrue("Unexpected CSS in line1! Expected to start with: " + expectedLine1 + ", but was " + line1.trim(), line1.trim().startsWith(expectedLine1.trim()));

        final String line10 = lines[10];
        String expectedLine10 = "/* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */ html {";
        assertTrue("Unexpected CSS in line10! Expected to start with: " + expectedLine10 + ", but was " + line10, line10.trim().startsWith(expectedLine10.trim()));
    }

    @Test
    public void testCheckCSSElements_CSSNull_ModifiedFalse() {
        boolean checkCSSElements = CSSMatcher.checkCSSElements(null, new HashMap<String, Set<String>>(), false);

        assertFalse(checkCSSElements);
    }

    @Test
    public void testCheckCSSElements_StyleMapNull_ModifiedFalse() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "\n" + "            /* +++++++++++++++++++++ RESET +++++++++++++++++++++ */\n" + "            @namespace \"http://www.w3.org/1999/xhtml\";\n" + "\n" + "\n" + "            @namespace svg \"http://www.w3.org/2000/svg\";\n" + "\n" + "\n" + "            body * {\n" + "                font-size: 1px;\n" + "                line-height: 1px;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            abbr, address, article, aside, audio, b, blockquote, body, canvas, caption, cite, code, dd, del, details, dfn, div, dl, dt, em, embed, fieldset, figcaption, figure, font, footer, form, h1, h2, h3, h4, h5, h6, header, hgroup, html, i, iframe, img, ins, kbd, label, legend, li, mark, menu, nav, object, object, ol, p, pre, q, samp, section, small, span, strong, sub, summary, sup, table, tbody, td, tfoot, th, thead, time, tr, ul, var, video {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                border: 0;\n" + "                /*font-size: 100%; */\n" + "                font: inherit;\n" + "            }\n" + "\n" + "\n" + "            a, img, a img, iframe, form, fieldset, abbr, acronym, object, applet, table {\n" + "                border: 0 none transparent;\n" + "            }\n" + "\n" + "\n" + "            q {\n" + "                quotes: \"\" \"\";\n" + "            }\n" + "\n" + "\n" + "            ul, ol, dir, menu {\n" + "                list-style: none;\n" + "            }\n" + "\n" + "\n" + "            img {\n" + "                display: block;\n" + "                border: 0 none;\n" + "                outline: none;\n" + "                text-decoration: none;\n" + "                -ms-interpolation-mode: bicubic;\n" + "            }\n" + "\n" + "\n" + "            table td {\n" + "                border-collapse: collapse;\n" + "            }\n" + "\n" + "\n" + "            hr {\n" + "                display: block;\n" + "                height: 1px;\n" + "                border: 0;\n" + "                border-top: 1px solid #ccc;\n" + "                margin: 1em 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            table, tbody, td, th, tr, p, a, img {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            strong, b {\n" + "                font-weight: bold;\n" + "            }\n" + "\n" + "\n" + "            /* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */\n" + "            html {\n" + "                direction: ltr;\n" + "                text-align: left;\n" + "                writing-mode: lr-tb;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                /* START: Always force scrollbar in non-IE */\n" + "                overflow-y: scroll;\n" + "            }\n" + "\n" + "\n" + "            body {\n" + "                -webkit-text-size-adjust: none;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                font-size: 10px;\n" + "                font-size: 1rem;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-arialregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.svg#ff-arialregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "                font-stretch: normal;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-helveticaregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.svg#ff-helveticaregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "            }\n" + "\n" + "\n" + "            .ff-arialregular, .ff-arial, .ff-helvetica {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "            }\n" + "\n" + "\n" + "            font {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                font-weight: normal;\n" + "                font-size: 12px;\n" + "                font-weight: normal;\n" + "                font-variant: normal;\n" + "            }\n" + "\n" + "\n" + "            a:link, a:visited {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #000066;\n" + "            }\n" + "\n" + "\n" + "            a:hover, a:active {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #666666;\n" + "            }\n" + "\n" + "\n" + "            .ReadMsgBody {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            .ExternalClass {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            * .ExternalClass {\n" + "                line-height: 100%;\n" + "            }\n" + "\n" + "\n" + "            *[class].bgcolor_KV {\n" + "                background-color: #411d05 !important;\n" + "            }\n" + "\n" + "\n" + "            @media only screen and (max-width: 580px) {\n" + "                .ff-arialregular {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                }\n" + "                font {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                    font-weight: normal;\n" + "                    font-size: 12px;\n" + "                    font-weight: normal;\n" + "                    font-variant: normal;\n" + "                }\n" + "                body {\n" + "                    background-color: #e7e7e7 !important;\n" + "                }\n" + "                \n" + "                *[class].h {\n" + "                    display: none !important;\n" + "                    visibility: none !important;\n" + "                    mso-hide: all !important;\n" + "                    font-size: 1px !important;\n" + "                    line-height: 1px !important;\n" + "                    max-height: 0;\n" + "                    padding: 0;\n" + "                    margin: 0;\n" + "                    height: 1px;\n" + "                    width: 1px;\n" + "                }\n" + "                *[class].w {\n" + "                    display: block !important;\n" + "                    visibility: visible !important;\n" + "                }\n" + "                *[class].fl {\n" + "                    float: left !important;\n" + "                }\n" + "                *[class].w1 {\n" + "                    width: 1px !important;\n" + "                }\n" + "                *[class].w13 {\n" + "                    width: 13px !important;\n" + "                }\n" + "                *[class].w21 {\n" + "                    width: 21px !important;\n" + "                }\n" + "                *[class].w27 {\n" + "                    width: 27px !important;\n" + "                }\n" + "                *[class].w32 {\n" + "                    width: 32px !important;\n" + "                }\n" + "                *[class].w85 {\n" + "                    width: 85px !important;\n" + "                }\n" + "                *[class].w119 {\n" + "                    width: 119px !important;\n" + "                }\n" + "                *[class].w122 {\n" + "                    width: 122px !important;\n" + "                }\n" + "                *[class].w138 {\n" + "                    width: 138px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w239 {\n" + "                    width: 239px !important;\n" + "                }\n" + "                *[class].w250 {\n" + "                    width: 250px !important;\n" + "                }\n" + "                 *[class].w260 {\n" + "                    width: 260px !important;\n" + "                }\n" + "                *[class].w290 {\n" + "                    width: 290px !important;\n" + "                }\n" + "                *[class].w295 {\n" + "                    width: 295px !important;\n" + "                }\n" + "                *[class].w305 {\n" + "                    width: 305px !important;\n" + "                }\n" + "                *[class].w310 {\n" + "                    width: 310px !important;\n" + "                }\n" + "                *[class].w315 {\n" + "                    width: 315px !important;\n" + "                }\n" + "                *[class].w320 {\n" + "                    width: 320px !important;\n" + "                }\n" + "                *[class].autoh {\n" + "                    height: auto !important;\n" + "                }\n" + "                *[class].h15 {\n" + "                    height: 15px !important;\n" + "                }\n" + "                *[class].h16 {\n" + "                    height: 16px !important;\n" + "                }\n" + "                *[class].h24 {\n" + "                    height: 24px !important;\n" + "                }\n" + "                *[class].h25 {\n" + "                    height: 25px !important;\n" + "                }\n" + "                *[class].h27 {\n" + "                    height: 27px !important;\n" + "                }\n" + "                *[class].h46 {\n" + "                    height: 46px !important;\n" + "                }\n" + "                *[class].h47 {\n" + "                    height: 47px !important;\n" + "                }\n" + "                *[class].h75 {\n" + "                    height: 75px !important;\n" + "                }\n" + "                *[class].h76 {\n" + "                    height: 76px !important;\n" + "                }\n" + "                *[class].h146 {\n" + "                    height: 146px !important;\n" + "                }\n" + "                *[class].h159 {\n" + "                    height: 159px !important;\n" + "                }\n" + "                *[class].h331 {\n" + "                    height: 331px !important;\n" + "                }\n" + "                *[class].w85h27 {\n" + "                    background-size: 85px 27px !important;\n" + "                    height: 27px !important;...";

        boolean checkCSSElements = CSSMatcher.checkCSSElements(cssBuffer.append(css), null, false);

        assertFalse(checkCSSElements);
    }

    @Test
    public void testCheckCSSElements_EmptyStyleMap_ModifiedFalse() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "\n" + "            /* +++++++++++++++++++++ RESET +++++++++++++++++++++ */\n" + "            @namespace \"http://www.w3.org/1999/xhtml\";\n" + "\n" + "\n" + "            @namespace svg \"http://www.w3.org/2000/svg\";\n" + "\n" + "\n" + "            body * {\n" + "                font-size: 1px;\n" + "                line-height: 1px;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            abbr, address, article, aside, audio, b, blockquote, body, canvas, caption, cite, code, dd, del, details, dfn, div, dl, dt, em, embed, fieldset, figcaption, figure, font, footer, form, h1, h2, h3, h4, h5, h6, header, hgroup, html, i, iframe, img, ins, kbd, label, legend, li, mark, menu, nav, object, object, ol, p, pre, q, samp, section, small, span, strong, sub, summary, sup, table, tbody, td, tfoot, th, thead, time, tr, ul, var, video {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                border: 0;\n" + "                /*font-size: 100%; */\n" + "                font: inherit;\n" + "            }\n" + "\n" + "\n" + "            a, img, a img, iframe, form, fieldset, abbr, acronym, object, applet, table {\n" + "                border: 0 none transparent;\n" + "            }\n" + "\n" + "\n" + "            q {\n" + "                quotes: \"\" \"\";\n" + "            }\n" + "\n" + "\n" + "            ul, ol, dir, menu {\n" + "                list-style: none;\n" + "            }\n" + "\n" + "\n" + "            img {\n" + "                display: block;\n" + "                border: 0 none;\n" + "                outline: none;\n" + "                text-decoration: none;\n" + "                -ms-interpolation-mode: bicubic;\n" + "            }\n" + "\n" + "\n" + "            table td {\n" + "                border-collapse: collapse;\n" + "            }\n" + "\n" + "\n" + "            hr {\n" + "                display: block;\n" + "                height: 1px;\n" + "                border: 0;\n" + "                border-top: 1px solid #ccc;\n" + "                margin: 1em 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            table, tbody, td, th, tr, p, a, img {\n" + "                margin: 0;\n" + "                padding: 0;\n" + "            }\n" + "\n" + "\n" + "            strong, b {\n" + "                font-weight: bold;\n" + "            }\n" + "\n" + "\n" + "            /* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */\n" + "            html {\n" + "                direction: ltr;\n" + "                text-align: left;\n" + "                writing-mode: lr-tb;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                /* START: Always force scrollbar in non-IE */\n" + "                overflow-y: scroll;\n" + "            }\n" + "\n" + "\n" + "            body {\n" + "                -webkit-text-size-adjust: none;\n" + "                margin: 0;\n" + "                padding: 0;\n" + "                font-size: 10px;\n" + "                font-size: 1rem;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-arialregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.svg#ff-arialregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "                font-stretch: normal;\n" + "            }\n" + "\n" + "\n" + "            @font-face {\n" + "                font-family: 'ff-helveticaregular';\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot');\n" + "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.svg#ff-helveticaregular') format('svg');\n" + "                font-weight: normal;\n" + "                font-style: normal;\n" + "            }\n" + "\n" + "\n" + "            .ff-arialregular, .ff-arial, .ff-helvetica {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "            }\n" + "\n" + "\n" + "            font {\n" + "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                font-weight: normal;\n" + "                font-size: 12px;\n" + "                font-weight: normal;\n" + "                font-variant: normal;\n" + "            }\n" + "\n" + "\n" + "            a:link, a:visited {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #000066;\n" + "            }\n" + "\n" + "\n" + "            a:hover, a:active {\n" + "                text-decoration: none;\n" + "                font-weight: normal;\n" + "                color: #666666;\n" + "            }\n" + "\n" + "\n" + "            .ReadMsgBody {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            .ExternalClass {\n" + "                width: 100%;\n" + "            }\n" + "\n" + "\n" + "            * .ExternalClass {\n" + "                line-height: 100%;\n" + "            }\n" + "\n" + "\n" + "            *[class].bgcolor_KV {\n" + "                background-color: #411d05 !important;\n" + "            }\n" + "\n" + "\n" + "            @media only screen and (max-width: 580px) {\n" + "                .ff-arialregular {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                }\n" + "                font {\n" + "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" + "                    font-weight: normal;\n" + "                    font-size: 12px;\n" + "                    font-weight: normal;\n" + "                    font-variant: normal;\n" + "                }\n" + "                body {\n" + "                    background-color: #e7e7e7 !important;\n" + "                }\n" + "                \n" + "                *[class].h {\n" + "                    display: none !important;\n" + "                    visibility: none !important;\n" + "                    mso-hide: all !important;\n" + "                    font-size: 1px !important;\n" + "                    line-height: 1px !important;\n" + "                    max-height: 0;\n" + "                    padding: 0;\n" + "                    margin: 0;\n" + "                    height: 1px;\n" + "                    width: 1px;\n" + "                }\n" + "                *[class].w {\n" + "                    display: block !important;\n" + "                    visibility: visible !important;\n" + "                }\n" + "                *[class].fl {\n" + "                    float: left !important;\n" + "                }\n" + "                *[class].w1 {\n" + "                    width: 1px !important;\n" + "                }\n" + "                *[class].w13 {\n" + "                    width: 13px !important;\n" + "                }\n" + "                *[class].w21 {\n" + "                    width: 21px !important;\n" + "                }\n" + "                *[class].w27 {\n" + "                    width: 27px !important;\n" + "                }\n" + "                *[class].w32 {\n" + "                    width: 32px !important;\n" + "                }\n" + "                *[class].w85 {\n" + "                    width: 85px !important;\n" + "                }\n" + "                *[class].w119 {\n" + "                    width: 119px !important;\n" + "                }\n" + "                *[class].w122 {\n" + "                    width: 122px !important;\n" + "                }\n" + "                *[class].w138 {\n" + "                    width: 138px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w188 {\n" + "                    width: 188px !important;\n" + "                }\n" + "                *[class].w239 {\n" + "                    width: 239px !important;\n" + "                }\n" + "                *[class].w250 {\n" + "                    width: 250px !important;\n" + "                }\n" + "                 *[class].w260 {\n" + "                    width: 260px !important;\n" + "                }\n" + "                *[class].w290 {\n" + "                    width: 290px !important;\n" + "                }\n" + "                *[class].w295 {\n" + "                    width: 295px !important;\n" + "                }\n" + "                *[class].w305 {\n" + "                    width: 305px !important;\n" + "                }\n" + "                *[class].w310 {\n" + "                    width: 310px !important;\n" + "                }\n" + "                *[class].w315 {\n" + "                    width: 315px !important;\n" + "                }\n" + "                *[class].w320 {\n" + "                    width: 320px !important;\n" + "                }\n" + "                *[class].autoh {\n" + "                    height: auto !important;\n" + "                }\n" + "                *[class].h15 {\n" + "                    height: 15px !important;\n" + "                }\n" + "                *[class].h16 {\n" + "                    height: 16px !important;\n" + "                }\n" + "                *[class].h24 {\n" + "                    height: 24px !important;\n" + "                }\n" + "                *[class].h25 {\n" + "                    height: 25px !important;\n" + "                }\n" + "                *[class].h27 {\n" + "                    height: 27px !important;\n" + "                }\n" + "                *[class].h46 {\n" + "                    height: 46px !important;\n" + "                }\n" + "                *[class].h47 {\n" + "                    height: 47px !important;\n" + "                }\n" + "                *[class].h75 {\n" + "                    height: 75px !important;\n" + "                }\n" + "                *[class].h76 {\n" + "                    height: 76px !important;\n" + "                }\n" + "                *[class].h146 {\n" + "                    height: 146px !important;\n" + "                }\n" + "                *[class].h159 {\n" + "                    height: 159px !important;\n" + "                }\n" + "                *[class].h331 {\n" + "                    height: 331px !important;\n" + "                }\n" + "                *[class].w85h27 {\n" + "                    background-size: 85px 27px !important;\n" + "                    height: 27px !important;...";

        boolean checkCSSElements = CSSMatcher.checkCSSElements(cssBuffer.append(css), new HashMap<String, Set<String>>(), false);

        assertFalse(checkCSSElements);
    }

    @Test
    public void testCheckCSSElements_StyleMapContainsCSS_ReturnTrue() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = " /* shown_as */   .shown_as.reserved { background-color: #08c; } /* blue */ .shown_as.temporary { background-color: #fc0; } /* yellow */ .shown_as.absent { background-color: #913F3F; } /* red */ .shown_as.free { background-color: #8EB360; } /* green */  .shown_as_label.reserved { color: #08c; } /* blue */ .shown_as_label.temporary { color: #fc0; } /* yellow */ .shown_as_label.absent { color: #913F3F; } /* red */ .shown_as_label.free { color: #8EB360; } /* green */  em {  font-weight: bold; }  /* Detail view */  .timezone {  margin-bottom: 2em; }  .justification, .attachmentNote {  margin-top: 2em;  margin-bottom: 2em; }  .calendar-detail .action {  float: right;  margin-right: 1em; }  .calendar-detail .link {  cursor: pointer;  text-decoration: underline;  color: #00a0cd; }  .calendar-detail .calendar-buttons {  height: 2em;  text-align: right;  line-height: 2em;  border-bottom: 1px solid #f0f0f0; }  .calendar-detail .date { font-size: 11pt; color: #ccc; }  .calendar-detail .interval { color: #555; white-space: nowrap; float: right; }  .calendar-detail .day { color: #888; }  .calendar-detail .title { font-size: 18pt; line-height: 22pt; margin: 0.25em 0 0.25em 0; }  .calendar-detail .location { font-size: 11pt; color: #888; margin-bottom: 1em; }  .calendar-detail .label { font-size: 9pt; color: #888; clear: both; border-bottom: 1px solid #ccc; padding: 1em 0 0.25em 0em; margin-bottom: 0.5em; }  .calendar-detail .note { max-width: 550px; margin: 2em 0 1em 0; -webkit-user-select: text; -moz-user-select: text; user-select: text; cursor: text; }  .calendar-detail .participants { min-height: 2em; }  .calendar-detail .participants table { text-align: left; vertical-align: left; }  .calendar-detail .participant { line-height: 1.2 em; }  .calendar-detail .detail-label { display: inline-block; width: 80px; white-space: nowrap; color: #666; }  .calendar-detail .detail { white-space: nowrap; }  .calendar-detail .detail.shown_as { display: inline-block; height: 1em; width: 1em; }  .calendar-detail .participant .status { font-weight: bold; } .calendar-detail .participant .status.accepted { color: #8EB360; } /* green */ .calendar-detail .participant .status.declined { color: #913F3F; } /* red */ .calendar-detail .participant .status.tentative { color: #c80; } /* orange */  .calendar-detail .participant .comment { color: #888; display: block; white-space: normal; padding-left: 1em; }  .calendar-detail .group { margin: 0.75em 0 0.25em 0; color: #333; }  .person, .person-link {  color: #00A0CD; }  .clear-title {  font-family: OpenSans, Helvetica, Arial, sans-serif;  font-weight: 200;  font-size: 20pt;  line-height: 1.15em; }  .calendar-action {  margin-bottom: 2em;  font-family: OpenSans, Helvetica, Arial, sans-serif;  font-weight: 200;  font-size: 12pt; }  .calendar-action .changes{ margin-top: 2em;  font-size: 11pt; }  .calendar-action .changes .original { font-weight: bold; }  .calendar-action .changes .recurrencePosition { font-weight: bold; }  .calendar-action .changes .updated { color: green; font-weight: bold; }  .calendar-action .status {  } .calendar-action  .status.accepted { color: #8EB360; } /* green */ .calendar-action  .status.declined { color: #913F3F; } /* red */ .calendar-action  .status.tentative { color: #c80; } /* orange */";
        HashSet<String> style = new HashSet<String>();
        style.add("sub");
        style.add("text-top");
        style.add("bottom");
        HashMap<String, Set<String>> styles = new HashMap<String, Set<String>>();
        styles.put("vertical-align", style);

        boolean checkCSSElements = CSSMatcher.checkCSSElements(cssBuffer.append(css), styles, true);

        assertTrue(checkCSSElements);
    }

    private final String bug34659CSS = "<style type=\"text/css\">                                                                                                          \n" +
        "        /* Client-specific Styles */                                                                                                                        \n" +
        "        #outlook a{padding:0;} /* Force Outlook to provide a \"view in browser\" button. */                                                                 \n" +
        "        body{width:100% !important;} .ReadMsgBody{width:100%;} .ExternalClass{width:100%;} /* Force Hotmail to display emails at full width */              \n" +
        "        body{-webkit-text-size-adjust:none; -ms-text-size-adjust:none;} /* Prevent Webkit and Windows Mobile platforms from changing default font sizes. */ \n" +
        "                                                                                                                                                            \n" +
        "        /* Reset Styles */                                                                                                                                  \n" +
        "        body{margin:0; padding:0;}                                                                                                                          \n" +
        "        img{outline:none; text-decoration:none;}                                                                                                            \n" +
        "        #backgroundTable{height:100% !important; margin:0; padding:0; width:100% !important;}                                                               \n" +
        "                                                                                                                                                            \n" +
        "       p {                                                                                                                                                  \n" +
        "           margin: 1em 0;                                                                                                                                   \n" +
        "       }                                                                                                                                                    \n" +
        "                                                                                                                                                            \n" +
        "        .top,                                                                                                                                               \n" +
        "        .footer a:link,                                                                                                                                     \n" +
        "        .footer a:visited,                                                                                                                                  \n" +
        "        .footer a:active { color:#a9a9a9; }                                                                                                                 \n" +
        "                                                                                                                                                            \n" +
        "        .body { color:#494949;}                                                                                                                             \n" +
        "        .body a,                                                                                                                                            \n" +
        "        .body a:active,                                                                                                                                     \n" +
        "        .body a:link,                                                                                                                                       \n" +
        "        .body a:visited{                                                                                                                                    \n" +
        "            color:#f7941e;                                                                                                                                  \n" +
        "        }                                                                                                                                                   \n" +
        "                                                                                                                                                            \n" +
        "                                                                                                                                                            \n" +
        "       h1, h2, h3, h4, h5, h6 {                                                                                                                             \n" +
        "           color: black !important;                                                                                                                         \n" +
        "           line-height: 100% !important;                                                                                                                    \n" +
        "       }                                                                                                                                                    \n" +
        "                                                                                                                                                            \n" +
        "       h1 a, h2 a, h3 a, h4 a, h5 a, h6 a {                                                                                                                 \n" +
        "           color: #ff0000 !important;                                                                                                                       \n" +
        "       }                                                                                                                                                    \n" +
        "                                                                                                                                                            \n" +
        "       h1 a:active, h2 a:active,  h3 a:active, h4 a:active, h5 a:active, h6 a:active {                                                                      \n" +
        "           color: #d60000 !important; /* Preferably not the same color as the normal header link color.  There is limited support for psuedo classes in email clients, this was added just for good measure. */    \n" +
        "       }                                                                                                                                                    \n" +
        "                                                                                                                                                            \n" +
        "       h1 a:visited, h2 a:visited,  h3 a:visited, h4 a:visited, h5 a:visited, h6 a:visited {                                                                \n" +
        "           color: #ff7070 !important; /* Preferably not the same color as the normal header link color. There is limited support for psuedo classes in email clients, this was added just for good measure. */     \n" +
        "       }                                                                                                                                                    \n" +
        "                                                                                                                                                            \n" +
        "       .yshortcuts, .yshortcuts a, .yshortcuts a:link,.yshortcuts a:visited, .yshortcuts a:hover, .yshortcuts a span { color: black; text-decoration: none !important; border-bottom: none !important; background: none !important;} /* Body text color for the New Yahoo.  This example sets the font of Yahoo's Shortcuts to black. */     \n" +
        "                                                                                                                                                            \n" +
        "        .ExternalClass {                                                                                                                                    \n" +
        "            width: 100%;                                                                                                                                    \n" +
        "            line-height: 18px                                                                                                                               \n" +
        "        }                                                                                                                                                   \n" +
        "        .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td {                                                                     \n" +
        "            line-height: 18px                                                                                                                               \n" +
        "        }                                                                                                                                                   \n" +
        "                                                                                                                                                            \n" +
        "    </style>";

    @Test
    public void testDoCheckCss_returnCorrectStartTag() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.doCheckCss(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456", true);
        String convertedCss = cssBld.toString();

        String startTag = "<style type=\"text/#123456 css\">";

        Assert.assertTrue("Processed CSS does not start with the desired parameter " + startTag, convertedCss.startsWith(startTag));
    }

    @Test
    public void testDoCheckCss_returnStyleEndTag() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.doCheckCss(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456", true);
        String convertedCss = cssBld.toString();

        String endTag = "</style>";

        Assert.assertTrue("Processed CSS does not end with the desired parameter " + endTag, convertedCss.endsWith(endTag));
    }

    @Test
    public void testDoCheckCss_includesActiveForHTags() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.doCheckCss(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456", true);
        String convertedCss = cssBld.toString().replaceAll("\\s+", " ");

        String content = "#123456 h1 a:active , #123456 h2 a:active , #123456 h3 a:active , #123456 h4 a:active";

        Assert.assertTrue("Processed CSS does not contain desired content " + content, convertedCss.contains(content));
    }

    @Test
    public void testDoCheckCss_includesCSSParamsWithPrefix() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.doCheckCss(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456", true);
        String convertedCss = cssBld.toString().replaceAll("\\s+", " ");

        String content = "#123456 .123456-top , #123456 .123456-footer a:link , #123456 .123456-footer a:visited , #123456 .123456-footer a:active { color: #a9a9a9; }";

        Assert.assertTrue("Processed CSS does not contain desired content " + content, convertedCss.contains(content));
    }

    @Test
    public void testDoCheckCss_removeBodyTag() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.doCheckCss(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456", true);
        String convertedCss = cssBld.toString().replaceAll("\\s+", " ");

        String content = "#123456  {width: 100%;}".replaceAll("\\s+", " ");

        Assert.assertTrue("Processed CSS does not contain desired content " + content, convertedCss.contains(content));
    }

    @Test
    public void testDoCheckCss_bug34806() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer("font-family:\"MS Mincho\";\n" +
            "    panose-1:2 2 6 9 4 2 5 8 3 4;\n" +
            "    mso-font-alt:\"MS \u660e\u671d\";\n" +
            "    mso-font-charset:128;\n" +
            "    mso-generic-font-family:modern;\n" +
            "    mso-font-pitch:fixed;\n" +
            "    mso-font-signature:-536870145 1791491579 18 0 131231 0;"));

        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.checkCSSElements(cssBld, FilterJerichoHandler.getStaticStyleMap(), true);

        String content = "font-family: \"MS Mincho\";";
        Assert.assertEquals("Processed CSS does not match.", content, cssBld.toString().trim());
    }

    @Test
    public void testDoCheckCss_bug35001() {
        Stringer cssBld = new StringBufferStringer(new StringBuffer("border-collapse: collapse; table-layout: auto; background-image: url(http://images.host.invalid/images/clients/{100974df-02f4-aaef-be13-9eb464b5ab91}_image-3BG.png)"));

        FilterJerichoHandler.loadWhitelist();

        checkCSS(cssBld, FilterJerichoHandler.getImageStyleMap(), true, false);

        String content = "border-collapse: collapse; table-layout: auto;";
        Assert.assertEquals("Processed CSS does not match.", content, cssBld.toString().trim());
    }

    @Test
    public void testDoCheckCss_bug37078() {
        FilterJerichoHandler.loadWhitelist();

        Stringer cssBld = new StringBufferStringer(new StringBuffer("background-position: right -38px;"));
        CSSMatcher.checkCSSElements(cssBld, FilterJerichoHandler.getStaticStyleMap(), true);
        String content = "background-position: right -38px;";
        Assert.assertEquals("Processed CSS does not match.", content, cssBld.toString().trim());

        cssBld = new StringBufferStringer(new StringBuffer("background:radial-gradient(at top, #3C73AA, #052D4B, #052D4B) repeat scroll 0% 0% transparent;"));
        CSSMatcher.checkCSSElements(cssBld, FilterJerichoHandler.getStaticStyleMap(), true);
        content = "background: radial-gradient(at top, #3C73AA, #052D4B, #052D4B) repeat scroll 0% 0% transparent;";
        Assert.assertEquals("Processed CSS does not match.", content, cssBld.toString().trim());
    }

    @Test
    public void testCheckCss_threadpoolAvailableAndNotInternallyInvoked_createAdditionalThread() throws InterruptedException, ExecutionException, TimeoutException {
        PowerMockito.mockStatic(ThreadPools.class);
        ThreadPoolService threadPoolService = Mockito.mock(ThreadPoolService.class);
        PowerMockito.when(ThreadPools.getThreadPool()).thenReturn(threadPoolService);
        Future future = Mockito.mock(Future.class);
        PowerMockito.when(threadPoolService.submit((Task<Boolean>) Matchers.any())).thenReturn(future);
        PowerMockito.when(future.get(Matchers.anyLong(), (TimeUnit) Matchers.any())).thenReturn(Boolean.TRUE);

        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));
        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.checkCSS(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456-prefix", true, false);

        Mockito.verify(threadPoolService, Mockito.times(1)).submit((Task<Boolean>) Matchers.any());
    }

    @Test
    public void testCheckCss_threadpoolAvailableAndInternallyInvoked_createNoThread() throws InterruptedException, ExecutionException, TimeoutException {
        PowerMockito.mockStatic(ThreadPools.class);
        ThreadPoolService threadPoolService = Mockito.mock(ThreadPoolService.class);
        PowerMockito.when(ThreadPools.getThreadPool()).thenReturn(threadPoolService);
        Future future = Mockito.mock(Future.class);
        PowerMockito.when(threadPoolService.submit((Task<Boolean>) Matchers.any())).thenReturn(future);
        PowerMockito.when(future.get(Matchers.anyLong(), (TimeUnit) Matchers.any())).thenReturn(Boolean.TRUE);

        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));
        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.checkCSS(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456-prefix", true, true);

        Mockito.verify(threadPoolService, Mockito.never()).submit((Task<Boolean>) Matchers.any());
    }

    @Test
    public void testCheckCss_threadpoolNotAvailableAndNotInternallyInvoked_createNoThread() throws InterruptedException, ExecutionException, TimeoutException {
        PowerMockito.mockStatic(ThreadPools.class);
        ThreadPoolService threadPoolService = Mockito.mock(ThreadPoolService.class);
        PowerMockito.when(ThreadPools.getThreadPool()).thenReturn(null);
        Future future = Mockito.mock(Future.class);
        PowerMockito.when(threadPoolService.submit((Task<Boolean>) Matchers.any())).thenReturn(future);
        PowerMockito.when(future.get(Matchers.anyLong(), (TimeUnit) Matchers.any())).thenReturn(Boolean.TRUE);

        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));
        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.checkCSS(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456-prefix", true, false);

        Mockito.verify(threadPoolService, Mockito.never()).submit((Task<Boolean>) Matchers.any());
    }

    @Test
    public void testCheckCss_threadpoolNotAvailableAndInternallyInvoked_createNoThread() throws InterruptedException, ExecutionException, TimeoutException {
        PowerMockito.mockStatic(ThreadPools.class);
        ThreadPoolService threadPoolService = Mockito.mock(ThreadPoolService.class);
        PowerMockito.when(ThreadPools.getThreadPool()).thenReturn(null);
        Future future = Mockito.mock(Future.class);
        PowerMockito.when(threadPoolService.submit((Task<Boolean>) Matchers.any())).thenReturn(future);
        PowerMockito.when(future.get(Matchers.anyLong(), (TimeUnit) Matchers.any())).thenReturn(Boolean.TRUE);

        Stringer cssBld = new StringBufferStringer(new StringBuffer(bug34659CSS));
        FilterJerichoHandler.loadWhitelist();

        CSSMatcher.checkCSS(cssBld, FilterJerichoHandler.getStaticStyleMap(), "123456-prefix", true, true);

        Mockito.verify(threadPoolService, Mockito.never()).submit((Task<Boolean>) Matchers.any());
    }
}
