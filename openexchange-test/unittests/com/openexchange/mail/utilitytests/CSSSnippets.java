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

package com.openexchange.mail.utilitytests;


/**
 * {@link CSSSnippets}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSSSnippets {

    /**
     * Initializes a new {@link CSSSnippets}.
     */
    private CSSSnippets() {
        super();
    }

    public static String getCssSnippet1() {
        // From http://www.email-standards.org/acid/
        return "    /* common\n" + 
        		"    --------------------------------------------------*/\n" + 
        		"    \n" + 
        		"    body {\n" + 
        		"        margin: 0px;\n" + 
        		"        padding: 0px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #930;\n" + 
        		"    }\n" + 
        		"    #BodyImposter {\n" + 
        		"        color: #fff;\n" + 
        		"        background: #930 url(\"img/bgBody.gif\") repeat-x;\n" + 
        		"        background-color: #930;\n" + 
        		"        font-family: Arial, Helvetica, sans-serif;\n" + 
        		"        width: 100%;\n" + 
        		"        margin: 0px;\n" + 
        		"        padding: 0px;\n" + 
        		"        text-align: center;\n" + 
        		"    }\n" + 
        		"    #BodyImposter dt {\n" + 
        		"        font-size: 14px;\n" + 
        		"        line-height: 1.5em;\n" + 
        		"        font-weight: bold;\n" + 
        		"    }\n" + 
        		"    #BodyImposter dd,\n" + 
        		"    #BodyImposter li,\n" + 
        		"    #BodyImposter p,\n" + 
        		"    #WidthHeight span {\n" + 
        		"        font-size: 12px;\n" + 
        		"        line-height: 1.5em;\n" + 
        		"    }\n" + 
        		"    #BodyImposter dd,\n" + 
        		"    #BodyImposter dt {\n" + 
        		"        margin: 0px;\n" + 
        		"        padding: 0px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter dl,\n" + 
        		"    #BodyImposter ol,\n" + 
        		"    #BodyImposter p,\n" + 
        		"    #BodyImposter ul {\n" + 
        		"        margin: 0px 0px 4px 0px;\n" + 
        		"        padding: 10px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #ad5c33;\n" + 
        		"    }\n" + 
        		"    #BodyImposter small {\n" + 
        		"        font-size: 11px;\n" + 
        		"        font-style: italic;\n" + 
        		"    }\n" + 
        		"    #BodyImposter ol li {\n" + 
        		"        margin: 0px 0px 0px 20px;\n" + 
        		"        padding: 0px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter ul#BulletBg li {\n" + 
        		"        background: url(\"img/bullet.gif\") no-repeat 0em 0.2em;\n" + 
        		"        padding: 0px 0px 0px 20px;\n" + 
        		"        margin: 0px;\n" + 
        		"        list-style: none;\n" + 
        		"    }\n" + 
        		"    #BodyImposter ul#BulletListStyle li {\n" + 
        		"        margin: 0px 0px 0px 22px;\n" + 
        		"        padding: 0px;\n" + 
        		"        list-style: url(\"img/bullet.gif\");\n" + 
        		"    }\n" + 
        		"    \n" + 
        		"    /* links\n" + 
        		"    --------------------------------------------------*/\n" + 
        		"    \n" + 
        		"    #BodyImposter a {\n" + 
        		"        text-decoration: underline;\n" + 
        		"    }\n" + 
        		"    #BodyImposter a:link,\n" + 
        		"    #BodyImposter a:visited {\n" + 
        		"        color: #dfb8a4;\n" + 
        		"        background: #ad5c33;\n" + 
        		"    }\n" + 
        		"    #ButtonBorders a:link,\n" + 
        		"    #ButtonBorders a:visited {\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00;\n" + 
        		"    }\n" + 
        		"    #BodyImposter a:hover {\n" + 
        		"        text-decoration: none;\n" + 
        		"    }\n" + 
        		"    #BodyImposter a:active {\n" + 
        		"        color: #000;\n" + 
        		"        background: #ad5c33;\n" + 
        		"        text-decoration: none;\n" + 
        		"    }\n" + 
        		"    \n" + 
        		"    /* heads\n" + 
        		"    --------------------------------------------------*/\n" + 
        		"    \n" + 
        		"    #BodyImposter h1,\n" + 
        		"    #BodyImposter h2,\n" + 
        		"    #BodyImposter h3 {\n" + 
        		"        color: #fff;\n" + 
        		"        background: #ad5c33;\n" + 
        		"        font-weight: bold;\n" + 
        		"        line-height: 1em;\n" + 
        		"        margin: 0px 0px 4px 0px;\n" + 
        		"        padding: 10px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter h1 {\n" + 
        		"        font-size: 34px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter h2 {\n" + 
        		"        font-size: 22px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter h3 {\n" + 
        		"        font-size: 16px;\n" + 
        		"    }\n" + 
        		"    #BodyImposter h1:hover,\n" + 
        		"    #BodyImposter h2:hover,\n" + 
        		"    #BodyImposter h3:hover,\n" + 
        		"    #BodyImposter dl:hover,\n" + 
        		"    #BodyImposter ol:hover,\n" + 
        		"    #BodyImposter p:hover,\n" + 
        		"    #BodyImposter ul:hover {\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00;\n" + 
        		"    }\n" + 
        		"    \n" + 
        		"    /* boxes\n" + 
        		"    --------------------------------------------------*/\n" + 
        		"    \n" + 
        		"    #Box {\n" + 
        		"        width: 470px;\n" + 
        		"        margin: 0px auto;\n" + 
        		"        padding: 40px 20px;\n" + 
        		"        text-align: left;\n" + 
        		"    }\n" + 
        		"    p#ButtonBorders {\n" + 
        		"        clear: both;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00;\n" + 
        		"        border-top: 10px solid #ad5c33;\n" + 
        		"        border-right: 1px dotted #ad5c33;\n" + 
        		"        border-bottom: 1px dashed #ad5c33;\n" + 
        		"        border-left: 1px dotted #ad5c33;\n" + 
        		"    }\n" + 
        		"    p#ButtonBorders a#Arrow {\n" + 
        		"        padding-right: 20px;\n" + 
        		"        background: url(\"img/arrow.gif\") no-repeat right 2px;\n" + 
        		"    }\n" + 
        		"    p#ButtonBorders a {\n" + 
        		"        color: #fff;\n" + 
        		"        background-color: #892e00;\n" + 
        		"    }\n" + 
        		"    p#ButtonBorders a#Arrow:hover {\n" + 
        		"        background-position: right -38px;\n" + 
        		"    }\n" + 
        		"    #Floater {\n" + 
        		"        width: 470px;\n" + 
        		"    }\n" + 
        		"    #Floater #Left {\n" + 
        		"        float: left;\n" + 
        		"        width: 279px;\n" + 
        		"        height: 280px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00;\n" + 
        		"        margin-bottom: 4px;\n" + 
        		"    }\n" + 
        		"    #Floater #Right {\n" + 
        		"        float: right;\n" + 
        		"        width: 187px;\n" + 
        		"        height: 280px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00 url(\"img/ornament.gif\") no-repeat right bottom;\n" + 
        		"        margin-bottom: 4px;\n" + 
        		"    }\n" + 
        		"    #Floater #Right p {\n" + 
        		"        color: #fff;\n" + 
        		"        background: transparent;\n" + 
        		"    }\n" + 
        		"    #FontInheritance {\n" + 
        		"        font-family: Georgia, Times, serif;\n" + 
        		"    }\n" + 
        		"    #MarginPaddingOut {\n" + 
        		"        padding: 20px;\n" + 
        		"    }\n" + 
        		"    #MarginPaddingOut #MarginPaddingIn {\n" + 
        		"        padding: 15px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #ad5c33;\n" + 
        		"    }\n" + 
        		"    #MarginPaddingOut #MarginPaddingIn img {\n" + 
        		"        background: url(\"img/bgPhoto.gif\") no-repeat;\n" + 
        		"        padding: 15px;\n" + 
        		"    }\n" + 
        		"    span#SerifFont {\n" + 
        		"        font-family: Georgia, Times, serif;\n" + 
        		"    }\n" + 
        		"    p#QuotedFontFamily {\n" + 
        		"        font-family: \"Trebuchet MS\", serif;\n" + 
        		"    }\n" + 
        		"    #WidthHeight {\n" + 
        		"        width: 470px;\n" + 
        		"        height: 200px;\n" + 
        		"        color: #fff;\n" + 
        		"        background: #892e00;\n" + 
        		"    }\n" + 
        		"    #WidthHeight span {\n" + 
        		"        display: block;\n" + 
        		"        padding: 10px;\n" + 
        		"    }\n";
    }

}
