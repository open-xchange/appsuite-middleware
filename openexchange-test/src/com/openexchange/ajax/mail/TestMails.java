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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.mail;

/**
 * {@link TestMails}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TestMails {

    public static final String UMLAUT_MAIL =
        "From: #ADDR#\n" +
        "To: #ADDR#\n" +
        "Subject: Test for bug 15608\n" +
        "Mime-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: 8bit\n" +
        "\n" +
        "Test for bug 15608\n" +
        "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\n";
    
    public static final String DDDTDL_MAIL = 
        "From: #ADDR#\n" + 
        "To: #ADDR#\n" + 
        "Subject: Test for bug 15901\n" + 
        "Mime-Version: 1.0\n" + 
        "Content-Type: text/html; charset=\"UTF-8\"\n" + 
        "Content-Transfer-Encoding: 8bit\n" + 
        "\n" +
        "Some plain text... <br> blah <a href=\"www.xyz.de\">blubb</a>" +
        "<dl>" + 
        "<dt>AA</dt>" + 
        "<dd>Auto Answer (Modem)</dd>" + 
        "<dt>AAE</dt>" + 
        "<dd>Allgemeine Anschalte-Erlaubnis</dd>" + 
        "<dt>AARP</dt>" + 
        "<dd>Appletalk Address Resolution Protocol</dd>" + 
        "</dl>";

    public static final String replaceAddresses(String mail, String address) {
        return mail.replaceAll("#ADDR#", address);
    }

    private TestMails() {
        super();
    }
}
