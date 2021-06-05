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

package com.openexchange.ajax.folder.actions;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link VisibleFoldersResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VisibleFoldersResponse extends AbstractAJAXResponse {

    private final int[] columns;

    private final Collection<Object[]> privateFolders;

    private final Collection<Object[]> publicFolders;

    private final Collection<Object[]> sharedFolders;

    public VisibleFoldersResponse(final Response response, final int[] columns) throws JSONException {
        super(response);
        this.columns = columns;
        if (false == response.hasError()) {
            final List<List<Object[]>> parsedResponse = parseResponse();
            privateFolders = parsedResponse.get(0);
            publicFolders = parsedResponse.get(1);
            sharedFolders = parsedResponse.get(2);
        } else {
            privateFolders = null;
            publicFolders = null;
            sharedFolders = null;
        }
    }

    private List<List<Object[]>> parseResponse() throws JSONException {
        final JSONObject object = (JSONObject) getResponse().getData();
        List<List<Object[]>> ret = new ArrayList<List<Object[]>>(3);
        // Parse private folders
        if (object.has("private")) {
            final Object[][] privateArray = parseData(object.getJSONArray("private"));
            ret.add(Collections.unmodifiableList(Arrays.asList(privateArray)));
        } else {
            ret.add(Collections.<Object[]> emptyList());
        }
        // Parse public folders
        if (object.has("public")) {
            final Object[][] publicArray = parseData(object.getJSONArray("public"));
            ret.add(Collections.unmodifiableList(Arrays.asList(publicArray)));
        } else {
            ret.add(Collections.<Object[]> emptyList());
        }
        // Parse shared folders
        if (object.has("shared")) {
            final Object[][] sharedArray = parseData(object.getJSONArray("shared"));
            ret.add(Collections.unmodifiableList(Arrays.asList(sharedArray)));
        } else {
            ret.add(Collections.<Object[]> emptyList());
        }
        // Return
        return ret;
    }

    private static Object[][] parseData(final JSONArray array) throws JSONException {
        final Object[][] values = new Object[array.length()][];
        for (int i = 0; i < array.length(); i++) {
            try {
                // insert or update
                final JSONArray inner = array.getJSONArray(i);
                values[i] = new Object[inner.length()];
                for (int j = 0; j < inner.length(); j++) {
                    if (inner.isNull(j)) {
                        values[i][j] = null;
                    } else {
                        values[i][j] = inner.get(j);
                    }
                }
            } catch (JSONException e) {
                // delete
                values[i] = new Integer[] { I(array.getInt(i)) };
            }
        }
        return values;
    }

    /**
     * Gets the private folders.
     *
     * @return The private folders
     * @throws OXException If parsing response to folders fails
     */
    public Iterator<FolderObject> getPrivateFolders() throws OXException {
        return getFoldersFrom(privateFolders);
    }

    /**
     * Gets the public folders.
     *
     * @return The public folders
     * @throws OXException If parsing response to folders fails
     */
    public Iterator<FolderObject> getPublicFolders() throws OXException {
        return getFoldersFrom(publicFolders);
    }

    /**
     * Gets the shared folders.
     *
     * @return The shared folders
     * @throws OXException If parsing response to folders fails
     */
    public Iterator<FolderObject> getSharedFolders() throws OXException {
        return getFoldersFrom(sharedFolders);
    }

    private Iterator<FolderObject> getFoldersFrom(final Collection<Object[]> col) throws OXException {
        final List<FolderObject> folders = new ArrayList<FolderObject>();
        for (final Object[] rows : col) {
            final FolderObject folder = new FolderObject();
            for (int columnPos = 0; columnPos < columns.length; columnPos++) {
                Parser.parse(rows[columnPos], columns[columnPos], folder);
            }
            folders.add(folder);
        }
        return folders.iterator();
    }

}
