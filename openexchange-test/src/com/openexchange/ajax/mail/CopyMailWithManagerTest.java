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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.exception.OXException;

/**
 * {@link CopyMailWithManagerTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CopyMailWithManagerTest extends AbstractMailTest {

    private UserValues values;

    public CopyMailWithManagerTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
        clearFolder(values.getSentFolder());
        clearFolder(values.getInboxFolder());
        clearFolder(values.getDraftsFolder());
    }

    @Test
    public void testShouldCopyFromSendToDrafts() throws OXException, JSONException, IOException {
        MailTestManager manager = new MailTestManager(getClient(), false);
        String destination = values.getDraftsFolder();

        TestMail myMail = new TestMail(values.getSendAddress(), values.getSendAddress(), "Testing copy with manager", "alternative", "Copying a mail we just sent and received vom the inbox to the draft folder");
        myMail = manager.send(myMail);

        TestMail movedMail = manager.copy(myMail, destination);
        assertFalse("Should get no errors when copying e-mail", manager.getLastResponse().hasError());
        String newID = movedMail.getId();

        manager.get(destination, newID);
        assertFalse("Should get no errors when getting copied e-mail", manager.getLastResponse().hasError());
        assertFalse("Should produce no conflicts when getting copied e-mail", manager.getLastResponse().hasConflicts());

        manager.get(myMail.getFolderAndId());
        assertFalse("Should still find original e-mail", manager.getLastResponse().hasError());

        manager.cleanUp();

        manager.get(destination, newID);
        assertTrue("Should not find copied e-mail after cleaning up", manager.getLastResponse().hasError());

        manager.get(myMail.getFolderAndId());
        assertTrue("Should not find original e-mail after cleaning up", manager.getLastResponse().hasError());
    }

}
