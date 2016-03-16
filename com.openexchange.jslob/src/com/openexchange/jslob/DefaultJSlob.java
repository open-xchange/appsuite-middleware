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

        protected EmptyJSlob() {
            super();
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

    private JSONObject jsonObject;
    private JSONObject metaObject;

    private JSlobId id;

    /**
     * Initializes a new empty {@link DefaultJSlob}.
     */
    public DefaultJSlob() {
        super();
    }

    /**
     * Initializes a new {@link DefaultJSlob}.
     * 
     * @param jsonObject The JSON object initially applied to this JSlob
     */
    public DefaultJSlob(final JSONObject jsonObject) {
        super();
        this.jsonObject = jsonObject;
        this.metaObject = new JSONObject();
    }

    /**
     * Initializes a new {@link DefaultJSlob}.
     * 
     * @param other The other JSlob
     */
    public DefaultJSlob(final JSlob other) {
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
        } catch (final CloneNotSupportedException e) {
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
