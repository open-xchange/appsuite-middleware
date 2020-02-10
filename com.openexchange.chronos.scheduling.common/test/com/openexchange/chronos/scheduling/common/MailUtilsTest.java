
package com.openexchange.chronos.scheduling.common;

import static org.hamcrest.core.Is.is;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import com.openexchange.junit.Assert;

public class MailUtilsTest {

    private final static Map<String, String> VALUES = new HashMap<>(5, 0.99f);

    static {
        // Google
        VALUES.put("4p4goiokaablfrgo4qp3rr3ppg@google.com", "<Appointment.4p4goiokaablfrgo4qp3rr3ppg(at)google.com@open-xchange.com>");
        // Outlook
        VALUES.put("040000008200E00074C5B7101A82E0080000000073DE34C60DE0D5010000000000000000100000007B48105D1F491D4AB243D177B9CDC2D2", 
            "<Appointment.040000008200E00074C5B7101A82E0080000000073DE34C60DE0D5010000000000000000100000007B48105D1F491D4AB243D177B9CDC2D2@open-xchange.com>");
        // Apple
        VALUES.put("9A16A657-A73a-4070-9A6A-72A4757A8239", "<Appointment.9A16A657-A73a-4070-9A6A-72A4757A8239@open-xchange.com>");
        // OX
        VALUES.put("a2a5a9a2-7a89-4f88-9af7-ab72cfe62055", "<Appointment.a2a5a9a2-7a89-4f88-9af7-ab72cfe62055@open-xchange.com>");
    }

    @Test
    public void test_MWB69_ReplyTo_sanitizeValues() throws Exception {
        for (Entry<String, String> entry : VALUES.entrySet()) {
            Assert.assertThat("Wrong header value", MailUtils.generateHeaderValue(entry.getKey(), false), is(entry.getValue()));
        }
    }

}
