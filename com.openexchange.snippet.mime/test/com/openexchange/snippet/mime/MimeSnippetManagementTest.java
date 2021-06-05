/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.snippet.mime;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Enumeration;
import java.util.Set;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link MimeSnippetManagementTest}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MimeMessageUtility.class, MessageUtility.class })
public class MimeSnippetManagementTest {

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private Enumeration<Header> headers;

    @Mock
    private Multipart multipart;

    private BodyPart bodypart;

    /**
     * Initializes a new {@link MimeSnippetManagementTest}.
     */
    public MimeSnippetManagementTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(mimeMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null)).thenReturn("multipart/form-data");
        Mockito.when(mimeMessage.getAllHeaders()).thenReturn(headers);
        Mockito.when(B(headers.hasMoreElements())).thenReturn(Boolean.TRUE, Boolean.FALSE);
        Mockito.when(headers.nextElement()).thenReturn(new Header("createdby", "0"));

        bodypart = new MimeBodyPart();

        Mockito.when(multipart.getBodyPart(ArgumentMatchers.anyInt())).thenReturn(bodypart);

        PowerMockito.mockStatic(MimeMessageUtility.class);
        PowerMockito.when(MimeMessageUtility.getMultipartContentFrom((Part) ArgumentMatchers.any())).thenReturn(multipart);
        PowerMockito.when(MimeMessageUtility.decodeMultiEncodedHeader(ArgumentMatchers.anyString())).thenReturn("0");
        PowerMockito.mockStatic(MessageUtility.class);
        PowerMockito.when(MessageUtility.readMimePart((Part) ArgumentMatchers.any(), (ContentType) ArgumentMatchers.any())).thenReturn("MyContent");
    }

    @Test
    public void testForBug38201() {
        // @formatter:off
        String htmlContent = "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\">\n" +
            "<a style=\"text-decoration:none\" href=\"http://www.testfirma.test\" >\n" +
            "<img src=\"https://my.cool.image/as34/foo.png\" alt=\"TESTfirma\" height=\"73\" width=\"360\" border=\"0\">\n" +
            "</a>\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica, Arial, sans-serif; font-size: 10px; line-height: 12px; color: #212121;\"><span style=\"font-weight: bold; display: inline;\" >testmann</span>\n" +
            "<span style=\"display: inline;\" >/</span> <span style=\"display: inline;\" >CEO</span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<a href=\"mailto:test@test.de\" style=\"text-decoration: none; display: inline;\">test@test.de</a><span style=\"display: inline;\" > / </span><span style=\"display: inline;\" >123456789</span></p>\n" +
            "<p style=\"font-family: Helvetica, Arial, sans-serif; font-size: 10px; line-height: 12px\">\n" +
            "<span style=\"font-weight: bold; display: inline;\" >TESTfirma</span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<span style=\"display: inline;\" >Office: </span> <span style=\"display: inline;\" >456789</span>\n" +
            "<span style=\"display: inline;\" >/ Fax: </span> <span style=\"display: inline;\" >879564521</span>\n" +
            "<span style=\"display: inline;\" ><br></span> <span style=\"display: inline;\" >Teststraße 12</span>\n" +
            "<span ></span> <span style=\"\" ></span>\n" +
            "<span style=\"display: inline;\" ><br></span>\n" +
            "<a href=\"http://www.testfirma.test\" style=\"text-decoration: none; display: inline;\">http://www.testfirma.test</a>\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\">\n" +
            "</p>\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 10px; line-height: 12px;\" >\n" +
            "<a href=\"https://https://my.cool.image?utm_source=email&amp;utm_medium=sig&amp;utm_campaign=email%20banner\">\n" +
            "<img src=\"https://my.cool.image/as34/banner.png\" alt=\"signature.biz\" height=\"35\" width=\"300\" border=\"0\">\n" +
            "</a>\n" +
            "</p>\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "<p style=\"font-family: Helvetica,Arial,sans-serif; font-size: 9px; line-height: 12px;\" ></p>";

        Set<String> contentIDs = MimeSnippetManagement.extractContentIDs(htmlContent);
        assertNotNull(contentIDs);
        assertTrue(contentIDs.isEmpty());
    }

}
