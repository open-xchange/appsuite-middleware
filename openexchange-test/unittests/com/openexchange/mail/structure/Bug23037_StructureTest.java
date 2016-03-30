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
 * {@link Bug23037_StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug23037_StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug23037_StructureTest}.
     */
    public Bug23037_StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug23037_StructureTest}.
     *
     * @param name The test name
     */
    public Bug23037_StructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMIMEStructure() {
        try {
            getSession();
            final InputStream is = new ByteArrayInputStream(("From: Janusz Kurzawski <jkurzawski@polarisfamily.com>\n" +
                "To: Martin Poglin <mpoglin@polarisfamily.com>\n" +
                "Subject: Przeczytano: Mailuserlsit\n" +
                "Date: Tue, 22 Feb 2011 13:02:24 +0100\n" +
                "Message-ID: <4340252EF61240F6B275681F552B965E@polarisfamily.com>\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: application/ms-tnef;\n" +
                "    name=\"winmail.dat\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=\"winmail.dat\"\n" +
                "X-Mailer: Microsoft Office Outlook 12.0\n" +
                "\n" +
                "eJ8+IhcQAQaQCAAEAAAAAAABAAEAAQeQBgAIAAAA5AQAAAAAAADoAAEIgAcAIAAAAElQTS5NaWNy\n" +
                "b3NvZnQgTWFpbC5SZWFkIFJlY2VpcHQAAwsBCoABACEAAAA0RjM4MzM0MTFDQUFCNzRGODJBQzRG\n" +
                "NDZEMzkzOUQ1RABEBwEDkAYA9AMAAB8AAAALACkAAAAAAEAAMgCw+/0oiNLLAR4ASQABAAAAEQAA\n" +
                "AFJlOiBNYWlsdXNlcmxzaXQAAAAAAgFMAAEAAABFAAAAAAAAAIErH6S+oxAZnW4A3QEPVAIAAAEA\n" +
                "TWFydGluIFBvZ2xpbgBTTVRQAG1wb2dsaW5AcG9sYXJpc2ZhbWlseS5jb20AAAAAQABOAIAdvrSG\n" +
                "0ssBQABVAADG62yH0ssBHgBwAAEAAAANAAAATWFpbHVzZXJsc2l0AAAAAAIBcQABAAAAGwAAAAHL\n" +
                "0ods60YFkxDbUkAqsXme8f2sjvsAAC8ETgAeAHIAAQAAAAEAAAAAAAAAHgBzAAEAAAABAAAAAAAA\n" +
                "AB4AdAABAAAAEQAAAEphbnVzeiBLdXJ6YXdza2kAAAAACwAIDAAAAAALAAEOAQAAAAMAFA4AAAAA\n" +
                "HgAoDgEAAABDAAAAMDAwMDAwMDIBamt1cnphd3NraUBwb2xhcmlzZmFtaWx5LmNvbQFtYWlsc2Vy\n" +
                "dmVyLnBvbGFyaXNmYW1pbHkuY29tAAAeACkOAQAAAEMAAAAwMDAwMDAwMgFqa3VyemF3c2tpQHBv\n" +
                "bGFyaXNmYW1pbHkuY29tAW1haWxzZXJ2ZXIucG9sYXJpc2ZhbWlseS5jb20AAB4AARABAAAAHwAA\n" +
                "AFdpYWRvbW+c5iB6b3N0YbNhIHByemVjenl0YW5hOgAAHgBGEAEAAAAdAAAAamt1cnphd3NraUBw\n" +
                "b2xhcmlzZmFtaWx5LmNvbQAAAAADAN4/n04AAAMAHYBWq/MpTVXQEal8AKDJEfUKAAAAAACgAAAB\n" +
                "AAAAAwAogFOr8ylNVdARqXwAoMkR9QoAAAAAQ6AAAAEAAAAeAE2AhgMCAAAAAADAAAAAAAAARgEA\n" +
                "AAAaAAAAYwBvAG4AdABlAG4AdAAtAHQAeQBwAGUAAAAAAAEAAABfAAAAbXVsdGlwYXJ0L21peGVk\n" +
                "OyBib3VuZGFyeT0iVktVVVhGSVRIT01RSk5ZWFFTRFBBR0RVRUtEQ1pGTlZQSFFOTVpIVU5VIjsg\n" +
                "Y2hhcnNldD0iV2luZG93cy0xMjUyIgAACwBTgAggBgAAAAAAwAAAAAAAAEYAAAAAgoUAAAEAAAAL\n" +
                "AB8OAQAAAAIB+A8BAAAAEAAAAE3Mno+Ux81Jgva+YKrLqQ8CAfoPAQAAABAAAABNzJ6PlMfNSYL2\n" +
                "vmCqy6kPAwD+DwUAAAADAA00/T+lBgMADzT9P6UGAgEUNAEAAAAQAAAATklUQfm/uAEAqgA32W4A\n" +
                "AAIBfwABAAAAMQAAADAwMDAwMDAwMUExMzg5NDAzMUU1RUY0Q0E4RUY0RkZDNzcwQjNCMUY0NDE1\n" +
                "MzgwMAAAAACL+w==\n" +
                "").getBytes());
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
            final MailMessage mail = MimeMessageConverter.convertMessage(mimeMessage);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
