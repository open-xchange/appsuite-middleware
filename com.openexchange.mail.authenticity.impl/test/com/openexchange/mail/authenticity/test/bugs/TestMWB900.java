package com.openexchange.mail.authenticity.test.bugs;

import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.Test;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.test.AbstractTestMailAuthenticity;

/**
 * {@link BugsTestSuite}
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.6
 */
public class TestMWB900 extends AbstractTestMailAuthenticity{
    
    /**
     * Initialises a new {@link TestMWB900}.
     */
    public TestMWB900() {
        super();
    }

    /**
     * MWB900 Test
     */
    @Test
    public void test() throws AddressException {
        fromAddresses[0] = new InternetAddress("Jane Doe <jane.doe@aliceland.com>");
        perform("ox.io; dkim=pass header.i=@aliceland.com header.s=201705; spf=pass smtp.mailfrom=aliceland.com; dmarc=pass(p=REJECT) header.from=aliceland.com;");
        assertStatus(MailAuthenticityStatus.PASS, result.getStatus());
        assertDomain("aliceland.com", result.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN, String.class));
        assertAmount(3);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(0), "aliceland.com", SPFResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(1), "aliceland.com", DKIMResult.PASS);
        assertAuthenticityMechanismResult((MailAuthenticityMechanismResult) result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class).get(2), "aliceland.com", DMARCResult.PASS);
    }

}
