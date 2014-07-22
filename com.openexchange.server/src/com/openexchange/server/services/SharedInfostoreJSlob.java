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
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link SharedInfostoreJSlob}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SharedInfostoreJSlob implements SharedJSlobService {

    private final String serviceId;

    private final DefaultJSlob jslob;

    /**
     * Initializes a new {@link SharedInfostoreJSlob}.
     */
    public SharedInfostoreJSlob() {
        super();
        this.serviceId = "com.openexchange.jslob.config";
        this.jslob = new DefaultJSlob();
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
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            JSONObject json = new JSONObject();
            /*
             * common restrictions
             */
            json.put("maxBodySize", ServerConfig.getInt(ServerConfig.Property.MAX_BODY_SIZE));
            json.put("attachmentMaxUploadSize", AttachmentConfig.getMaxUploadSize());
            /*
             * infostore specific restrictions
             */
            if (serverSession.getUserPermissionBits().hasInfostore()) {
                json.put("infostoreMaxUploadSize", InfostoreConfig.getMaxUploadSize());
                QuotaFileStorage fs = QuotaFileStorage.getInstance(
                    FilestoreStorage.createURI(serverSession.getContext()), serverSession.getContext());
                json.put("infostoreQuota", fs.getQuota());
                json.put("infostoreUsage", fs.getUsage());
            }
            /*
             * mail specific restrictions
             */
            if (serverSession.getUserPermissionBits().hasWebMail()) {
                UserSettingMail userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(
                    session.getUserId(), serverSession.getContext());
                final MailUploadQuotaChecker mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);
                json.put("attachmentQuota", mailUploadQuotaChecker.getQuotaMax());
                json.put("attachmentQuotaPerFile", mailUploadQuotaChecker.getFileQuotaMax());
            }
            /*
             * apply jslob
             */
            jslob.setJsonObject(json);
            jslob.setId(new JSlobId(serviceId, "io.ox/core/properties", session.getUserId(), session.getContextId()));
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
