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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;

/**
 * Checks if the problem described in bug 9475 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug9475Test extends AbstractAJAXSession {

    private static final int SIZE = 30 * 1024 * 1024;

    /**
     * Big tmp file with lots of unuseful bytes.
     */
    private File tmp;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public Bug9475Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tmp = File.createTempFile("tmp", null);
        final FileOutputStream fos = new FileOutputStream(tmp);
        try {
            final Random rand = new Random(System.currentTimeMillis());
            final byte[] buf = new byte[512];
            for (int i = 0; i < SIZE; i = i + buf.length) {
                rand.nextBytes(buf);
                fos.write(buf);
            }
        } finally {
            fos.close();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            tmp.delete();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Checks if the vcard tokenizer is too slow to parse a big unuseful file.
     */
    @Test
    public void testBigFile() {
        final AJAXClient client = getClient();
        try {
            final VCardImportResponse iResponse = Tools.importVCard(client, new VCardImportRequest(client.getValues().getPrivateContactFolder(), new FileInputStream(tmp), false));
            assertTrue("VCard importer does not give an error.", iResponse.hasError());
        } catch (Exception assertionFailed) {
            // Response Parsing dies with an AssertionFailedError on a response code different from 200, which is also okay in our case
            assertTrue(assertionFailed.getMessage().contains("Response code"));
            // quite a hack
        }
    }
}
