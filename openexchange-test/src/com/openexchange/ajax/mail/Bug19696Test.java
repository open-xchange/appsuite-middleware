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

import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.java.Charsets;

/**
 * {@link Bug19696Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Bug19696Test extends AbstractMailTest {

    private String folder;
    private String[] ids;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Bug19696Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = getClient().getValues().getInboxFolder();
        final String mail = TestMails.BUG_19696_MAIL;
        final ImportMailRequest request = new ImportMailRequest(folder, 0, Charsets.UTF_8, mail);
        final ImportMailResponse response = getClient().execute(request);
        ids = response.getIds()[0];
    }

    /**
     * Tests the <code>action=get</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testGet() throws Throwable {
        {
            final GetRequest request = new GetRequest(folder, ids[1]);
            request.setUnseen(true);
            request.setSource(true);
            request.setSave(true);
            getClient().execute(request);
        }
    }

}
