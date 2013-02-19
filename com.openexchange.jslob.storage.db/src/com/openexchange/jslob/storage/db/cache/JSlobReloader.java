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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.jslob.storage.db.cache;

import org.json.JSONObject;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.caching.dynamic.Refresher;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;

/**
 * {@link JSlobReloader}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobReloader extends Refresher<JSlob> implements JSlob {

    private static final long serialVersionUID = -5530962482214019318L;

    private JSlob js;

    /**
     * Initializes a new {@link JSlobReloader}.
     */
    public JSlobReloader(final OXObjectFactory<JSlob> factory, final String regionName) throws OXException {
        super(factory, regionName, true);
        js = refresh();
    }

    @Override
    public Object clone() {
        return this;
    }

    /**
     * @throws RuntimeException if refreshing fails.
     */
    private void updateDelegate() throws RuntimeException {
        try {
            js = refresh();
        } catch (final OXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        updateDelegate();
        return js.toString();
    }

    @Override
    public JSlobId getId() {
        updateDelegate();
        return js.getId();
    }

    @Override
    public JSlob setId(final JSlobId id) {
        updateDelegate();
        js.setId(id);
        return this;
    }

    @Override
    public JSONObject getJsonObject() {
        updateDelegate();
        return js.getJsonObject();
    }

    @Override
    public JSONObject getMetaObject() {
        updateDelegate();
        return js.getMetaObject();
    }

}
