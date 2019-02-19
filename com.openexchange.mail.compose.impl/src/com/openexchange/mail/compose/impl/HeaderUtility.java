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

package com.openexchange.mail.compose.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.internet.MimeUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.Type;
import com.openexchange.mail.compose.Meta.MetaType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailPasswordUtil;

/**
 * {@link HeaderUtility} - Utility class to set/read headers used for composing a mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class HeaderUtility {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HeaderUtility.class);
    }

    public static final String HEADER_X_OX_SHARED_ATTACHMENTS = MessageHeaders.HDR_X_OX_SHARED_ATTACHMENTS;
    public static final String HEADER_X_OX_SECURITY = MessageHeaders.HDR_X_OX_SECURITY;
    public static final String HEADER_X_OX_META = MessageHeaders.HDR_X_OX_META;
    public static final String HEADER_X_OX_READ_RECEIPT = MessageHeaders.HDR_X_OX_READ_RECEIPT;

    /**
     * Initializes a new {@link HeaderUtility}.
     */
    private HeaderUtility() {
        super();
    }

    private static final String HEADER_PW = "open-xchange";

    public static String encodeHeaderValue(int used, String raw) {
        if (null == raw) {
            return null;
        }

        try {
            return MimeMessageUtility.forceFold(used, MailPasswordUtil.encrypt(raw, HEADER_PW));
        } catch (GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to encode header value", x);
            return MimeMessageUtility.forceFold(used, raw);
        }
    }

    public static String decodeHeaderValue(String encoded) {
        if (null == encoded) {
            return null;
        }

        try {
            return MailPasswordUtil.decrypt(MimeUtility.unfold(encoded), HEADER_PW);
        } catch (GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to decode header value", x);
            return MimeUtility.unfold(encoded);
        }
    }

    private static final String JSON_META_NEW = new JSONObject(4).putSafe("type", Type.NEW.getId()).toString();

    public static String meta2HeaderValue(Meta meta) {
        if (null == meta || MetaType.NEW == meta.getType()) {
            return JSON_META_NEW;
        }

        JSONObject jMeta = new JSONObject(8).putSafe("type", meta.getType().getId());
        {
            Date date = meta.getDate();
            if (null != date) {
                jMeta.putSafe("date", Long.valueOf(meta.getDate().getTime()));
            }
        }
        {
            MailPath replyFor = meta.getReplyFor();
            if (null != replyFor) {
                jMeta.putSafe("replyFor", replyFor.toString());
            }
        }
        {
            MailPath editFor = meta.getEditFor();
            if (null != editFor) {
                jMeta.putSafe("editFor", editFor.toString());
            }
        }
        {
            List<MailPath> forwardsFor = meta.getForwardsFor();
            if (null != forwardsFor) {
                JSONArray jForwardsFor = new JSONArray(forwardsFor.size());
                for (MailPath forwardFor : forwardsFor) {
                    jForwardsFor.put(forwardFor.toString());
                }
                jMeta.putSafe("forwardsFor", jForwardsFor);
            }
        }

        return jMeta.toString();
    }

    public static Meta headerValue2Meta(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return Meta.META_NEW;
        }

        try {
            JSONObject jMeta = new JSONObject(headerValue);

            Meta.Builder meta = Meta.builder();
            meta.withType(MetaType.typeFor(jMeta.optString("type", Type.NEW.getId())));
            {
                long lDate = jMeta.optLong("date", -1L);
                meta.withDate(lDate < 0 ? null : new Date(lDate));
            }
            {
                String path = jMeta.optString("replyFor", null);
                meta.withReplyFor(Strings.isEmpty(path) ? null : new MailPath(path));
            }
            {
                String path = jMeta.optString("editFor", null);
                meta.withEditFor(Strings.isEmpty(path) ? null : new MailPath(path));
            }
            {
                JSONArray jPaths = jMeta.optJSONArray("forwardsFor");
                if (null != jPaths) {
                    List<MailPath> paths = new ArrayList<MailPath>(jPaths.length());
                    for (Object jPath : jPaths) {
                        paths.add(new MailPath(jPath.toString()));
                    }
                    meta.withForwardsFor(paths);
                }
            }
            return meta.build();
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to meta information: {}", headerValue, e);
            return Meta.META_NEW;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String JSON_SHARED_ATTACHMENTS_DISABLED = new JSONObject(4).putSafe("enabled", Boolean.FALSE).toString();

    public static String sharedAttachments2HeaderValue(SharedAttachmentsInfo sharedAttachmentsInfo) {
        if (null == sharedAttachmentsInfo || sharedAttachmentsInfo.isDisabled()) {
            return JSON_SHARED_ATTACHMENTS_DISABLED;
        }

        return new JSONObject(8).putSafe("enabled", Boolean.valueOf(sharedAttachmentsInfo.isEnabled())).putSafe("language", sharedAttachmentsInfo.getLanguage() == null ? JSONObject.NULL : sharedAttachmentsInfo.getLanguage().toString())
            .putSafe("autoDelete", Boolean.valueOf(sharedAttachmentsInfo.isAutoDelete())).putSafe("expiryDate", sharedAttachmentsInfo.getExpiryDate() == null ? JSONObject.NULL : Long.valueOf(sharedAttachmentsInfo.getExpiryDate().getTime()))
            .putSafe("password", sharedAttachmentsInfo.getPassword() == null ? JSONObject.NULL : sharedAttachmentsInfo.getPassword()).toString();
    }

    public static SharedAttachmentsInfo headerValue2SharedAttachments(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return SharedAttachmentsInfo.DISABLED;
        }

        try {
            JSONObject jSharedAttachments = new JSONObject(headerValue);

            SharedAttachmentsInfo.Builder sharedAttachments = SharedAttachmentsInfo.builder();
            sharedAttachments.withEnabled(jSharedAttachments.optBoolean("enabled", false));
            {
                String language = jSharedAttachments.optString("language", null);
                sharedAttachments.withLanguage(Strings.isEmpty(language) ? null : LocaleTools.getLocale(language));
            }
            sharedAttachments.withAutoDelete(jSharedAttachments.optBoolean("autoDelete", false));
            {
                long lDate = jSharedAttachments.optLong("expiryDate", -1L);
                sharedAttachments.withExpiryDate(lDate < 0 ? null : new Date(lDate));
            }
            sharedAttachments.withPassword(jSharedAttachments.optString("password", null));
            return sharedAttachments.build();
        } catch (JSONException e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to shared-attachments settings: {}", headerValue, e);
            return SharedAttachmentsInfo.DISABLED;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String JSON_SECURITY_DISABLED = new JSONObject(4).putSafe("encrypt", Boolean.FALSE).putSafe("pgpInline", Boolean.FALSE).putSafe("sign", Boolean.FALSE).toString();

    public static String security2HeaderValue(Security security) {
        if (null == security || security.isDisabled()) {
            return JSON_SECURITY_DISABLED;
        }

        return new JSONObject(4)
            .putSafe("encrypt", Boolean.valueOf(security.isEncrypt()))
            .putSafe("pgpInline", Boolean.valueOf(security.isPgpInline()))
            .putSafe("sign", Boolean.valueOf(security.isSign()))
            .putSafe("language", security.getLanguage())
            .putSafe("message", security.getMessage())
            .putSafe("pin", security.getPin())
            .toString();
    }

    public static Security headerValue2Security(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return Security.DISABLED;
        }

        try {
            JSONObject jSecurity = new JSONObject(headerValue);
            return Security.builder()
                .withEncrypt(jSecurity.optBoolean("encrypt"))
                .withPgpInline(jSecurity.optBoolean("pgpInline"))
                .withSign(jSecurity.optBoolean("sign"))
                .withLanguage(jSecurity.optString("language", null))
                .withMessage(jSecurity.optString("message", null))
                .withPin(jSecurity.optString("pin", null))
                .withMsgRef(jSecurity.optString("msgRef", null))
                .build();
        } catch (JSONException e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to security settings: {}", headerValue, e);
            return Security.DISABLED;
        }
    }

}
