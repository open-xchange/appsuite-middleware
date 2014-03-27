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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;
import org.json.JSONObject;
import com.openexchange.java.Streams;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug30848_StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug30848_StructureTest extends TestCase {

    /**
     * Initializes a new {@link Bug30848_StructureTest}.
     */
    public Bug30848_StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug30848_StructureTest}.
     *
     * @param name The test name
     */
    public Bug30848_StructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMIMEStructure() {
        try {
            //getSession();

            String[] lines = {"Return-path: <helen@foo.com>\n",
                "Delivery-date: Mon, 03 Feb 2014 16:03:01 +0100\n",
                "From: \"Helen Delhey\" <helen@foo.com>\n",
                "To: \"'Markus Delhey'\" <markus@foo.com>\n",
                "Subject: Neuer Test\n",
                "Date: Mon, 3 Feb 2014 16:02:57 +0100\n",
                "Message-ID: <000d01cf85748f1$071a3940$154eabc0$@de>\n",
                "MIME-Version: 1.0\n",
                "Content-Type: application/x-pkcs7-mime;\n",
                "    smime-type=enveloped-data;\n",
                "    name=\"smime.p7m\"\n",
                "Content-Transfer-Encoding: base64\n",
                "Content-Disposition: attachment;\n",
                "    filename=\"smime.p7m\"\n",
                "X-Mailer: Microsoft Office Outlook 12.0\n",
                "thread-index: Ac8g8QXp+qOJkKSbQKqLEilci6G1KQ==\n",
                "Content-Language: de\n",
                "\n",
                "MIAGCSqGSIb3DQEHA6CAMIACAQAxggLQMIIBYwIBADBLMDcxHjAcBgkqhkiG9w0BCQEWD2hlbGVu\n",
                "QGRlbGhleS5kZTEVMBMGA1UEAwwMSGVsZW4gRGVsaGV5AhApk4pwy8I1mkF7FJj0nhvpMA0GCSqG\n",
                "SIb3DQEBAQUABIIBABQnc2Bo0omfyL1YqIA4KOOuT0uSyjAXhEoBJycE0fJ8PQLpA6R2CA0yhp2A\n",
                "yvyhcdaaPSOhwr0bjHfz+omeMycQGRltJdJ4QzgqUsC+0Wegcvrg/mXz35b44x3ZImlaE/3MwYtp\n",
                "8keumHhjSwVe8BI+PYcXF1C4YUAh/ykHePZVSzIfVrCQBDVk9zOnUY/u4mrG8MxAqnsXvpIjviTe\n",
                "sx/gAQmsAGKDUciQmoWEK6ZzG4idlHLm4EXPUz1BJK7RUTmFwiDcgDRhzY4HTFtj/rNgVaH0s2wK\n",
                "+CtJG+w7MJeiRAF+78rxkif7iC1Vw1sWWr8b1gvm4Eqh/U4wBSE/b9MwggFlAgEAME0wOTEfMB0G\n",
                "CSqGSIb3DQEJARYQbWFya3VzQGRlbGhleS5kZTEWMBQGA1UEAwwNTWFya3VzIERlbGhleQIQRyC4\n",
                "0v7FI59Go01lKTx7zjANBgkqhkiG9w0BAQEFAASCAQCEGhjT7AwxlabZMhQN57g7lr/VvCDtRpwo\n",
                "wWSE1tBkrNXs5tIJY+MjU3XZEPuUvazqo2dTkZVnoB1c1oiXZu7OOC6Txmm+ChBxG50SXkAdi1VD\n",
                "DmmulaFMJuGnOm1C9NZ2icU3aVBzWyQ82UwzTNj1pacBXZ3H9lBuD019O6vusiuxUyEkTNF1+nUJ\n",
                "89RWuVu+CHZLIVNjp6yBYlcUlZQJA28zxr5IFSoLRm3rbgN1Jjven33FFXy1ahvLqbvNbWZ0BT1d\n",
                "7Ln2+WzrwQr6iKS/zAVggxUk4ZtnXgRU1sAOcLiacjnN1gwaoXb7mIM3cx2aNU0jP/WVC6CKlWrc\n",
                "+bwPMIAGCSqGSIb3DQEHATAdBglghkgBZQMEASoEEJpJh5DcjeQVamo5BbrW3eSggASCBACSSzA4\n",
                "/O+cXgE5khw5IjdA5GroJ5HoHpvhRlRi8wOYWe6mw5DnuQLxC5ylWUyt87/4I66SZOw7nXl/etYl\n",
                "an7N3zDPOTJlT9nczTbmePdg9Igi6EomdEWbvfoQeD1qMIt0M/3N2itsb81XoMbcloAsJgHM9iw7\n",
                "/E2xBjDe/K7xVautNWhKXFlclO801Dg8ii5jZfxMTWvqIZEbC691ZVmsWo/ovcYUekax5Db74wJ0\n",
                "lV8HejnBXs9lg3FML5IHhkbImXZg672ShIYcb5hBZVvbFnOm9r890E788H2CIc9j233K2ESSJIrc\n",
                "SeZbffbtf3GlSGvMjkkvKxKF5GBhF3gz1Tz1LpT6JeRNpvowa6s6JN9x5TF39a/+pw/F4vYqE5o1\n",
                "5vxQKEUND2e5jgOaBSz4MnOAf5IyzN7gQ9xL1ByoEjLFig7uYSJqQAvIqXtnHMWSjwZrXI2fT9Dz\n",
                "igZ2aWTKAC+HvGwyxV1j1VEcjMZYID4Iul/nwEY+SDI015QLdZ6UrWK+r8v5WPjD0QV25wklCSqw\n",
                "2niPHRgn5jopjmUNnJdA6pSer4WbKT/GY2NjXahVonTSoIhvkuY89ymccBHPQsujwfKjusL7vCBH\n",
                "3VodPpn1gG9XzclV3E6y8NBM94hcicC7KUzF9gSjgV8oJnC1WG7Yg4L7v+QUtNdkqz2b7gSX6mJk\n",
                "nCanKqLtcGqOWesNokD8FAp+pLIs1OuByAMlHOc7La4ZwO5Rsl6sbaYUu2df7o5UNm9Aa2emAGK/\n",
                "F5a8SDgtY0q+WVnXpEro57Vak9GAi4xRGmceckp48aBafDE7o1BLNer8pNq/n5hX/UBNskhnALou\n",
                "vj6mAuudMIf+ucrdm/P1JzugCh2sXhE8VG0/LvX3+A9c7H30Gsf8ZCf4ENAg1na5kK34SYFjOh28\n",
                "+OJFMiQ/Qd6YdwGo8DmNQMKzOXfRSuAms42bbfDYfGU16GOPt4MHBjGA0h32lxCkKms0OyAe/gi5\n",
                "W06NoCkM893rxDJo9c3TEsteempyRYXSoAjc44zuUyjoZoIRg3+U2zznXmLnI4f1bqBpbhhD7vT7\n",
                "Vppd+VJ5iqvtYIEFpQJo0y7ArxaRoOb4IhJnlJofIecLzujyASqqhd2NrsCJTflQ5Zqa8mOBJttY\n",
                "3r4UZND5JCajJyMU7BHc880+qPjv6ZeiIWKvfcYQHuALu2DmJw/V7opcgcpSwd9v/AeDbrg4YJlz\n",
                "sfLvsoA24t0OE2mG3VfGPk0TM0TW+HZ02Jt/3uzk0RcLlSlEwEpYO9zavSGwxbSO8P+ruyBlcX5Q\n",
                "tUlYmIWvPLqdsh/qi76ZZglSm5n05zrsXbY97S1wTgrpXbadB35yiLmECL7tJSn5vApUBIIEAEht\n",
                "+w4bssfYaZ7gG5lsR3xqVhAgm9drqd5BkWbRXj0zHqzuXUyOe/4iNRQvbesNTNoE6hrbF937insP\n",
                "FRtbZSmr5fstp08S4+KvROn1HCjvcnatQyY7iWa4qGbkoCbOpZZwjIoHaCFl4e7MTy9BA00z8Ovo\n",
                "GUULatHK7YGMt7Q3k5XUIWXG5q5nk1FB8Vg8Hgxx1XgbG+Y9m7WyYOXUsHs9iU7vd1uirY2sJegd\n",
                "yf0mQIzyAOHLzCLsDs2nQbwdrU0pEkyOAWFSmzpvgknGlOrHHnODFOM+EEbro62Cf2t3meIuuNEw\n",
                "6Q2T6TngYYQMLeLMX+CttaA3msLMGA+Ufu7tGseH55hA94EAGz1XaAiIOnz7EkwqfNaFoSKP7v8M\n",
                "aSsqHXJG52zLwEKrm6tD6pd7Qpu98K3CIRa9Hj6YtwtoTKeXOJkHLm0CFslFtPkFtzP3mNV6hK5b\n",
                "2i1err4OVM6vnX+FIIknXj9uVLFG6/4o55bgTUBIp0TNERhQo2BASFuCMayxdmqhVfgEyEge2unQ\n",
                "QknLZAqSrOPgTLxv+shxxaxdXBYQbcPMLw6zxDZu58edHBnwwCGGUajRKZGDh+WZBN59oOjbnSfk\n",
                "5JB9RWSdXr1hcf/osZ3ueUGMEHCLV+dsbOCDUxYWATmLwIa4J1WDXsH1nVesmNnRrnl5/QklRCNc\n",
                "g1VY63cMhHtSfcnKf68xWnbJvmaFEEMIMYQExRp0Vzeg9V+GtfI4aCNoNWa+zU4IDk5e7Odlyzj9\n",
                "J5oVqCDKfeP1dn/ATmAFI9Z8EhQQDYW+IVhs6OVLcsrhYrK05EGnxD+OykV/9jRMsmPVkIGGCMUF\n",
                "/96x6CXwkyRUKiJQp3MkM114rf6Ktb+5cU4QnX0zBQxjcQIyfg7HLZaRk2aVk0MjkccGJJHxS6GW\n",
                "7WZYyKhnUdXa8x4rgFUuGg5K7ymi3ExrTUf2aUg4jRd2pgPK4aB36oyOnPj6FXEWgdTvR2uZZkgH\n",
                "MMDIHi1rAxENTX+nWdxKA/jCYj6KYSG8tsL6c4SX60hIpspadlD3rVdm4SdmP32X0qAvDhrUdGUO\n",
                "2zHJzLxRML3GrZcLo2HFsAmRJrrCPtuPgtBSXgK7RwAcYPJKqqatOAY7YDeiDV/qmxSlT+xmAmrt\n",
                "U5y13J030mPYDHYi4GiwO8MJMfzgQ0msHzQJOdHT+hathb1UvboNO+zPxXTDU8ZK40lQ8IFiR15U\n",
                "LfbTan3383VSKrrTIAnAiLYu2TZqMMezUCzHBduDabsSZ3vUXnDgatu1R6ApBmC5Rrx8y1bxKIAH\n",
                "RFjwIH4hc9HmSA3iGWQ5sBWPfZcKVW05n4wTVfgU8Tw3S6zdb3AJW4q2n7m8zs5TxbfDmXQEcGMB\n",
                "4B35mIiUhWn/e+IXy8R1mkGrvCi1Wmtyzy3ZjFx+wQcFR0Hnog4EQVmNMf2hjU4TipG/y60JKQpr\n",
                "jQM4aMMAltRQJWNqDCgrzqodgKN57Q15pBGQixKHpGx9VVoJToSg5oHsSJn9URJTSO0tvXoAAAAA\n",
                "AAAAAAAA\n",
                "\n",
                "\n",
                ""};

            ByteArrayOutputStream baos = Streams.newByteArrayOutputStream();
            for (String line : lines) {
                baos.write(line.getBytes());
            }
            final InputStream is = Streams.asInputStream(baos);
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
            final MailMessage mail = MimeMessageConverter.convertMessage(mimeMessage);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            assertTrue("Detected a body object, but shouldn't be there.", !jsonMailObject.hasAndNotNull("body"));

            assertTrue("Missing S/MIME body text.", jsonMailObject.hasAndNotNull("smime_body_text"));
            assertTrue("Missing S/MIME body data.", jsonMailObject.hasAndNotNull("smime_body_data"));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
