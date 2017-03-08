package com.openexchange.userfeedback.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
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
@PrepareForTest({ Services.class, FeedbackService.class, SSLSocketFactoryProvider.class, FeedbackMailServiceSMTP.class})
public class FeedbackMailServiceSMTPTest {

    @Mock
    private ConfigurationService configService;
    @Mock
    private FeedbackService feedbackService;
    
    private FeedbackMailFilter filter;
    
    private Properties properties;
    
    @Before
    public void setUp() throws Exception {
        filter = new FeedbackMailFilter("1", new HashMap<String, String>(),  "sub", "body", 0l, 0l, "");
        
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(configService);
        PowerMockito.when(Services.getService(FeedbackService.class)).thenReturn(feedbackService);
        PowerMockito.when(Services.getService(FeedbackService.class)).thenReturn(feedbackService);
        
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
        
        PowerMockito.doReturn(properties).when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "getSMTPProperties")).withNoArguments();
        filter.getRecipients().put("dsfa", "");
        try {
            serviceSpy.sendFeedbackMail(filter);
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES);
            return;
        }
        // should never get here
        assertFalse(true);
    }
    
    @Test
    public void sendFeedbackMail_FailInvalidSMTP() throws Exception {
        FeedbackMailServiceSMTP service = new FeedbackMailServiceSMTP();
        FeedbackMailServiceSMTP serviceSpy = PowerMockito.spy(service);
        
        PowerMockito.doReturn(properties).when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "getSMTPProperties")).withNoArguments();
        
        filter.getRecipients().put("dsfa@blub.de", "");
        try {
            serviceSpy.sendFeedbackMail(filter);
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION);
            return;
        }
        // should never get here
        assertFalse(true);
    }
}
