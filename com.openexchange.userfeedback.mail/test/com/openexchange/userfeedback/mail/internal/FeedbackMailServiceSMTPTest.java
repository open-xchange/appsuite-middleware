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
package com.openexchange.userfeedback.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Transport;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

/**
 * {@link FeedbackMailServiceSMTPTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, FeedbackService.class, SSLSocketFactoryProvider.class, FeedbackMailServiceSMTP.class, LeanConfigurationService.class, Transport.class})
public class FeedbackMailServiceSMTPTest {

    @Mock
    private ConfigurationService configService;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private LeanConfigurationService leanConfigurationService;
    @Mock
    private Transport transport;

    private FeedbackMailFilter filter;

    private Properties properties;

    @Before
    public void setUp() throws Exception {
        filter = new FeedbackMailFilter("1", new HashMap<String, String>(),  "sub", "body", 0l, 0l, "", false);

        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(configService);
        PowerMockito.when(Services.getService(FeedbackService.class)).thenReturn(feedbackService);
        PowerMockito.when(Services.getService(FeedbackService.class)).thenReturn(feedbackService);
        PowerMockito.when(Services.getService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);

        Mockito.when(configService.getProperty(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn("");
        Mockito.when(configService.getIntProperty(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyInt())).thenReturn(1);

        ExportResultConverter value = new ExportResultConverter() {

            @Override
            public ExportResult get(ExportType type) {
                return new ExportResult() {
                    @Override
                    public Object getResult() {
                        String source = "This is the source of my input stream";
                        InputStream in = null;
                        try {
                            in = IOUtils.toInputStream(source, "UTF-8");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return in;
                    }
                };
            }
        };
        Mockito.when(feedbackService.export("1", filter)).thenReturn(value);
        properties = new Properties();
    }

    @Test
    public void sendFeedbackMail_FailInvalidAddresses() throws Exception {
        FeedbackMailServiceSMTP service = new FeedbackMailServiceSMTP();
        FeedbackMailServiceSMTP serviceSpy = PowerMockito.spy(service);

        PowerMockito.whenNew(Transport.class).withAnyArguments().thenReturn(transport);
        PowerMockito.doNothing().when(transport).connect(Matchers.any(String.class), Matchers.any(Integer.class), Matchers.any(String.class), Matchers.any(String.class));

        PowerMockito.doReturn(properties).when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "getSMTPProperties")).withArguments(leanConfigurationService);
        filter.getRecipients().put("dsfa", "");
        try {
            serviceSpy.sendFeedbackMail(filter);
        } catch (OXException e) {
            assertEquals(FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES, e.getExceptionCode());
            return;
        }
        // should never get here
        assertFalse(true);
    }

    @Test
    public void sendFeedbackMail_FailInvalidSMTP() throws Exception {
        FeedbackMailServiceSMTP service = new FeedbackMailServiceSMTP();
        FeedbackMailServiceSMTP serviceSpy = PowerMockito.spy(service);

        PowerMockito.doReturn(properties).when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "getSMTPProperties")).withArguments(leanConfigurationService);

        filter.getRecipients().put("dsfa@blub.de", "");
        try {
            serviceSpy.sendFeedbackMail(filter);
        } catch (OXException e) {
            assertEquals(FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION, e.getExceptionCode());
            return;
        }
        // should never get here
        assertFalse(true);
    }
}
