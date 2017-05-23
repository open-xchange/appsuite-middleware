package com.openexchange.groupware.upload.quotachecker;

import org.junit.Assert;
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
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.mail.usersetting.UserSettingMail;


/**
 * Unit tests for {@link MailUploadQuotaCheckerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UserSettingMail.class, ServerConfig.class })
public class MailUploadQuotaCheckerTest {

    /**
     * Class under test
     */
    private MailUploadQuotaChecker mailUploadQuotaChecker;

    @Mock
    private UserSettingMail userSettingMail;

    private final long quota = 10000000L;

    private final long quotaFromFile = 9999L;

    private final long unlimitedQuota = 0L;

    private final long unlimitedQuota2 = -1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(ServerConfig.class);
        PowerMockito.when(ServerConfig.getLong((Property) Matchers.any())).thenReturn(quotaFromFile);
    }

    @Test(expected = IllegalArgumentException.class)
     public void testMailUploadQuotaCheckerUserSettingMail_userSettingsNull_throwException() {
        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(null);
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_bothZero_maxQuotaZero() {
        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long quotaMax = this.mailUploadQuotaChecker.getQuotaMax();

        Assert.assertEquals(this.unlimitedQuota2, quotaMax);
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_bothZero_maxQuotaPerFileNegativ() {
        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long fileQuotaMax = this.mailUploadQuotaChecker.getFileQuotaMax();

        Assert.assertEquals(this.unlimitedQuota2, fileQuotaMax);
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_uploadQuotaSetInDB_setUploadQuota() {
        Mockito.when(userSettingMail.getUploadQuota()).thenReturn(this.quota);

        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long quotaMax = this.mailUploadQuotaChecker.getQuotaMax();

        Assert.assertEquals(this.quota, quotaMax);
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_uploadQuotaPerFileSetInDB_setUploadQuotaPerFile() {
        Mockito.when(userSettingMail.getUploadQuotaPerFile()).thenReturn(this.quota);

        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long fileQuotaMax = this.mailUploadQuotaChecker.getFileQuotaMax();

        Assert.assertEquals(this.quota, fileQuotaMax);
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_uploadQuotaNegativ_setUploadQuotaFromServerProperties() {
        Mockito.when(userSettingMail.getUploadQuota()).thenReturn(-1L);

        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long quotaMax = this.mailUploadQuotaChecker.getQuotaMax();

        Assert.assertEquals(this.quotaFromFile, quotaMax);
        Assert.assertEquals(this.unlimitedQuota2, this.mailUploadQuotaChecker.getFileQuotaMax());
    }

     @Test
     public void testMailUploadQuotaCheckerUserSettingMail_uploadQuotaNegativAndServerConfigException_setUploadQuotaToZero() throws OXException {
        Mockito.when(userSettingMail.getUploadQuota()).thenReturn(-1L);
        PowerMockito.when(ServerConfig.getLong((Property) Matchers.any())).thenThrow(new OXException());

        this.mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);

        long quotaMax = this.mailUploadQuotaChecker.getQuotaMax();

        Assert.assertEquals(unlimitedQuota, quotaMax);
        Assert.assertEquals(this.unlimitedQuota2, this.mailUploadQuotaChecker.getFileQuotaMax());
    }
}
