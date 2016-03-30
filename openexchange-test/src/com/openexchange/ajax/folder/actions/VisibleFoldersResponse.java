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
            ret.add(Collections.<Object[]>emptyList());
        }
        // Parse public folders
        if (object.has("public")) {
            final Object[][] publicArray = parseData(object.getJSONArray("public"));
            ret.add(Collections.unmodifiableList(Arrays.asList(publicArray)));
        } else {
            ret.add(Collections.<Object[]>emptyList());
        }
        // Parse shared folders
        if (object.has("shared")) {
            final Object[][] sharedArray = parseData(object.getJSONArray("shared"));
            ret.add(Collections.unmodifiableList(Arrays.asList(sharedArray)));
        } else {
            ret.add(Collections.<Object[]>emptyList());
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
            } catch (final JSONException e) {
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
