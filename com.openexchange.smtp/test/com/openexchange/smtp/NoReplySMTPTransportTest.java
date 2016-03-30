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

package com.openexchange.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import java.util.Iterator;
import java.util.UUID;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.MtaStatusInfo;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.smtp.services.Services;
import com.openexchange.version.Version;


/**
 * {@link NoReplySMTPTransportTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ContextStorage.class, Version.class})
public class NoReplySMTPTransportTest {

    private static final int CONTEXT_ID = 1;
    private static final String NO_REPLY_ADDRESS = "no-reply@ox.invalid";
    private SimpleSmtpServer server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        prepareMockServices();
        SMTPProvider.getInstance().startUp();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        SMTPProvider.getInstance().shutDown();
    }

    @Before
    public void setUp() throws Exception {
        server = SimpleSmtpServer.start(8025);
        mockStatic(Version.class);
        when(Version.getInstance()).thenReturn(new Version() {
            @Override
            public String getVersionString() {
                return "7.8.0";
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testSendMailMessage() throws Exception {
        InternetAddress[] recipients = QuotedInternetAddress.parse("otto@example.com");
        String subject = UUID.randomUUID().toString();
        String body = UUID.randomUUID().toString();

        MailTransport transport = SMTPProvider.getInstance().createNewNoReplyTransport(CONTEXT_ID);
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        mail.setRecipients(RecipientType.TO, recipients);
        mail.setSubject(subject, "ASCII");
        mail.setContent(body, "text/plain");
        MtaStatusInfo mtaStatusInfo = new MtaStatusInfo();
        transport.sendMailMessage(new ContentAwareComposedMailMessage(mail, CONTEXT_ID), ComposeType.NEW, recipients, mtaStatusInfo);
        transport.close();

        assertEquals(recipients[0], mtaStatusInfo.getSentAddresses().get(0));

        assertEquals(1, server.getReceivedEmailSize());
        @SuppressWarnings("unchecked")
        Iterator<SmtpMessage> mails = server.getReceivedEmail();
        SmtpMessage received = mails.next();
        assertEquals(NO_REPLY_ADDRESS, received.getHeaderValue("From"));
        assertNull(received.getHeaderValue("Sender"));
        assertEquals(recipients[0].toUnicodeString(), received.getHeaderValue("To"));
        assertNull(received.getHeaderValue("CC"));
        assertNull(received.getHeaderValue("BCC"));
        assertNull(received.getHeaderValue("Reply-To"));
        assertEquals(subject, received.getHeaderValue("Subject"));
        assertEquals(body, received.getBody());
    }

    @Test
    public void testSendRawMessage() throws Exception {
        InternetAddress[] recipients = QuotedInternetAddress.parse("otto@example.com");
        String subject = UUID.randomUUID().toString();
        String body = UUID.randomUUID().toString();
        String message = ""
            + "From: " + NO_REPLY_ADDRESS + "\n"
            + "To: " + recipients[0].toString() + "\n"
            + "Subject: " + subject + "\n"
            + "Content-Transfer-Encoding: 7bit\n\n"
            + body;


        MailTransport transport = SMTPProvider.getInstance().createNewNoReplyTransport(1);
        transport.sendRawMessage(message.getBytes("ASCII"));
        transport.close();

        assertEquals(1, server.getReceivedEmailSize());
        @SuppressWarnings("unchecked")
        Iterator<SmtpMessage> mails = server.getReceivedEmail();
        SmtpMessage received = mails.next();
        assertEquals(NO_REPLY_ADDRESS, received.getHeaderValue("From"));
        assertNull(received.getHeaderValue("Sender"));
        assertEquals(recipients[0].toUnicodeString(), received.getHeaderValue("To"));
        assertNull(received.getHeaderValue("CC"));
        assertNull(received.getHeaderValue("BCC"));
        assertNull(received.getHeaderValue("Reply-To"));
        assertEquals(subject, received.getHeaderValue("Subject"));
        assertEquals(body, received.getBody());
    }

    private static void prepareMockServices() throws Exception {
        mockStatic(ContextStorage.class);
        ContextService contextServiceMock = mock(ContextService.class);
        ConfigurationService configServiceMock = mock(ConfigurationService.class);
        ServiceLookup mockServices = mock(ServiceLookup.class);
        Services.setServiceLookup(mockServices);
        when(mockServices.getService(ContextService.class)).thenReturn(contextServiceMock);
        when(mockServices.getService(ConfigurationService.class)).thenReturn(configServiceMock);
        when(configServiceMock.getProperty(anyString(), anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[1];
            }
        });
        when(configServiceMock.getProperty(anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                String property = (String) arguments[0];
                if (property.equals("com.openexchange.smtp.smtpLocalhost")) {
                    return "null";
                }

                if (property.equals("com.openexchange.smtp.smtpAuthentication")) {
                    return "false";
                }

                if (property.equals("com.openexchange.smtp.smtpAuthEnc")) {
                    return "UTF-8";
                }

                if (property.equals("com.openexchange.smtp.smtpAuthentication")) {
                    return "true";
                }

                if (property.equals("com.openexchange.smtp.setSMTPEnvelopeFrom")) {
                    return "false";
                }

                if (property.equals("com.openexchange.smtp.smtpTimeout")) {
                    return "50000";
                }

                if (property.equals("com.openexchange.smtp.smtpConnectionTimeout")) {
                    return "10000";
                }

                if (property.equals("com.openexchange.smtp.logTransport")) {
                    return "true";
                }

                if (property.equals("com.openexchange.smtp.ssl.protocols")) {
                    return "SSLv3 TLSv1";
                }

                return null;
            }
        });
        when(mockServices.getService(NoReplyConfigFactory.class)).thenReturn(new NoReplyConfigFactory() {
            @Override
            public NoReplyConfig getNoReplyConfig(int contextId) throws OXException {
                return new NoReplyConfig() {

                    @Override
                    public String getServer() {
                        return "localhost";
                    }

                    @Override
                    public SecureMode getSecureMode() {
                        return SecureMode.PLAIN;
                    }

                    @Override
                    public int getPort() {
                        return 8025;
                    }

                    @Override
                    public String getPassword() {
                        return null;
                    }

                    @Override
                    public String getLogin() {
                        return null;
                    }

                    @Override
                    public InternetAddress getAddress() {
                        try {
                            return QuotedInternetAddress.parse(NO_REPLY_ADDRESS)[0];
                        } catch (AddressException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        });
    }

}
