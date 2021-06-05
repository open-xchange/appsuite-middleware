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

package com.openexchange.ajax.manifests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.manifests.actions.ConfigRequest;
import com.openexchange.ajax.manifests.actions.ConfigResponse;

/**
 * {@link Bug30835Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class Bug30835Test extends AbstractAJAXSession {

    private JSONComparator comp;

    /**
     * Initializes a new {@link Bug30835Test}.
     */
    public Bug30835Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        comp = new JSONComparator();
    }

    @Test
    public void testBug30835() throws Exception {
        ConfigRequest request = new ConfigRequest();
        ConfigResponse response = getClient().execute(request);
        JSONArray json = response.getConfig().getJSONArray("languages");
        assertFalse("Response contains no languages", json.isEmpty());
        assertTrue("Response is not ordered", isOrdered(json, comp));
    }

    private boolean isOrdered(JSONArray json, Comparator<JSONArray> comp) throws Exception {
        if (json.length() > 1) {
            for (int i = 1; i < json.length(); i++) {
                JSONArray obj0 = json.getJSONArray(i - 1);
                JSONArray obj1 = json.getJSONArray(i);
                if (comp.compare(obj0, obj1) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private class JSONComparator implements Comparator<JSONArray> {

        /**
         * Initializes a new {@link JSONComparator}.
         */
        public JSONComparator() {
            super();
        }

        @Override
        public int compare(JSONArray o1, JSONArray o2) {
            if (o1.isEmpty() && o2.isEmpty()) {
                return 0;
            }
            if (o1.length() != o2.length()) {
                return o1.length() - o2.length();
            }

            try {
                String key1 = o1.getString(0);
                String key2 = o2.getString(0);
                int comp = key1.compareToIgnoreCase(key2);
                if (comp > 0) {
                    return comp;
                }
            } catch (JSONException e) {
                return 1;
            }
            return -1;
        }

    }

}
