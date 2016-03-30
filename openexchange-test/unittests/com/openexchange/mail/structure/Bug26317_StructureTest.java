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

package com.openexchange.mail.structure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug26317_StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug26317_StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug26317_StructureTest}.
     */
    public Bug26317_StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug26317_StructureTest}.
     *
     * @param name The test name
     */
    public Bug26317_StructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMIMEStructure() {
        try {
            getSession();
            final InputStream is = new ByteArrayInputStream(("X-Header: Open-Xchange USM Mailer (USM Version: 7.0.2-6, EAS Version: 7.0.2-6, Build f9dd375f96fde35b0cc630a626971a1196347d48)\n" +
                "Content-Type: multipart/mixed; boundary=Apple-Mail-908E0B40-E1C4-4CC2-8245-8674A4C8DB7C\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Subject: Stempeluhr\n" +
                "From: Jane Doe <jane@foo.com>\n" +
                "Message-ID: <2094039877.318527.1367408118992.open-xchange@localhost>\n" +
                "Date: Wed, 1 May 2013 13:35:19 +0200\n" +
                "To: bob@bar.com>\n" +
                "Mime-Version: 1.0\n" +
                "\n" +
                "\n" +
                "\n" +
                "--Apple-Mail-908E0B40-E1C4-4CC2-8245-8674A4C8DB7C\n" +
                "Content-Type: text/plain;\n" +
                "    charset=utf-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "=C3=9Cbersicht der gebuchten Zeiten:\n" +
                "\n" +
                "\n" +
                "\n" +
                "--Apple-Mail-908E0B40-E1C4-4CC2-8245-8674A4C8DB7C\n" +
                "Content-Type: text/comma-separated-values;\n" +
                "    name=\"Stempeluhr__01.05.2013 13:35.csv\"\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=\"Stempeluhr__01.05.2013 13:35.csv\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "//5OAHUAdAB6AGUAcgAsAFMAdABhAHIAdABkAGEAdAB1AG0ALABTAHQAYQByAHQAegBlAGkAdAAs\n" +
                "AEUAbgBkAGQAYQB0AHUAbQAsAEUAbgBkAHoAZQBpAHQALABHAHIAdQBuAGQALABPAHIAdAAsAEsA\n" +
                "dQBuAGQAZQAsAEsAYQB0AGUAZwBvAHIAaQBlACwARABhAHUAZQByACgAbQBpAG4AKQAsAFAAYQB1\n" +
                "AHMAZQAoAG0AaQBuACkALABOAG8AdABpAHoAZQBuACwASwBvAHMAdABlAG4ALABQAHIAZQBpAHMA\n" +
                "CgAiAHUAbgBiAGUAawBhAG4AbgB0ACIALAAiADMAMAAuADAANAAuADEAMwAiACwAIgAwADcAOgAz\n" +
                "ADAAIgAsACIAMwAwAC4AMAA0AC4AMQAzACIALAAiADEANgA6ADEAMAAiACwAIgBBAHIAYgBlAGkA\n" +
                "dABzAHoAZQBpAHQAIgAsACIAIAAiACwAIgBPAHIAdAAgAHoAdQAgAHUAbgBnAGUAbgBhAHUAIgAs\n" +
                "ACIARwBlAHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUAMgAwACwAMAAsACIAIgAsACwACgAiAHUA\n" +
                "bgBiAGUAawBhAG4AbgB0ACIALAAiADIAOQAuADAANAAuADEAMwAiACwAIgAwADcAOgAwADAAIgAs\n" +
                "ACIAMgA5AC4AMAA0AC4AMQAzACIALAAiADEANwA6ADMAMAAiACwAIgBBAHIAYgBlAGkAdABzAHoA\n" +
                "ZQBpAHQAIgAsACIAIAAiACwAIgBPAHIAdAAgAHoAdQAgAHUAbgBnAGUAbgBhAHUAIgAsACIARwBl\n" +
                "AHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADYAMwAwACwAMAAsACIAIgAsACwACgAiAHUAbgBiAGUA\n" +
                "awBhAG4AbgB0ACIALAAiADIANgAuADAANAAuADEAMwAiACwAIgAwADcAOgAzADAAIgAsACIAMgA2\n" +
                "AC4AMAA0AC4AMQAzACIALAAiADEANAA6ADAAMAAiACwAIgBBAHIAYgBlAGkAdABzAHoAZQBpAHQA\n" +
                "IgAsACIANQAxADEANAA5ACAASwD2AGwAbgAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAi\n" +
                "AEcAZQBzAGMAaADkAGYAdABsAGkAYwBoACIALAAzADkAMAAsADAALAAiACIALAAsAAoAIgB1AG4A\n" +
                "YgBlAGsAYQBuAG4AdAAiACwAIgAyADUALgAwADQALgAxADMAIgAsACIAMAA3ADoAMwAwACIALAAi\n" +
                "ADIANQAuADAANAAuADEAMwAiACwAIgAxADcAOgAwADAAIgAsACIAQQByAGIAZQBpAHQAcwB6AGUA\n" +
                "aQB0ACIALAAiADUAMQA0ADIANwAgAEIAZQByAGcAaQBzAGMAaAAgAEcAbABhAGQAYgBhAGMAaAAg\n" +
                "ACIALAAiAEgAbwBtAGUAIgAsACIARwBlAHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUANwAwACwA\n" +
                "MAAsACIAIgAsACwACgAiAHUAbgBiAGUAawBhAG4AbgB0ACIALAAiADIANAAuADAANAAuADEAMwAi\n" +
                "ACwAIgAwADcAOgAzADAAIgAsACIAMgA0AC4AMAA0AC4AMQAzACIALAAiADEANgA6ADQAMAAiACwA\n" +
                "IgBBAHIAYgBlAGkAdABzAHoAZQBpAHQAIgAsACIAIAAiACwAIgBPAHIAdAAgAHoAdQAgAHUAbgBn\n" +
                "AGUAbgBhAHUAIgAsACIARwBlAHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUANQAwACwAMAAsACIA\n" +
                "IgAsACwACgAiAHUAbgBiAGUAawBhAG4AbgB0ACIALAAiADIAMwAuADAANAAuADEAMwAiACwAIgAw\n" +
                "ADcAOgAxADAAIgAsACIAMgAzAC4AMAA0AC4AMQAzACIALAAiADEANwA6ADAAMAAiACwAIgBBAHIA\n" +
                "YgBlAGkAdABzAHoAZQBpAHQAIgAsACIANQAxADQAMgA3ACAAQgBlAHIAZwBpAHMAYwBoACAARwBs\n" +
                "AGEAZABiAGEAYwBoACIALAAiADUAMQA0ADIANwAgAEIAZQByAGcAaQBzAGMAaAAgAEcAbABhAGQA\n" +
                "YgBhAGMAaAAiACwAIgBHAGUAcwBjAGgA5ABmAHQAbABpAGMAaAAiACwANQA5ADAALAAwACwAIgAi\n" +
                "ACwALAAKACIAdQBuAGIAZQBrAGEAbgBuAHQAIgAsACIAMgAyAC4AMAA0AC4AMQAzACIALAAiADAA\n" +
                "NwA6ADIAMAAiACwAIgAyADIALgAwADQALgAxADMAIgAsACIAMQA2ADoAMQAwACIALAAiAEEAcgBi\n" +
                "AGUAaQB0AHMAegBlAGkAdAAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiADUAMQAxADQA\n" +
                "OQAgAEsA9gBsAG4AIgAsACIARwBlAHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUAMwAwACwAMAAs\n" +
                "ACIAIgAsACwACgAiAHUAbgBiAGUAawBhAG4AbgB0ACIALAAiADEAOQAuADAANAAuADEAMwAiACwA\n" +
                "IgAwADcAOgAxADAAIgAsACIAMQA5AC4AMAA0AC4AMQAzACIALAAiADEANAA6ADAAMAAiACwAIgBB\n" +
                "AHIAYgBlAGkAdABzAHoAZQBpAHQAIgAsACIANQAxADEANAA5ACAASwD2AGwAbgAiACwAIgA1ADEA\n" +
                "MQA0ADkAIABLAPYAbABuACIALAAiAEcAZQBzAGMAaADkAGYAdABsAGkAYwBoACIALAA0ADEAMAAs\n" +
                "ADAALAAiACIALAAsAAoAIgB1AG4AYgBlAGsAYQBuAG4AdAAiACwAIgAxADgALgAwADQALgAxADMA\n" +
                "IgAsACIAMAA3ADoAMAAwACIALAAiADEAOAAuADAANAAuADEAMwAiACwAIgAxADgAOgAxADAAIgAs\n" +
                "ACIAUwBwAGUAcwBlAG4AYQBiAHIAZQBjAGgAbgB1AG4AZwAiACwAIgAgACIALAAiAE8AcgB0ACAA\n" +
                "egB1ACAAdQBuAGcAZQBuAGEAdQAiACwAIgBHAGUAcwBjAGgA5ABmAHQAbABpAGMAaAAiACwANgA3\n" +
                "ADAALAAwACwAIgAiACwALAAKACIAdQBuAGIAZQBrAGEAbgBuAHQAIgAsACIAMQA3AC4AMAA0AC4A\n" +
                "MQAzACIALAAiADAANwA6ADMAMAAiACwAIgAxADcALgAwADQALgAxADMAIgAsACIAMQA4ADoAMAAw\n" +
                "ACIALAAiAFMAcABlAHMAZQBuAGEAYgByAGUAYwBoAG4AdQBuAGcAIgAsACIANQAxADEANAA5ACAA\n" +
                "SwD2AGwAbgAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiAEcAZQBzAGMAaADkAGYAdABs\n" +
                "AGkAYwBoACIALAA2ADMAMAAsADAALAAiACIALAAsAAoAIgB1AG4AYgBlAGsAYQBuAG4AdAAiACwA\n" +
                "IgAxADYALgAwADQALgAxADMAIgAsACIAMAA3ADoAMwAwACIALAAiADEANgAuADAANAAuADEAMwAi\n" +
                "ACwAIgAxADYAOgAyADAAIgAsACIAUwBwAGUAcwBlAG4AYQBiAHIAZQBjAGgAbgB1AG4AZwAiACwA\n" +
                "IgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiADUAMQAxADQAOQAgAEsA9gBsAG4AIgAsACIARwBl\n" +
                "AHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUAMwAwACwAMAAsACIAIgAsACwACgAiAHUAbgBiAGUA\n" +
                "awBhAG4AbgB0ACIALAAiADEANQAuADAANAAuADEAMwAiACwAIgAwADcAOgAxADAAIgAsACIAMQA1\n" +
                "AC4AMAA0AC4AMQAzACIALAAiADEANgA6ADMAMAAiACwAIgBTAHAAZQBzAGUAbgBhAGIAcgBlAGMA\n" +
                "aABuAHUAbgBnACIALAAiACAAIgAsACIATwByAHQAIAB6AHUAIAB1AG4AZwBlAG4AYQB1ACIALAAi\n" +
                "AEcAZQBzAGMAaADkAGYAdABsAGkAYwBoACIALAA1ADYAMAAsADAALAAiACIALAAsAAoAIgB1AG4A\n" +
                "YgBlAGsAYQBuAG4AdAAiACwAIgAxADIALgAwADQALgAxADMAIgAsACIAMAA3ADoAMwAwACIALAAi\n" +
                "ADEAMgAuADAANAAuADEAMwAiACwAIgAxADYAOgAwADAAIgAsACIAUwBwAGUAcwBlAG4AYQBiAHIA\n" +
                "ZQBjAGgAbgB1AG4AZwAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiADUAMQAxADQAOQAg\n" +
                "AEsA9gBsAG4AIgAsACIARwBlAHMAYwBoAOQAZgB0AGwAaQBjAGgAIgAsADUAMQAwACwAMAAsACIA\n" +
                "IgAsACwACgAiAHUAbgBiAGUAawBhAG4AbgB0ACIALAAiADEAMQAuADAANAAuADEAMwAiACwAIgAw\n" +
                "ADcAOgAxADkAIgAsACIAMQAxAC4AMAA0AC4AMQAzACIALAAiADEANgA6ADUAMAAiACwAIgBTAHAA\n" +
                "ZQBzAGUAbgBhAGIAcgBlAGMAaABuAHUAbgBnACIALAAiACAAIgAsACIATwByAHQAIAB6AHUAIAB1\n" +
                "AG4AZwBlAG4AYQB1ACIALAAiAEcAZQBzAGMAaADkAGYAdABsAGkAYwBoACIALAA1ADcAMQAsADAA\n" +
                "LAAiACIALAAsAAoAIgB1AG4AYgBlAGsAYQBuAG4AdAAiACwAIgAxADAALgAwADQALgAxADMAIgAs\n" +
                "ACIAMAA2ADoAMAAwACIALAAiADEAMAAuADAANAAuADEAMwAiACwAIgAxADgAOgA0ADAAIgAsACIA\n" +
                "UwBwAGUAcwBlAG4AYQBiAHIAZQBjAGgAbgB1AG4AZwAiACwAIgAgACIALAAiAE8AcgB0ACAAegB1\n" +
                "ACAAdQBuAGcAZQBuAGEAdQAiACwAIgBHAGUAcwBjAGgA5ABmAHQAbABpAGMAaAAiACwANwA2ADAA\n" +
                "LAAwACwAIgAiACwALAAKACIAdQBuAGIAZQBrAGEAbgBuAHQAIgAsACIAMAA5AC4AMAA0AC4AMQAz\n" +
                "ACIALAAiADAANwA6ADIAMAAiACwAIgAwADkALgAwADQALgAxADMAIgAsACIAMQA2ADoAMgAwACIA\n" +
                "LAAiAFMAcABlAHMAZQBuAGEAYgByAGUAYwBoAG4AdQBuAGcAIgAsACIANQAxADEANAA5ACAASwD2\n" +
                "AGwAbgAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiAEcAZQBzAGMAaADkAGYAdABsAGkA\n" +
                "YwBoACIALAA1ADQAMAAsADAALAAiACIALAAsAAoAIgB1AG4AYgBlAGsAYQBuAG4AdAAiACwAIgAw\n" +
                "ADgALgAwADQALgAxADMAIgAsACIAMAA3ADoAMwAwACIALAAiADAAOAAuADAANAAuADEAMwAiACwA\n" +
                "IgAxADYAOgA0ADAAIgAsACIAUwBwAGUAcwBlAG4AYQBiAHIAZQBjAGgAbgB1AG4AZwAiACwAIgA1\n" +
                "ADEAMQA0ADkAIABLAPYAbABuACIALAAiADUAMQAxADQAOQAgAEsA9gBsAG4AIgAsACIARwBlAHMA\n" +
                "YwBoAOQAZgB0AGwAaQBjAGgAIgAsADUANQAwACwAMAAsACIAIgAsACwACgAiAHUAbgBiAGUAawBh\n" +
                "AG4AbgB0ACIALAAiADAANwAuADAANAAuADEAMwAiACwAIgAxADEAOgAwADAAIgAsACIAMAA3AC4A\n" +
                "MAA0AC4AMQAzACIALAAiADEAMgA6ADMAMAAiACwAIgBTAHAAZQBzAGUAbgBhAGIAcgBlAGMAaABu\n" +
                "AHUAbgBnACIALAAiACAAIgAsACIAKABuAHUAbABsACkAIAAoAG4AdQBsAGwAKQAiACwAIgBHAGUA\n" +
                "cwBjAGgA5ABmAHQAbABpAGMAaAAiACwAOQAwACwAMAAsACIAIgAsACwACgAiAHUAbgBiAGUAawBh\n" +
                "AG4AbgB0ACIALAAiADAANQAuADAANAAuADEAMwAiACwAIgAwADcAOgAxADAAIgAsACIAMAA1AC4A\n" +
                "MAA0AC4AMQAzACIALAAiADEANQA6ADEAMAAiACwAIgBBAHIAYgBlAGkAdABzAHoAZQBpAHQAIgAs\n" +
                "ACIAIAAiACwAIgAoAG4AdQBsAGwAKQAgACgAbgB1AGwAbAApACIALAAiAEcAZQBzAGMAaADkAGYA\n" +
                "dABsAGkAYwBoACIALAA0ADgAMAAsADAALAAiACIALAAsAAoAIgB1AG4AYgBlAGsAYQBuAG4AdAAi\n" +
                "ACwAIgAwADQALgAwADQALgAxADMAIgAsACIAMAA3ADoAMgAwACIALAAiADAANAAuADAANAAuADEA\n" +
                "MwAiACwAIgAxADYAOgAwADAAIgAsACIAQQByAGIAZQBpAHQAcwB6AGUAaQB0ACIALAAiADUAMQAx\n" +
                "ADQAOQAgAEsA9gBsAG4AIgAsACIANQAxADEANAA5ACAASwD2AGwAbgAiACwAIgBHAGUAcwBjAGgA\n" +
                "5ABmAHQAbABpAGMAaAAiACwANQAyADAALAAwACwAIgAiACwALAAKACIAdQBuAGIAZQBrAGEAbgBu\n" +
                "AHQAIgAsACIAMAAzAC4AMAA0AC4AMQAzACIALAAiADAANwA6ADMAMAAiACwAIgAwADMALgAwADQA\n" +
                "LgAxADMAIgAsACIAMQA3ADoAMAAwACIALAAiAEEAcgBiAGUAaQB0AHMAegBlAGkAdAAiACwAIgA1\n" +
                "ADEAMQA0ADkAIABLAPYAbABuACIALAAiADUAMQAxADQAOQAgAEsA9gBsAG4AIgAsACIARwBlAHMA\n" +
                "YwBoAOQAZgB0AGwAaQBjAGgAIgAsADUANwAwACwAMAAsACIAIgAsACwACgAiAHUAbgBiAGUAawBh\n" +
                "AG4AbgB0ACIALAAiADAAMgAuADAANAAuADEAMwAiACwAIgAwADcAOgAzADAAIgAsACIAMAAyAC4A\n" +
                "MAA0AC4AMQAzACIALAAiADEANgA6ADEAMAAiACwAIgBBAHIAYgBlAGkAdABzAHoAZQBpAHQAIgAs\n" +
                "ACIANQAxADEANAA5ACAASwD2AGwAbgAiACwAIgA1ADEAMQA0ADkAIABLAPYAbABuACIALAAiAEcA\n" +
                "ZQBzAGMAaADkAGYAdABsAGkAYwBoACIALAA1ADIAMAAsADAALAAiACIALAAsAAoA\n" +
                "\n" +
                "--Apple-Mail-908E0B40-E1C4-4CC2-8245-8674A4C8DB7C\n" +
                "Content-Type: text/plain;\n" +
                "    charset=utf-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "Mit freundlichen Gr=C3=BC=C3=9Fen\n" +
                "\n" +
                "Jane Doe\n" +
                "--Apple-Mail-908E0B40-E1C4-4CC2-8245-8674A4C8DB7C--").getBytes());
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
            final MailMessage mail = MimeMessageConverter.convertMessage(mimeMessage);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            final JSONObject jsonPartObject = jsonMailObject.getJSONArray("body").getJSONObject(1);
            final JSONObject jsonHeaderObject = jsonPartObject.getJSONObject("headers").getJSONObject("content-type");

            assertEquals("Unexpected Content-Type for Excel CSV sheet.", "application/vnd.ms-excel", jsonHeaderObject.getString("type"));

            final String sData = jsonPartObject.getJSONObject("body").getString("data");
            assertTrue("Unexpected base64 data:\n" + sData, sData != null && sData.endsWith("AAsAAoA"));

            // System.out.println(jsonPartObject.toString(2));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
