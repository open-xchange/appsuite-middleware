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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link SearchTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SearchTest extends FileActionTest {

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
    public void testAction() throws OXException, JSONException {
        request().body(new JSONObject("{pattern: 'someSearch'}")).param("folder", "12").param("columns", "1,700,702") // id, title and filename
            .param("sort", "700").param("order", "desc").param("start", "10").param("end", "12").param("timezone", "Europe/Berlin");

        List<Field> columns = Arrays.asList(File.Field.ID, File.Field.TITLE, File.Field.FILENAME);
        fileAccess().expectCall("search", "someSearch", columns, "12", File.Field.TITLE, SortDirection.DESC, I(10), I(12)).andReturn(SearchIteratorAdapter.emptyIterator());

        perform();

        fileAccess().assertAllWereCalled();
    }

    @Override
    public AbstractFileAction createAction() {
        return new SearchAction();
    }
}
