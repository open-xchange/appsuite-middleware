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

package com.openexchange.ajax.writer;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.DataWriter.FieldWriter;
import com.openexchange.groupware.container.SystemObject;
import com.openexchange.session.Session;
import gnu.trove.procedure.TObjectProcedure;

/**
 * {@link WriterProcedure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class WriterProcedure<T extends SystemObject> implements TObjectProcedure<FieldWriter<T>> {

    private JSONException error;

    private final T obj;
    private final JSONObject json;
    private final TimeZone tz;
    private final Session session;

    /**
     * Initializes a new {@link WriterProcedure}.
     */
    public WriterProcedure(final T obj, final JSONObject json, final TimeZone tz, final Session session) {
        super();
        this.obj = obj;
        this.json = json;
        this.tz = tz;
        this.session = session;
    }

    @Override
    public boolean execute(final FieldWriter<T> writer) {
        try {
            writer.write(obj, tz, json, session);
            return true;
        } catch (JSONException e) {
            error = e;
            return false;
        }
    }

    /**
     * Gets the possible JSON error if {@link #execute(FieldWriter)} returned <code>false</code>.
     *
     * @return The JSON error or <code>null</code>
     */
    public JSONException getError() {
        return error;
    }

}
