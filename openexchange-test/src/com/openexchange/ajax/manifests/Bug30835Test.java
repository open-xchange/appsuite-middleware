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

package com.openexchange.ajax.manifests;

import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONException;
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
    public Bug30835Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        comp = new JSONComparator();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBug30835() throws Exception {
        ConfigRequest request = new ConfigRequest();
        ConfigResponse response = client.execute(request);
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
