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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.server.services;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.session.Session;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link SharedInfostoreJSlob}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SharedInfostoreJSlob implements SharedJSlobService {

    private final String serviceId;

    private final DefaultJSlob jslob;

    private final int maxUploadSize;

    /**
     * Initializes a new {@link SharedInfostoreJSlob}.
     */
    public SharedInfostoreJSlob(int maxUploadSize) {
        super();
        serviceId = "com.openexchange.jslob.config";
        this.maxUploadSize = maxUploadSize;
        jslob = new DefaultJSlob();
        jslob.setId(new JSlobId(serviceId, "io.ox/core/properties", 0, 0));
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.jslob.shared.SharedJSlobService#getServiceId()
     */
    @Override
    public String getServiceId() {
        return serviceId;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.jslob.shared.SharedJSlobService#getJSlob()
     */
    @Override
    public JSlob getJSlob(Session session) throws OXException {
        try {
            jslob.setId(new JSlobId(serviceId, "io.ox/core/properties", session.getUserId(), session.getContextId()));
            JSONObject json = new JSONObject();
            json.put("maxUploadSize", maxUploadSize);
            Context ctx = ContextStorage.getStorageContext(session);
            QuotaFileStorage fs = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
            long quota = fs.getQuota();
            long usage = fs.getUsage();
            json.put("quota", quota);
            json.put("usage", usage);
            jslob.setJsonObject(json);
            return jslob;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.jslob.shared.SharedJSlobService#getId()
     */
    @Override
    public String getId() {
        return jslob.getId().getId();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.jslob.shared.SharedJSlobService#setJSONObject(org.json.JSONObject)
     */
    @Override
    public void setJSONObject(JSONObject jsonObject) {
        jslob.setJsonObject(jsonObject);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.jslob.shared.SharedJSlobService#setMetaObject(org.json.JSONObject)
     */
    @Override
    public void setMetaObject(JSONObject metaObject) {
        jslob.setMetaObject(metaObject);
    }

}
