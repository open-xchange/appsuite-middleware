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

import org.json.JSONObject;

/**
 * {@link DefaultJSlob} - A JSlob holding a JSON object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultJSlob implements JSlob, Cloneable {

    private static final long serialVersionUID = -9125059146804828888L;

    private static final class EmptyJSlob extends DefaultJSlob {

        private static final long serialVersionUID = 870193683123103886L;

        EmptyJSlob() {
            super(null, null);
        }

        @Override
        public JSlob setJsonObject(final JSONObject jsonObject) {
            throw new UnsupportedOperationException("EmptyJSlob.setJsonObject()");
        }

        @Override
        public DefaultJSlob setId(JSlobId id) {
            throw new UnsupportedOperationException("EmptyJSlob.setId()");
        }

        @Override
        public JSlob setMetaObject(JSONObject metaObject) {
            throw new UnsupportedOperationException("EmptyJSlob.setMetaObject()");
        }
    }

    /**
     * The unmodifiable, empty {@link DefaultJSlob} instance.
     * <p>
     * Invoking {@link #setJsonObject(JSONObject)} will throw an {@link UnsupportedOperationException}.
     */
    public static final DefaultJSlob EMPTY_JSLOB = new EmptyJSlob();

    /**
     * Creates a <code>DefaultJSlob</code> instance from specified JSlob.
     *
     * @param other The JSlob to copy from
     * @return The resulting <code>DefaultJSlob</code> instance
     */
    public static DefaultJSlob copyOf(JSlob other) {
        if (null == other) {
            return null;
        }

        return other instanceof DefaultJSlob ? (DefaultJSlob) other : new DefaultJSlob(other);
    }

    private JSONObject jsonObject;
    private JSONObject metaObject;
    private JSlobId id;

    /**
     * Initializes a new {@link DefaultJSlob} with given JSON object.
     *
     * @param jsonObject The JSON object initially applied to this JSlob
     */
    public DefaultJSlob(final JSONObject jsonObject) {
        this(jsonObject, new JSONObject(4));
    }

    /**
     * Initializes a new empty {@link DefaultJSlob}.
     */
    protected DefaultJSlob(JSONObject jsonObject, JSONObject metaObject) {
        super();
        this.jsonObject = jsonObject;
        this.metaObject = metaObject;
    }

    /**
     * Initializes a new {@link DefaultJSlob}.
     *
     * @param other The other JSlob
     */
    private DefaultJSlob(final JSlob other) {
        super();
        JSONObject jo = other.getJsonObject();
        this.jsonObject = null == jo ? null : new JSONObject(jo);
        jo = other.getMetaObject();
        this.metaObject = null == jo ? null : new JSONObject(jo);
        final JSlobId otherId = other.getId();
        this.id = null == otherId ? null : new JSlobId(otherId.getServiceId(), otherId.getId(), otherId.getUser(), otherId.getContext());
    }

    @Override
    public JSlob clone() {
        try {
            final DefaultJSlob clone = (DefaultJSlob) super.clone();
            clone.setId(id == null ? null : new JSlobId(id.getServiceId(), id.getId(), id.getUser(), id.getContext()));
            clone.setJsonObject(null == jsonObject ? null : new JSONObject(jsonObject));
            clone.setMetaObject(null == metaObject ? null : new JSONObject(metaObject));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("DefaultJSlob {");
        if (getJsonObject() != null) {
            builder.append("jsonObject=").append(getJsonObject()).append(", ");
        }
        if (getMetaObject() != null) {
            builder.append("metaObject=").append(getMetaObject()).append(", ");
        }
        if (getId() != null) {
            builder.append("id=").append(getId());
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public JSlobId getId() {
        return id;
    }

    @Override
    public DefaultJSlob setId(final JSlobId id) {
        this.id = id;
        return this;
    }

    @Override
    public JSONObject getJsonObject() {
        return jsonObject;
    }

    /**
     * Sets the JSON object stored in this JSlob.
     *
     * @param jsonObject The JSON object
     * @return This JSlob with new JSON object applied
     */
    public JSlob setJsonObject(final JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    @Override
    public JSONObject getMetaObject() {
        return metaObject;
    }

    /**
     * Sets the json object with unmodifiable metadata describing the regular payload data
     *
     * @param The metadata object
     * @return This JSlob with new metadata object applied
     */
    public JSlob setMetaObject(final JSONObject metaObject) {
        this.metaObject = metaObject;
        return this;
    }

}
