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

package com.openexchange.jslob;

import org.json.ImmutableJSONObject;
import org.json.JSONObject;


/**
 * {@link ImmutableJSlob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ImmutableJSlob implements JSlob, Cloneable {

    private static final long serialVersionUID = -6376315625940796965L;

    /**
     * Creates the appropriate <code>ImmutableJSlob</code> instance for specified JSlob.
     *
     * @param jslob The source JSlob
     * @return The <code>ImmutableJSlob</code> instance
     */
    public static ImmutableJSlob valueOf(JSlob jslob) {
        if (null == jslob) {
            return null;
        }

        if (jslob instanceof ImmutableJSlob) {
            return (ImmutableJSlob) jslob;
        }
        return new ImmutableJSlob(jslob.getId(), jslob.getJsonObject(), jslob.getMetaObject());
    }

    /**
     * Creates the appropriate <code>ImmutableJSlob</code> instance for specified JSlob.
     *
     * @param jslobId The JSlob identifier to set
     * @param jslob The source JSlob
     * @return The <code>ImmutableJSlob</code> instance
     */
    public static ImmutableJSlob valueOf(JSlobId jslobId, JSlob jslob) {
        if (null == jslob) {
            return null;
        }

        if ((jslob instanceof ImmutableJSlob) && (null == jslobId || jslobId.equals(jslob.getId()))) {
            return (ImmutableJSlob) jslob;
        }
        return new ImmutableJSlob(null == jslobId ? jslob.getId() : jslobId, jslob.getJsonObject(), jslob.getMetaObject());
    }

    // -------------------------------------------------------------------------------

    private final JSlobId id;
    private final ImmutableJSONObject content;
    private final ImmutableJSONObject meta;

    /**
     * Initializes a new {@link ImmutableJSlob}.
     */
    public ImmutableJSlob(JSlobId id, JSONObject content, JSONObject meta) {
        super();
        this.id = id;
        this.content = ImmutableJSONObject.immutableFor(content);
        this.meta = ImmutableJSONObject.immutableFor(meta);
    }

    @Override
    public JSlob clone() {
        DefaultJSlob clone = new DefaultJSlob(null == content ? null : new JSONObject(content));
        clone.setId(id == null ? null : new JSlobId(id.getServiceId(), id.getId(), id.getUser(), id.getContext()));
        clone.setMetaObject(null == meta ? null : new JSONObject(meta));
        return clone;
    }

    @Override
    public JSlobId getId() {
        return id;
    }

    @Override
    public JSlob setId(JSlobId id) {
        throw new UnsupportedOperationException("Not supported by ImmutableJSlob");
    }

    @Override
    public JSONObject getJsonObject() {
        return content;
    }

    @Override
    public JSONObject getMetaObject() {
        return meta;
    }

}
