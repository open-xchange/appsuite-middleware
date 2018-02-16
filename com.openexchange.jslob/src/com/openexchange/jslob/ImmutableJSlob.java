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
 *    trademarks of the OX Software GmbH. group of companies.
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
