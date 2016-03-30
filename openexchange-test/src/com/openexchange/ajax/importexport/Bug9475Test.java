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

package com.openexchange.ajax.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
import junit.framework.AssertionFailedError;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;

/**
 * Checks if the problem described in bug 9475 appears again.
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
     * @param name name of the test.
     */
    public Bug9475Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        tmp.delete();
        super.tearDown();
    }

    /**
     * Checks if the vcard tokenizer is too slow to parse a big unuseful file.
     * @throws Throwable if an exception occurs.
     */
    public void testBigFile() throws Throwable {
        final AJAXClient client = getClient();
        try {
            final VCardImportResponse iResponse = Tools.importVCard(client,
                new VCardImportRequest(client.getValues().getPrivateContactFolder(),
                    new FileInputStream(tmp), false));
            assertTrue("VCard importer does not give an error.", iResponse.hasError());
        } catch (final AssertionFailedError assertionFailed) {
            // Response Parsing dies with an AssertionFailedError on a response code different from 200, which is also okay in our case
            assertTrue(assertionFailed.getMessage().contains("Response code"));
            // quite a hack
        }
    }
}
