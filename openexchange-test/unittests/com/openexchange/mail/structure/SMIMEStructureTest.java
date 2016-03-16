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

import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link SMIMEStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMIMEStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     */
    public SMIMEStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     *
     * @param name The test name
     */
    public SMIMEStructureTest(final String name) {
        super(name);
    }

    public void testAnotherMIMEStructure() {
        try {
            getSession();

            final byte[] smime = (
                "From: \"Jane Doe\" <jane@spoof.de>\n" +
                "To: <barfoo@trub.org>\n" +
                "Subject: SMIME\n" +
                "Date: Thu, 30 Jun 2011 11:40:19 +0200\n" +
                "Message-ID: <000233b01cc3709$ba120c00$2e362400$@de>\n" +
                "MIME-Version: 1.0\n" +
                "Content-Language: de\n" +
                "Content-Type: multipart/signed;\n" +
                "    protocol=\"application/x-pkcs7-signature\";\n" +
                "    micalg=MD5;\n" +
                "    boundary=\"----=_NextPart_000_0003_01CC371A.78F2B0B0\"\n" +
                "\n" +
                "This is a multi-part message in MIME format.\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
                "Content-Type: multipart/related;\n" +
                "    boundary=\"----=_NextPart_001_0004_01CC371A.78F2B0B0\"\n" +
                "\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: multipart/alternative;\n" +
                "    boundary=\"----=_NextPart_002_0005_01CC371A.78F2D7C0\"\n" +
                "\n" +
                "\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
                "Content-Type: text/plain;\n" +
                "    charset=\"UTF-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "Hallo Peter Pan,\n" +
                "\n" +
                "=20\n" +
                "\n" +
                "blah blah blah.\n" +
                "\n" +
                "\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
                "Content-Type: text/html;\n" +
                "    charset=\"UTF-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html xmlns:v=3D\"urn:schemas-microsoft-com:vml\" =\n" +
                "xmlns:o=3D\"urn:schemas-microsoft-com:office:office\" =\n" +
                "xmlns:w=3D\"urn:schemas-microsoft-com:office:word\" =\n" +
                "xmlns:m=3D\"http://schemas.microsoft.com/office/2004/12/omml\" =\n" +
                "xmlns=3D\"http://www.w3.org/TR/REC-html40\"><head><meta http-equiv=3DContent-Type content=3D\"text/html; charset=3Dutf-8\">\n" +
                "</head><body lang=3DDE link=3Dblue =\n" +
                "vlink=3Dpurple><div class=3DWordSection1><p class=3DMsoNormal>Hallo =\n" +
                "Dirk,<o:p></o:p></p><p class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
                "class=3DMsoNormal>blah blah blah. =\n" +
                "<o:p></o:p></p><p =\n" +
                "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
                "</body></html>\n" +
                "------=_NextPart_002_0005_01CC371A.78F2D7C0--\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: image/jpeg;\n" +
                "    name=\"image001.jpg\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-ID: <image001.jpg@01CC371A.78CBEED0>\n" +
                "\n" +
                "/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAVQAA/+4ADkFkb2JlAGTAAAAAAf/b\n" +
                "AIQAAgEBAQEBAgEBAgMCAQIDAwICAgIDAwMDAwMDAwQDBAQEBAMEBAUGBgYFBAcHCAgHBwoKCgoK\n" +
                "BoA0AaANAGgDQBoA0AaANAGgDQBoA0AaANAYLf4B/l+U3+J8vw/tfw+/QAr8C/L8wfP/AE/Z/H7t\n" +
                "AZ6ANAay/Mb4fH7Pj8pfm/j/AFaA+pfAfh8w/L8PmH/z9+gM9AGgDQBoDQl/7Kvyf2Pl+f4D839W\n" +
                "heCN+hA0B//Z\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
                "Content-Type: image/jpeg;\n" +
                "    name=\"image002.jpg\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-ID: <image002.jpg@01CC371A.78CBEED0>\n" +
                "\n" +
                "/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAVQAA/+4ADkFkb2JlAGTAAAAAAf/b\n" +
                "AIQAAgEBAQEBAgEBAgMCAQIDAwICAgIDAwMDAwMDAwQDBAQEBAMEBAUGBgYFBAcHCAgHBwoKCgoK\n" +
                "/i3vJvoLx36yL1o/RGfi5SePGO8HwtQ4rLMnGC2vqvwRWHlVZtobzHiyrjy3yzl2ZTZwY4DuoPaO\n" +
                "5em2wZuLC3G2Babb0Yv+K8p6jy87KzJ3eSEhfLbt0dhpECLxAi30s1KiqIOr60i//9k=\n" +
                "\n" +
                "------=_NextPart_001_0004_01CC371A.78F2B0B0--\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
                "Content-Type: application/x-pkcs7-signature;\n" +
                "    name=\"smime.p7s\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=\"smime.p7s\"\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExDjAMBggqhkiG9w0CBQUAMIAGCSqGSIb3DQEHAQAAoIISdjCC\n" +
                "A58wggKHoAMCAQICASYwDQYJKoZIhvcNAQEFBQAwcTELMAkGA1UEBhMCREUxHDAaBgNVBAoTE0Rl\n" +
                "+143qlXSfnnnWFAgjCePzaRpl6nzl/WUqBNWldF9POxRqhTnuze1Kt06AyfqxK2Z8fX8FUj8h5Tj\n" +
                "sYOMGi8yNP9Ik5fjuUeLWSoVPnor3rE5YW68xyY79VLOb2pLIqMsF7x0XXkGVn31AAAAAAAA\n" +
                "\n" +
                "------=_NextPart_000_0003_01CC371A.78F2B0B0--\n" +
                "\n").getBytes();

            final MailMessage mail = MimeMessageConverter.convertMessage(smime);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

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

    public void testYetAnotherMIMEStructure2() {
        try {
            getSession();

            final byte[] smime = (
                "From: asdsad@gmx.de\n" +
                "Content-Type: multipart/signed; boundary=\"Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\"; protocol=\"application/pkcs7-signature\"; micalg=sha1\n" +
                "Date: Wed, 14 May 2014 08:56:15 -0400\n" +
                "Subject: My subject\n" +
                "To: hgjjgh@open-xchange.com\n" +
                "Message-Id: <7029863B-C54D-403B-B29C-9BAFD00E0DBABE@gmx.de>\n" +
                "Mime-Version: 1.0 (Mac OS X Mail 7.2 \\(1874\\))\n" +
                "X-Mailer: Apple Mail (2.1874)\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/plain;\n" +
                "    charset=us-ascii\n" +
                "\n" +
                "Hi stefan\n" +
                ";)\n" +
                "stefan\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=smime.p7s\n" +
                "Content-Type: application/pkcs7-signature;\n" +
                "    name=smime.p7s\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIO2TCCBIow\n" +
                "ggNyoAMCAQICECf06hH0eobEbp27bqkXBwcwDQYJKoZIhvcNAQEFBQAwbzELMAkGA1UEBhMCU0Ux\n" +
                "FDASBgNVBAoTC0FkZFRydXN0IEFCMSYwJAYDVQQLEx1BZGRUcnVzdCBFeHRlcm5hbCBUVFAgTmV0\n" +
                "GTppEtDs5R1PU3gYbpis78Ay9ZhjHpmM7ip3IX+PAGqsO3K7ASfZk68yeqpyuBy8mr3jA4cbymMh\n" +
                "pAHl6SOzA2VJqEhh+CxDWfBlzdMm3v/JXAIE4WJAUhiD7XW3lqX1O2RBTwLHUypPAx3/8B1to6Gn\n" +
                "Bvjl9VXHCRRbimvMR6+mRXJaXdyF9Q8kyBtQ1YQMpgPln+C5svwRQK5znMMDow6ky6zCnVPvx6TA\n" +
                "dRqQbotPXRhJYf2hYV62t5QMZuz4Y7fHB6k6+VVdhC1Kms3J1YxnQX52Fr7HQcOgKeRKvluug2wV\n" +
                "EkJjJDoENHQAAAAAAAA=\n" +
                "\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350--\n" +
                "").getBytes();

            final MailMessage mail = MimeMessageConverter.convertMessage(smime);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

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

    public void testYetAnotherMIMEStructure() {
        try {
            getSession();

            final byte[] smime = ("From: sadsr@dsa.nl\n" +
                "Content-Type: multipart/signed; boundary=\"Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\"; protocol=\"application/pkcs7-signature\"; micalg=sha1\n" +
                "Date: Wed, 14 May 2014 08:56:15 -0400\n" +
                "Subject: F G H J\n" +
                "To: asdd@dsa.nl>\n" +
                "Message-Id: <7029863B-C54D-403B-B29C-9BD00E0DBABE@gmx.de>\n" +
                "Mime-Version: 1.0\n" +
                "\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/plain;\n" +
                "    charset=us-ascii\n" +
                "\n" +
                "Hi Asdfg\n" +
                ";)\n" +
                "dsfgh\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350\n" +
                "Content-Disposition: attachment;\n" +
                "    filename=smime.p7s\n" +
                "Content-Type: application/pkcs7-signature;\n" +
                "    name=smime.p7s\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIO2TCCBIow\n" +
                "ggNyoAMCAQICECf06hH0eobEbp27bqkXBwcwDQYJKoZIhvcNAQEFBQAwbzELMAkGA1UEBhMCU0Ux\n" +
                "FDASBgNVBAoTC0FkZFRydXN0IEFCMSYwJAYDVQQLEx1BZGRUcnVzdCBFeHRlcm5hbCBUVFAgTmV0\n" +
                "d29yazEiMCAGA1UEAxMZQWRkVHJ1c3QgRXh0ZXJuYWwgQ0EgUm9vdDAeFw0wNTA2MDcwODA5MTBa\n" +
                "Fw0yMDA1MzAxMDQ4MzhaMIGuMQswCQYDVQQGEwJVUzELMAkGA1UECBMCVVQxFzAVBgNVBAcTDlNh\n" +
                "bHQgTGFrZSBDaXR5MR4wHAYDVQQKExVUaGUgVVNFUlRSVVNUIE5ldHdvcmsxITAfBgNVBAsTGGh0\n" +
                "dHA6Ly93d3cudXNlcnRydXN0LmNvbTE2MDQGA1UEAxMtVVROLVVTRVJGaXJzdC1DbGllbnQgQXV0\n" +
                "aGVudGljYXRpb24gYW5kIEVtYWlsMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsjmF\n" +
                "pPJ9q0E7YkY3rs3BYHW8OWX5ShpHornMSMxqmNVNNRm5pELlzkniii8efNIxB8dOtINknS4p1aJk\n" +
                "xIW9hVE1eaROaJB7HHqkkqgX8pgV8pPMyaQylbsMTzC9mKALi+VuG6JG+ni8om+rWV6lL8/K2m2q\n" +
                "L+usobNqqrcuZzWLeeEeaYji5kbNoKXqvgvOdjp6Dpvq/NonWz1zHyLmSGHGTPNpsaguG7bUMSAs\n" +
                "vIKKjqQOpdeJQ/wWWq8dcdcRWdq6hw2v+vPhwvCkxWeM1tZUOt4KpLoDd7NlyP0e03RiqhjKaJMe\n" +
                "oYV+9Udly/hNVyh00jT/MLbu9mIwFIws6wIDAQABo4HhMIHeMB8GA1UdIwQYMBaAFK29mHo0tCb3\n" +
                "+sQmVO8DveAky1QaMB0GA1UdDgQWBBSJgmd9xJ0mcABLtFBIfN49rgRufTAOBgNVHQ8BAf8EBAMC\n" +
                "AQYwDwYDVR0TAQH/BAUwAwEB/zB7BgNVHR8EdDByMDigNqA0hjJodHRwOi8vY3JsLmNvbW9kb2Nh\n" +
                "LmNvbS9BZGRUcnVzdEV4dGVybmFsQ0FSb290LmNybDA2oDSgMoYwaHR0cDovL2NybC5jb21vZG8u\n" +
                "bmV0L0FkZFRydXN0RXh0ZXJuYWxDQVJvb3QuY3JsMA0GCSqGSIb3DQEBBQUAA4IBAQAZ2IkRbyis\n" +
                "pgCi54fBm5AD236hEv0e8+LwAamUVEJrmgnEoG3XkJIEA2Z5Q3H8+G+v23ZF4jcaPd3kWQR4rBz0\n" +
                "g0bzes9bhHIt5UbBuhgRKfPLSXmHPLptBZ2kbWhPrXIUNqi5sf2/z3/wpGqUNVCPz4FtVbHdWTBK\n" +
                "322gnGQfSXzvNrv042n0+DmPWq1LhTq3Du3Tzw1EovsEv+QvcI4l+1pUBrPQxLxtjftzMizpm4Qk\n" +
                "LdZ/kXpoAlAfDj9N6cz1u2fo3BwuO/xOzf4CjuOoEwqlJkRl6RDyTVKnrtw+ymsyXEFs/vVdoOr/\n" +
                "0fqbhlhtPZZH5f4ulQTCAMyOofK7MIIFGjCCBAKgAwIBAgIQbRnqpxlPajMi5iIyeqpx3jANBgkq\n" +
                "hkiG9w0BAQUFADCBrjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAlVUMRcwFQYDVQQHEw5TYWx0IExh\n" +
                "a2UgQ2l0eTEeMBwGA1UEChMVVGhlIFVTRVJUUlVTVCBOZXR3b3JrMSEwHwYDVQQLExhodHRwOi8v\n" +
                "d3d3LnVzZXJ0cnVzdC5jb20xNjA0BgNVBAMTLVVUTi1VU0VSRmlyc3QtQ2xpZW50IEF1dGhlbnRp\n" +
                "Y2F0aW9uIGFuZCBFbWFpbDAeFw0xMTA0MjgwMDAwMDBaFw0yMDA1MzAxMDQ4MzhaMIGTMQswCQYD\n" +
                "VQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRow\n" +
                "GAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDE5MDcGA1UEAxMwQ09NT0RPIENsaWVudCBBdXRoZW50\n" +
                "aWNhdGlvbiBhbmQgU2VjdXJlIEVtYWlsIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n" +
                "AQEAkoSEW0tXmNReL4uk4UDIo1NYX2Zl8TJO958yfVXQeExVt0KU4PkncQfFxmmkuTLE8UAakMwn\n" +
                "VmJ/F7Vxaa7lIBvky2NeYMqiQfZq4aP/uN8fSG1lQ4wqLitjOHffsReswtqCAtbUMmrUZ28gE49c\n" +
                "NfrlVICv2HEKHTcKAlBTbJUdqRAUtJmVWRIx/wmi0kzcUtve4kABW0ho3cVKtODtJB86r3FfB+Os\n" +
                "vxQ7sCVxaD30D9YXWEYVgTxoi4uDD216IVfmNLDbMn7jSuGlUnJkJpFOpZIP/+CxYP0ab2hRmWON\n" +
                "GoulzEKbm30iY9OpoPzOnpDfRBn0XFs1uhbzp5v/wQIDAQABo4IBSzCCAUcwHwYDVR0jBBgwFoAU\n" +
                "iYJnfcSdJnAAS7RQSHzePa4Ebn0wHQYDVR0OBBYEFHoTTgB0W8Z4Y2QnwS/ioFu8ecV7MA4GA1Ud\n" +
                "DwEB/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEAMBEGA1UdIAQKMAgwBgYEVR0gADBYBgNVHR8E\n" +
                "UTBPME2gS6BJhkdodHRwOi8vY3JsLnVzZXJ0cnVzdC5jb20vVVROLVVTRVJGaXJzdC1DbGllbnRB\n" +
                "dXRoZW50aWNhdGlvbmFuZEVtYWlsLmNybDB0BggrBgEFBQcBAQRoMGYwPQYIKwYBBQUHMAKGMWh0\n" +
                "dHA6Ly9jcnQudXNlcnRydXN0LmNvbS9VVE5BZGRUcnVzdENsaWVudF9DQS5jcnQwJQYIKwYBBQUH\n" +
                "MAGGGWh0dHA6Ly9vY3NwLnVzZXJ0cnVzdC5jb20wDQYJKoZIhvcNAQEFBQADggEBAIXWvnhXVW0z\n" +
                "f0RS/kLVBqgBA4CK+w2y/Uq/9q9BSfUbWsXSrRtzbj7pJnzmTJjBMCjfy/tCPKElPgp11tA9OYZm\n" +
                "0aGbtU2bb68obB2v5ep0WqjascDxdXovnrqTecr+4pEeVnSy+I3T4ENyG+2P/WA5IEf7i686ZUg8\n" +
                "mD2lJb+972DgSeUWyOs/Q4Pw4O4NwdPNM1+b0L1garM7/vrUyTo8H+2b/5tJM75CKTmD7jNpLoKd\n" +
                "RU2oadqAGx490hpdfEeZpZsIbRKZhtZdVwcbpzC+S0lEuJB+ytF5OOu0M/qgOl0mWJ5hVRi0IdWZ\n" +
                "1eBDQEIwvuql55TSsP7zdfl/bucwggUpMIIEEaADAgECAhB0XW+rL1MYSBRGDYODQ6EmMA0GCSqG\n" +
                "SIb3DQEBBQUAMIGTMQswCQYDVQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVyMRAw\n" +
                "DgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDE5MDcGA1UEAxMwQ09N\n" +
                "T0RPIENsaWVudCBBdXRoZW50aWNhdGlvbiBhbmQgU2VjdXJlIEVtYWlsIENBMB4XDTE0MDUxMzAw\n" +
                "MDAwMFoXDTE1MDUxMzIzNTk1OVowJTEjMCEGCSqGSIb3DQEJARYUc3RlZmFuLmdhYmxlckBnbXgu\n" +
                "ZGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC64DZNhJxpXLBeGCdLM0Fw9tb0kr2G\n" +
                "3fvoTpMF5fUfjB73LdRArGl6mBXj7Ya5G8Qgy7EXBg215Cm9av27gWXxZJVmLG3chXXx7zq42Io3\n" +
                "NBx98u6jS5qCSlYeH7pP5PqPrQAkE181eE2g4bzlUuTXV0Todd9gGSJyXK2fLHmJ1p5EJtmuqjFX\n" +
                "LvVqxoBEtd7m091xf2YEun0GmvT8BKBaXcX+UzE7UNWpqI0m3scnW3DSoF7n9C/GHbbim+WGyyCG\n" +
                "gyhunOBOfhob7GqwquvySfaLU8BfyAHMn6/TjDCzgEAOKbcSVqiL+MARZwTi62ts8gcHsLuGpJIo\n" +
                "vRoQem3/AgMBAAGjggHkMIIB4DAfBgNVHSMEGDAWgBR6E04AdFvGeGNkJ8Ev4qBbvHnFezAdBgNV\n" +
                "HQ4EFgQUXuest2tqUffOU+XYHJyBe2PGVuswDgYDVR0PAQH/BAQDAgWgMAwGA1UdEwEB/wQCMAAw\n" +
                "IAYDVR0lBBkwFwYIKwYBBQUHAwQGCysGAQQBsjEBAwUCMBEGCWCGSAGG+EIBAQQEAwIFIDBGBgNV\n" +
                "HSAEPzA9MDsGDCsGAQQBsjEBAgEBATArMCkGCCsGAQUFBwIBFh1odHRwczovL3NlY3VyZS5jb21v\n" +
                "ZG8ubmV0L0NQUzBXBgNVHR8EUDBOMEygSqBIhkZodHRwOi8vY3JsLmNvbW9kb2NhLmNvbS9DT01P\n" +
                "RE9DbGllbnRBdXRoZW50aWNhdGlvbmFuZFNlY3VyZUVtYWlsQ0EuY3JsMIGIBggrBgEFBQcBAQR8\n" +
                "MHowUgYIKwYBBQUHMAKGRmh0dHA6Ly9jcnQuY29tb2RvY2EuY29tL0NPTU9ET0NsaWVudEF1dGhl\n" +
                "bnRpY2F0aW9uYW5kU2VjdXJlRW1haWxDQS5jcnQwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmNv\n" +
                "bW9kb2NhLmNvbTAfBgNVHREEGDAWgRRzdGVmYW4uZ2FibGVyQGdteC5kZTANBgkqhkiG9w0BAQUF\n" +
                "AAOCAQEAEGTY/sL5BJqlPwZM3fO6sG0WZM4NV3pMjm6tdYxefOuxf1jXTLE39LtvvP4kBTdcjMOF\n" +
                "lpOeGC4JDYTpQv6iDhxKePEcftdErAugIUwjtsoXOSeI8LW0v8r0TYW+uXWDxHoa2HxmcgzTbUv2\n" +
                "yNpvZpmrE0fyKxM8dY9WRDZtKu0DFYRj5lQr9Y+ExayzgTO+/nvyUW08CtRUExyEqE5RJEl41pK0\n" +
                "kElxkR6mkAPzV5I5+J6dsycu91i6K+aD0EgOTPeI5huqTzOE4ntEjVuyuzo+iRIWKLuJkgQ6le3s\n" +
                "osvVzqCjY4b5b406TCUEl2kWkZkjJAKuS3M/j85vMmolAjGCA6swggOnAgEBMIGoMIGTMQswCQYD\n" +
                "VQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRow\n" +
                "GAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDE5MDcGA1UEAxMwQ09NT0RPIENsaWVudCBBdXRoZW50\n" +
                "aWNhdGlvbiBhbmQgU2VjdXJlIEVtYWlsIENBAhB0XW+rL1MYSBRGDYODQ6EmMAkGBSsOAwIaBQCg\n" +
                "ggHXMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8XDTE0MDUxNDEyNTYx\n" +
                "NlowIwYJKoZIhvcNAQkEMRYEFCjCAHefLo7i3OacCf+g2+X8HSnZMIG5BgkrBgEEAYI3EAQxgasw\n" +
                "gagwgZMxCzAJBgNVBAYTAkdCMRswGQYDVQQIExJHcmVhdGVyIE1hbmNoZXN0ZXIxEDAOBgNVBAcT\n" +
                "B1NhbGZvcmQxGjAYBgNVBAoTEUNPTU9ETyBDQSBMaW1pdGVkMTkwNwYDVQQDEzBDT01PRE8gQ2xp\n" +
                "ZW50IEF1dGhlbnRpY2F0aW9uIGFuZCBTZWN1cmUgRW1haWwgQ0ECEHRdb6svUxhIFEYNg4NDoSYw\n" +
                "gbsGCyqGSIb3DQEJEAILMYGroIGoMIGTMQswCQYDVQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBN\n" +
                "YW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDE5\n" +
                "MDcGA1UEAxMwQ09NT0RPIENsaWVudCBBdXRoZW50aWNhdGlvbiBhbmQgU2VjdXJlIEVtYWlsIENB\n" +
                "AhB0XW+rL1MYSBRGDYODQ6EmMA0GCSqGSIb3DQEBAQUABIIBAIjCLK51hq+lk6Ii4Oxgh4GoNwRL\n" +
                "GTppEtDs5R1PU3gYbpis78Ay9ZhjHpmM7ip3IX+PAGqsO3K7ASfZk68yeqpyuBy8mr3jA4cbymMh\n" +
                "pAHl6SOzA2VJqEhh+CxDWfBlzdMm3v/JXAIE4WJAUhiD7XW3lqX1O2RBTwLHUypPAx3/8B1to6Gn\n" +
                "Bvjl9VXHCRRbimvMR6+mRXJaXdyF9Q8kyBtQ1YQMpgPln+C5svwRQK5znMMDow6ky6zCnVPvx6TA\n" +
                "dRqQbotPXRhJYf2hYV62t5QMZuz4Y7fHB6k6+VVdhC1Kms3J1YxnQX52Fr7HQcOgKeRKvluug2wV\n" +
                "EkJjJDoENHQAAAAAAAA=\n" +
                "\n" +
                "--Apple-Mail=_7F65DE4D-4C06-49D2-BFA0-63F9B3D3E350--\n").getBytes();

            final MailMessage mail = MimeMessageConverter.convertMessage(smime);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

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
