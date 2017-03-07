package com.openexchange.userfeedback.mail.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
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
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;
import com.openexchange.userfeedback.mail.transport.TransportHandler;

/**
 * {@link FeedbackMailServiceSMTPTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, FeedbackService.class, SSLSocketFactoryProvider.class, TransportHandler.class, FeedbackMailServiceSMTP.class})
public class FeedbackMailServiceSMTPTest {

    @Mock
    private ConfigurationService configService;
    @Mock
    private FeedbackService feedbackService;
    
    FeedbackMailFilter filter;
    
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
        
    }
    
    @Test
    public void sendFeedbackMail_testAll() throws Exception {
        FeedbackMailServiceSMTP service = new FeedbackMailServiceSMTP();
        FeedbackMailServiceSMTP serviceSpy = PowerMockito.spy(service);
        
        PowerMockito.doReturn("Name").when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "getSocketFactoryClassName")).withNoArguments();
        PowerMockito.doReturn(true).when(serviceSpy, PowerMockito.method(FeedbackMailServiceSMTP.class, "send")).withArguments(Matchers.any(Session.class), Matchers.any(Address[].class), Matchers.any(MimeMessage.class));
        
        serviceSpy.sendFeedbackMail(filter);
    }
}
