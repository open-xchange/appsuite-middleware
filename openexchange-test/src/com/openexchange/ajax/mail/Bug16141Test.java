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

package com.openexchange.ajax.mail;

import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug16141Test extends AbstractAJAXSession {


    private String folder;

    private UserValues values;

    private String testMailDir;

    private String address;

    public Bug16141Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
        folder = values.getInboxFolder();
        address = getClient().getValues().getSendAddress();
        testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
    }

    @Test
    public void testMailImport() throws Exception {
        InputStream[] is = createABunchOfMails();

        final ImportMailRequest importReq = new ImportMailRequest(folder, MailFlag.SEEN.getValue(), is);
        final ImportMailResponse importResp = getClient().execute(importReq);
        JSONArray json = (JSONArray) importResp.getData();

        int err = 0;
        for (int i = 0; i < json.length(); i++) {
            JSONObject jo = json.getJSONObject(i);
            if (jo.has("Error")) {
                err++;
            }
        }

        if (err != 1) {
            fail("Number of corrupt mails is wrong");
        }

        if (json.length() - err != 3) {
            fail("Import did not run til end.");
        }
    }

    private InputStream[] createABunchOfMails() {
        List<InputStream> retval = new ArrayList<InputStream>(4);
        for (String fileName : new String[] { "bug16141_1.eml", "bug16141_2.eml", "bug16141_3.eml", "bug16141_4.eml" }) {
            try {
                retval.add(getMailAndReplaceAddress(fileName));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        return retval.toArray(new InputStream[retval.size()]);
    }

    private InputStream getMailAndReplaceAddress(String fileName) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(testMailDir, fileName)), "UTF-8")) {
            char[] buf = new char[512];
            int length;
            StringBuilder sb = new StringBuilder();
            while ((length = isr.read(buf)) != -1) {
                sb.append(buf, 0, length);
            }
            return new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), address).getBytes(com.openexchange.java.Charsets.UTF_8));
        }
    }

}
