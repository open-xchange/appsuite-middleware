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

package com.openexchange.ajax.mail;

/**
 * {@link TestMails}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TestMails {

    public static final String BUG_19696_MAIL =
        "Date: Mon, 4 Jul 2011 14:22 +0200\n" +
        "From: \"ecoWorkTrack\" <mnuissl@econtec.de>\n" +
        "To: hsehic@econtec.de\n" +
        "Subject: [!!! no reply !!!] => new project ...\n" +
        "X-Mailer: Mail::SendEasy/1.2 Perl/5.008008-linux\n" +
        "Mime-version: 1.0\n" +
        "Content-Type: text/plain; charset=ISO-8859-1\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "Message-Id: <20110704122246.66998FA800C@mail02.econtec.de>\n" +
        "\n" +
        "\n" +
        "\n" +
        "=45=73 =77=75=72=64=65 =65=69=6E =6E=65=75=65=73 =50=72=6F=6A=65=6B=74 =61=\n" +
        "=6E=67=65=6C=65=67=74=3A\n" +
        "\n" +
        "=46=69=72=6D=61      =3A =46=65=72=64=69=6E=61=6E=64 =4D=65=6E=72=61=64 =47=\n" +
        "=6D=62=48 =2B =43=6F=2E =4B=47\n" +
        "\n" +
        "=4E=75=6D=6D=65=72     =3A =41=35=39=38=31\n" +
        "=20=20=20=20\n" +
        "=4E=61=6D=65       =3A =57=69=6C=64=63=61=72=64-=5A=65=72=74=69=66=69=6B=61=\n" +
        "=74=20\n" +
        "\n" +
        "=5A=65=69=74       =3A =32=30=31=31-=30=37-=30=34 - =32=30=31=31-=30=38-=33=\n" +
        "=31\n" +
        "\n" +
        "=50=6C=61=6E=73=74=75=6E=64=65=6E=3A =38\n" +
        "\n" +
        "=54=65=78=74  =3A\n" +
        "=49=6E=73=74=61=6C=6C=61=74=69=6F=6E\n" +
        "=20=20=20=20\n" +
        "=20=20=20=20\n" +
        "\n" +
        "\n";

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

    public static final String replaceAddresses(final String mail, final String address) {
        return mail.replaceAll("#ADDR#", address);
    }

    private TestMails() {
        super();
    }
}
