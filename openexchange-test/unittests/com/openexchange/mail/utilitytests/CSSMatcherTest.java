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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.utilitytests;

import junit.framework.TestCase;
import com.openexchange.html.internal.css.CSSMatcher;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandler;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;


/**
 * {@link CSSMatcherTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CSSMatcherTest extends TestCase {

    /**
     * Initializes a new {@link CSSMatcherTest}.
     */
    public CSSMatcherTest() {
        super();
    }

    /**
     * Initializes a new {@link CSSMatcherTest}.
     * @param name
     */
    public CSSMatcherTest(String name) {
        super(name);
    }

    public void notestCss() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        final String css = " .content {\n" +
            " white-space: normal;\n" +
            " color: black;\n" +
            " font-family: Arial, Helvetica, sans-serif;\n" +
            " font-size: 12px;\n" +
            " cursor: default;\n" +
            "}\n" +
            "/* shown_as */\n" +
            "\n" +
            "\n" +
            ".shown_as.reserved { background-color: #08c; } /* blue */\n" +
            ".shown_as.temporary { background-color: #fc0; } /* yellow */\n" +
            ".shown_as.absent { background-color: #913F3F; } /* red */\n" +
            ".shown_as.free { background-color: #8EB360; } /* green */\n" +
            "\n" +
            ".shown_as_label.reserved { color: #08c; } /* blue */\n" +
            ".shown_as_label.temporary { color: #fc0; } /* yellow */\n" +
            ".shown_as_label.absent { color: #913F3F; } /* red */\n" +
            ".shown_as_label.free { color: #8EB360; } /* green */\n" +
            "\n" +
            "em {\n" +
            " font-weight: bold;\n" +
            "}\n" +
            "\n" +
            "/* Detail view */\n" +
            "\n" +
            ".timezone {\n" +
            " margin-bottom: 2em;\n" +
            "}\n" +
            "\n" +
            ".justification, .attachmentNote {\n" +
            " margin-top: 2em;\n" +
            " margin-bottom: 2em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .action {\n" +
            " float: right;\n" +
            " margin-right: 1em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .link {\n" +
            " cursor: pointer;\n" +
            " text-decoration: underline;\n" +
            " color: #00a0cd;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .calendar-buttons {\n" +
            " height: 2em;\n" +
            " text-align: right;\n" +
            " line-height: 2em;\n" +
            " border-bottom: 1px solid #f0f0f0;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .date {\n" +
            "    font-size: 11pt;\n" +
            "    color: #ccc;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .interval {\n" +
            "    color: #555;\n" +
            "    white-space: nowrap;\n" +
            "    float: right;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .day {\n" +
            "    color: #888;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .title {\n" +
            "    font-size: 18pt;\n" +
            "    line-height: 22pt;\n" +
            "    margin: 0.25em 0 0.25em 0;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .location {\n" +
            "    font-size: 11pt;\n" +
            "    color: #888;\n" +
            "    margin-bottom: 1em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .label {\n" +
            "    font-size: 9pt;\n" +
            "    color: #888;\n" +
            "    clear: both;\n" +
            "    border-bottom: 1px solid #ccc;\n" +
            "    padding: 1em 0 0.25em 0em;\n" +
            "    margin-bottom: 0.5em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .note {\n" +
            "    max-width: 550px;\n" +
            "    margin: 2em 0 1em 0;\n" +
            "    -webkit-user-select: text;\n" +
            "    -moz-user-select: text;\n" +
            "    user-select: text;\n" +
            "    cursor: text;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .participants {\n" +
            "    min-height: 2em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .participants table {\n" +
            "    text-align: left;\n" +
            "    vertical-align: left;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .participant {\n" +
            "    line-height: 1.2 em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .detail-label {\n" +
            "    display: inline-block;\n" +
            "    width: 80px;\n" +
            "    white-space: nowrap;\n" +
            "    color: #666;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .detail {\n" +
            "    white-space: nowrap;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .detail.shown_as {\n" +
            "    display: inline-block;\n" +
            "    height: 1em;\n" +
            "    width: 1em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .participant .status { font-weight: bold; }\n" +
            ".calendar-detail .participant .status.accepted { color: #8EB360; } /* green= */\n" +
            ".calendar-detail .participant .status.declined { color: #913F3F; } /* red */\n" +
            ".calendar-detail .participant .status.tentative { color: #c80; } /* orange = */\n" +
            "\n" +
            ".calendar-detail .participant .comment {\n" +
            "    color: #888;\n" +
            "    display: block;\n" +
            "    white-space: normal;\n" +
            "    padding-left: 1em;\n" +
            "}\n" +
            "\n" +
            ".calendar-detail .group {\n" +
            "    margin: 0.75em 0 0.25em 0;\n" +
            "    color: #333;\n" +
            "}\n" +
            "\n" +
            ".person, .person-link {\n" +
            " color: #00A0CD;\n" +
            "}\n" +
            "\n" +
            ".clear-title {\n" +
            " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" +
            " font-weight: 200;\n" +
            " font-size: 20pt;\n" +
            " line-height: 1.15em;\n" +
            "}\n" +
            "\n" +
            ".calendar-action {\n" +
            " margin-bottom: 2em;\n" +
            " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" +
            " font-weight: 200;\n" +
            " font-size: 12pt;\n" +
            "}\n" +
            "\n" +
            ".calendar-action .changes{\n" +
            "    margin-top: 2em;\n" +
            " font-size: 11pt;\n" +
            "}\n" +
            "\n" +
            ".calendar-action .changes .original {\n" +
            "    font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".calendar-action .changes .recurrencePosition {\n" +
            "    font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".calendar-action .changes .updated {\n" +
            "    color: green;\n" +
            "    font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".calendar-action .status {  }\n" +
            ".calendar-action  .status.accepted { color: #8EB360; } /* green */\n" +
            ".calendar-action  .status.declined { color: #913F3F; } /* red */\n" +
            ".calendar-action  .status.tentative { color: #c80; } /* orange */";

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");

        System.out.println(cssBuffer.toString());
    }

    public void testCss1() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        final String css = " .content {\n" +
            " white-space: normal;\n" +
            " color: black;\n" +
            " font-family: Arial, Helvetica, sans-serif;\n" +
            " font-size: 12px;\n" +
            " cursor: default;\n" +
            "}";

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");

        assertTrue("Unexpected CSS: "+cssBuffer.toString(), cssBuffer.toString().trim().startsWith("#test .test-content {"));
    }

    public void testCss2() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        final String css = ".calendar-action  .status.accepted { color: #8EB360; } /* green */\n";

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");

        assertTrue("Unexpected CSS: "+cssBuffer.toString(), cssBuffer.toString().trim().startsWith("#test .test-calendar-action .test-status.test-accepted {"));
    }

    public void testCss3() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        final String css = ".justification, .attachmentNote {\n" +
            " margin-top: 2em;\n" +
            " margin-bottom: 2em;\n" +
            "}\n";

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");
        final String saneCss = cssBuffer.toString().trim();

        assertTrue("Unexpected CSS: "+saneCss, saneCss.indexOf(',') > 0);

        final String[] splits = saneCss.split(" *, *");
        assertTrue("Unexpected CSS: "+saneCss, splits.length == 2);

        assertTrue("Unexpected CSS: "+saneCss, splits[0].trim().startsWith("#test .test-justification"));
        assertTrue("Unexpected CSS: "+saneCss, splits[1].trim().startsWith("#test .test-attachmentNote {"));
    }

    public void testCss4() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        final String css = CSSSnippets.getCssSnippet1();

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");
        final String saneCss = cssBuffer.toString().trim();

        final String[] lines = saneCss.split("\r?\n");

        {
            final String line = lines[0];
            assertTrue("Unexpected CSS: "+saneCss, line.trim().startsWith("/* common --------------------------------------------------*/  #test {"));
        }

        {
            final String line = lines[1];
            assertTrue("Unexpected CSS: "+saneCss, line.trim().startsWith("#test {"));
        }
    }

    public void testCSSMatcher_CSSwithNamespaceAndBodyWildcard_returnPreparedForMail() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "\n" +
            "            /* +++++++++++++++++++++ RESET +++++++++++++++++++++ */\n" +
            "            @namespace \"http://www.w3.org/1999/xhtml\";\n" +
            "\n" +
            "\n" +
            "            @namespace svg \"http://www.w3.org/2000/svg\";\n" +
            "\n" +
            "\n" +
            "            body * {\n" +
            "                font-size: 1px;\n" +
            "                line-height: 1px;\n" +
            "                margin: 0;\n" +
            "                padding: 0;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            abbr, address, article, aside, audio, b, blockquote, body, canvas, caption, cite, code, dd, del, details, dfn, div, dl, dt, em, embed, fieldset, figcaption, figure, font, footer, form, h1, h2, h3, h4, h5, h6, header, hgroup, html, i, iframe, img, ins, kbd, label, legend, li, mark, menu, nav, object, object, ol, p, pre, q, samp, section, small, span, strong, sub, summary, sup, table, tbody, td, tfoot, th, thead, time, tr, ul, var, video {\n" +
            "                margin: 0;\n" +
            "                padding: 0;\n" +
            "                border: 0;\n" +
            "                /*font-size: 100%; */\n" +
            "                font: inherit;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            a, img, a img, iframe, form, fieldset, abbr, acronym, object, applet, table {\n" +
            "                border: 0 none transparent;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            q {\n" +
            "                quotes: \"\" \"\";\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            ul, ol, dir, menu {\n" +
            "                list-style: none;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            img {\n" +
            "                display: block;\n" +
            "                border: 0 none;\n" +
            "                outline: none;\n" +
            "                text-decoration: none;\n" +
            "                -ms-interpolation-mode: bicubic;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            table td {\n" +
            "                border-collapse: collapse;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            hr {\n" +
            "                display: block;\n" +
            "                height: 1px;\n" +
            "                border: 0;\n" +
            "                border-top: 1px solid #ccc;\n" +
            "                margin: 1em 0;\n" +
            "                padding: 0;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            table, tbody, td, th, tr, p, a, img {\n" +
            "                margin: 0;\n" +
            "                padding: 0;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            strong, b {\n" +
            "                font-weight: bold;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            /* +++++++++++++++++++++ BASICS +++++++++++++++++++++ */\n" +
            "            html {\n" +
            "                direction: ltr;\n" +
            "                text-align: left;\n" +
            "                writing-mode: lr-tb;\n" +
            "                margin: 0;\n" +
            "                padding: 0;\n" +
            "                /* START: Always force scrollbar in non-IE */\n" +
            "                overflow-y: scroll;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            body {\n" +
            "                -webkit-text-size-adjust: none;\n" +
            "                margin: 0;\n" +
            "                padding: 0;\n" +
            "                font-size: 10px;\n" +
            "                font-size: 1rem;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            @font-face {\n" +
            "                font-family: 'ff-arialregular';\n" +
            "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot');\n" +
            "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-ar-std-webfont.svg#ff-arialregular') format('svg');\n" +
            "                font-weight: normal;\n" +
            "                font-style: normal;\n" +
            "                font-stretch: normal;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            @font-face {\n" +
            "                font-family: 'ff-helveticaregular';\n" +
            "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot');\n" +
            "                src: url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.eot?#iefix') format('embedded-opentype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.woff') format('woff'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.ttf') format('truetype'), url('https://ig.cdn.responsys.net/i3/responsysimages/lh/__RS_CP__/ff-hel-std-webfont.svg#ff-helveticaregular') format('svg');\n" +
            "                font-weight: normal;\n" +
            "                font-style: normal;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            .ff-arialregular, .ff-arial, .ff-helvetica {\n" +
            "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            font {\n" +
            "                font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" +
            "                font-weight: normal;\n" +
            "                font-size: 12px;\n" +
            "                font-weight: normal;\n" +
            "                font-variant: normal;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            a:link, a:visited {\n" +
            "                text-decoration: none;\n" +
            "                font-weight: normal;\n" +
            "                color: #000066;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            a:hover, a:active {\n" +
            "                text-decoration: none;\n" +
            "                font-weight: normal;\n" +
            "                color: #666666;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            .ReadMsgBody {\n" +
            "                width: 100%;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            .ExternalClass {\n" +
            "                width: 100%;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            * .ExternalClass {\n" +
            "                line-height: 100%;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            *[class].bgcolor_KV {\n" +
            "                background-color: #411d05 !important;\n" +
            "            }\n" +
            "\n" +
            "\n" +
            "            @media only screen and (max-width: 580px) {\n" +
            "                .ff-arialregular {\n" +
            "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" +
            "                }\n" +
            "                font {\n" +
            "                    font-family: Arial, 'ff-arialregular', Helvetica, sans-serif !important;\n" +
            "                    font-weight: normal;\n" +
            "                    font-size: 12px;\n" +
            "                    font-weight: normal;\n" +
            "                    font-variant: normal;\n" +
            "                }\n" +
            "                body {\n" +
            "                    background-color: #e7e7e7 !important;\n" +
            "                }\n" +
            "                \n" +
            "                *[class].h {\n" +
            "                    display: none !important;\n" +
            "                    visibility: none !important;\n" +
            "                    mso-hide: all !important;\n" +
            "                    font-size: 1px !important;\n" +
            "                    line-height: 1px !important;\n" +
            "                    max-height: 0;\n" +
            "                    padding: 0;\n" +
            "                    margin: 0;\n" +
            "                    height: 1px;\n" +
            "                    width: 1px;\n" +
            "                }\n" +
            "                *[class].w {\n" +
            "                    display: block !important;\n" +
            "                    visibility: visible !important;\n" +
            "                }\n" +
            "                *[class].fl {\n" +
            "                    float: left !important;\n" +
            "                }\n" +
            "                *[class].w1 {\n" +
            "                    width: 1px !important;\n" +
            "                }\n" +
            "                *[class].w13 {\n" +
            "                    width: 13px !important;\n" +
            "                }\n" +
            "                *[class].w21 {\n" +
            "                    width: 21px !important;\n" +
            "                }\n" +
            "                *[class].w27 {\n" +
            "                    width: 27px !important;\n" +
            "                }\n" +
            "                *[class].w32 {\n" +
            "                    width: 32px !important;\n" +
            "                }\n" +
            "                *[class].w85 {\n" +
            "                    width: 85px !important;\n" +
            "                }\n" +
            "                *[class].w119 {\n" +
            "                    width: 119px !important;\n" +
            "                }\n" +
            "                *[class].w122 {\n" +
            "                    width: 122px !important;\n" +
            "                }\n" +
            "                *[class].w138 {\n" +
            "                    width: 138px !important;\n" +
            "                }\n" +
            "                *[class].w188 {\n" +
            "                    width: 188px !important;\n" +
            "                }\n" +
            "                *[class].w188 {\n" +
            "                    width: 188px !important;\n" +
            "                }\n" +
            "                *[class].w239 {\n" +
            "                    width: 239px !important;\n" +
            "                }\n" +
            "                *[class].w250 {\n" +
            "                    width: 250px !important;\n" +
            "                }\n" +
            "                 *[class].w260 {\n" +
            "                    width: 260px !important;\n" +
            "                }\n" +
            "                *[class].w290 {\n" +
            "                    width: 290px !important;\n" +
            "                }\n" +
            "                *[class].w295 {\n" +
            "                    width: 295px !important;\n" +
            "                }\n" +
            "                *[class].w305 {\n" +
            "                    width: 305px !important;\n" +
            "                }\n" +
            "                *[class].w310 {\n" +
            "                    width: 310px !important;\n" +
            "                }\n" +
            "                *[class].w315 {\n" +
            "                    width: 315px !important;\n" +
            "                }\n" +
            "                *[class].w320 {\n" +
            "                    width: 320px !important;\n" +
            "                }\n" +
            "                *[class].autoh {\n" +
            "                    height: auto !important;\n" +
            "                }\n" +
            "                *[class].h15 {\n" +
            "                    height: 15px !important;\n" +
            "                }\n" +
            "                *[class].h16 {\n" +
            "                    height: 16px !important;\n" +
            "                }\n" +
            "                *[class].h24 {\n" +
            "                    height: 24px !important;\n" +
            "                }\n" +
            "                *[class].h25 {\n" +
            "                    height: 25px !important;\n" +
            "                }\n" +
            "                *[class].h27 {\n" +
            "                    height: 27px !important;\n" +
            "                }\n" +
            "                *[class].h46 {\n" +
            "                    height: 46px !important;\n" +
            "                }\n" +
            "                *[class].h47 {\n" +
            "                    height: 47px !important;\n" +
            "                }\n" +
            "                *[class].h75 {\n" +
            "                    height: 75px !important;\n" +
            "                }\n" +
            "                *[class].h76 {\n" +
            "                    height: 76px !important;\n" +
            "                }\n" +
            "                *[class].h146 {\n" +
            "                    height: 146px !important;\n" +
            "                }\n" +
            "                *[class].h159 {\n" +
            "                    height: 159px !important;\n" +
            "                }\n" +
            "                *[class].h331 {\n" +
            "                    height: 331px !important;\n" +
            "                }\n" +
            "                *[class].w85h27 {\n" +
            "                    background-size: 85px 27px !important;\n" +
            "                    height: 27px !important;...";

        CSSMatcher.checkCSS(cssBuffer.append(css), FilterJerichoHandler.getStaticStyleMap(), "test");
        final String saneCss = cssBuffer.toString().trim();

        final String[] lines = saneCss.split("\r?\n");

        final String line0 = lines[0];
        assertTrue(
            "Unexpected CSS: " + saneCss,
            line0.trim().startsWith(
                "/* +++++++++++++++++++++ RESET +++++++++++++++++++++ */ @namespace \"http://www.w3.org/1999/xhtml\";   @namespace svg \"http://www.w3.org/2000/svg\";   body * { #test font-size: 1px; line-height: 1px;"));

        final String line1 = lines[1];
        assertTrue(
            "Unexpected CSS: " + saneCss,
            line1.trim().startsWith("#test a , #test img , #test a img , #test iframe , #test form , #test fieldset"));

        final String line10 = lines[10];
        assertTrue("Unexpected CSS: " + saneCss, line10.trim().startsWith("#test { -webkit-text-size-adjust: none; "));
    }
}
