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

package com.openexchange.file.storage.json.actions.files;

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;

/**
 * {@link UpdateTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateTest extends FileActionTest {

    @Test
    public void testMissingParameters() {
        try {
            action.handle(request());
            fail("Expected Exception due to missing parameters");
        } catch (OXException x) {
            assertTrue(true);
        }
    }

    @Test
    public void testNoUpload() throws JSONException, OXException {
        request().param("timestamp", "1337").body(new JSONObject("{id: '23', folder_id: '12', title: 'nice title'}"));

        DefaultFile file = new DefaultFile();
        file.setId("23");
        file.setFolderId("12");
        file.setTitle("nice title");

        fileAccess().expectCall("saveFileMetadata", file, L(1337l), Arrays.asList(File.Field.FOLDER_ID, File.Field.TITLE, File.Field.ID));

        perform();

        fileAccess().assertAllWereCalled();
    }

    @Test
    public void testMissingId() throws JSONException {
        request().param("timestamp", "1337").body(new JSONObject("{folder_id: '12', title: 'nice title'}"));
        try {
            action.handle(request);
            fail("Expected Exception due to missing parameters");
        } catch (OXException x) {
            assertTrue(true);
        }

    }

    @Test
    public void testUpload() {
        // TODO
    }

    @Override
    public AbstractFileAction createAction() {
        return new UpdateAction();
    }
}
