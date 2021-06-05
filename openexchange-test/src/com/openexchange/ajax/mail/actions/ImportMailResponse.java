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

package com.openexchange.ajax.mail.actions;

import java.util.Iterator;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link ImportMailResponse} - Response received by <code>/ajax/mail?action=new</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImportMailResponse extends AbstractAJAXResponse implements Iterable<String[]> {

    String[][] ids;

    ImportMailResponse(final Response response) {
        super(response);
    }

    void setIds(String[][] ids) {
        this.ids = ids;
    }

    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {

            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < ids.length;
            }

            @Override
            public String[] next() {
                return ids[pos++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String[][] getIds() {
        return ids;
    }
}
