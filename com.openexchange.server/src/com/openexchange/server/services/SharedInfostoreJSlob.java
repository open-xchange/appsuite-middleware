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

package com.openexchange.server.services;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Quota;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
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
    private final String id;

    /**
     * Initializes a new {@link SharedInfostoreJSlob}.
     */
    public SharedInfostoreJSlob() {
        super();
        serviceId = "com.openexchange.jslob.config";
        id = "io.ox/core/properties";
    }

    @Override
    public JSlob getJSlob(Session session) throws OXException {
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            JSONObject json = new JSONObject(10);
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
                Quota storageQuota = getStorageQuota(serverSession);
                json.put("infostoreQuota", storageQuota.getLimit());
                json.put("infostoreUsage", storageQuota.getUsage());
            }
            /*
             * mail specific restrictions
             */
            if (serverSession.getUserPermissionBits().hasWebMail()) {
                UserSettingMail userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), serverSession.getContext());
                final MailUploadQuotaChecker mailUploadQuotaChecker = new MailUploadQuotaChecker(userSettingMail);
                json.put("attachmentQuota", mailUploadQuotaChecker.getQuotaMax());
                json.put("attachmentQuotaPerFile", mailUploadQuotaChecker.getFileQuotaMax());
            }
            /*
             * apply jslob
             */
            DefaultJSlob jslob = new DefaultJSlob(json);
            jslob.setId(new JSlobId(serviceId, "io.ox/core/properties", session.getUserId(), session.getContextId()));
            return jslob;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    private com.openexchange.file.storage.Quota getStorageQuota(Session session) throws OXException {
        long limit = com.openexchange.file.storage.Quota.UNLIMITED;
        long usage = com.openexchange.file.storage.Quota.UNLIMITED;

        QuotaFileStorage fileStorage = getFileStorage(session.getUserId(), session.getContextId());
        limit = fileStorage.getQuota();
        if (com.openexchange.file.storage.Quota.UNLIMITED != limit) {
            usage = fileStorage.getUsage();
        }

        return new com.openexchange.file.storage.Quota(limit, usage, com.openexchange.file.storage.Quota.Type.STORAGE);
    }

    private QuotaFileStorage getFileStorage(int userId, int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        return storageService.getQuotaFileStorage(userId, contextId, Info.drive());
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getId() {
        return id;
    }

}
