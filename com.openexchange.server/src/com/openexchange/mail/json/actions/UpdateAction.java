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

package com.openexchange.mail.json.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class UpdateAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateAction.class);

    /**
     * Initializes a new {@link UpdateAction}.
     *
     * @param services
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    private static final String SYSTEM_PREFIX = "\\";

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            /*
             * Read in parameters
             */
            final String sourceFolder = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final JSONObject bodyObj = (JSONObject) req.getRequest().requireData();
            final String destFolder = bodyObj.hasAndNotNull(FolderChildFields.FOLDER_ID) ? bodyObj.getString(FolderChildFields.FOLDER_ID) : null;
            final Integer colorLabel = bodyObj.hasAndNotNull(CommonFields.COLORLABEL) ? Integer.valueOf(bodyObj.getInt(CommonFields.COLORLABEL)) : null;
            final Integer flagBits = bodyObj.hasAndNotNull(MailJSONField.FLAGS.getKey()) ? Integer.valueOf(bodyObj.getInt(MailJSONField.FLAGS.getKey())) : null;
            boolean collectAddresses = bodyObj.optBoolean("collect_addresses", false);
            boolean flagVal = false;
            if (flagBits != null) {
                /*
                 * Look for boolean value
                 */
                flagVal =
                    (bodyObj.has(MailJSONField.VALUE.getKey()) && !bodyObj.isNull(MailJSONField.VALUE.getKey()) ? bodyObj.getBoolean(MailJSONField.VALUE.getKey()) : false);
            }
            Integer setFlags = bodyObj.hasAndNotNull("set_flags") ? Integer.valueOf(bodyObj.getInt("set_flags")) : null;
            Integer clearFlags = bodyObj.hasAndNotNull("clear_flags") ? Integer.valueOf(bodyObj.getInt("clear_flags")) : null;

            JSONArray setUserFlagsArray = bodyObj.hasAndNotNull("set_user_flags") ? bodyObj.getJSONArray("set_user_flags") : null;
            String[] setUserFlags = null;
            if (setUserFlagsArray != null && setUserFlagsArray.length() != 0) {
                setUserFlags = new String[setUserFlagsArray.length()];
                int x = 0;
                for (Object o : setUserFlagsArray.asList()) {
                    if (o.toString().startsWith(SYSTEM_PREFIX)) {
                        throw MailExceptionCode.INVALID_FLAG_WITH_LEADING_BACKSLASH.create(o.toString());
                    }
                    setUserFlags[x++] = o.toString();
                }
            }

            JSONArray clearUserFlagsArray = bodyObj.hasAndNotNull("clear_user_flags") ? bodyObj.getJSONArray("clear_user_flags") : null;
            String[] clearUserFlags = null;
            if (clearUserFlagsArray != null && clearUserFlagsArray.length() != 0) {
                clearUserFlags = new String[clearUserFlagsArray.length()];
                int x = 0;
                for (Object o : clearUserFlagsArray.asList()) {
                    if (o.toString().startsWith(SYSTEM_PREFIX)) {
                        throw MailExceptionCode.INVALID_FLAG_WITH_LEADING_BACKSLASH.create(o.toString());
                    }
                    clearUserFlags[x++] = o.toString();
                }
            }

            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Start response
             */
            final String uid;
            {
                String tmp = req.getParameter(AJAXServlet.PARAMETER_ID);
                if (null == tmp) {
                    tmp = req.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp) {
                        uid = null;
                    } else {
                        uid = mailInterface.getMailIDByMessageID(sourceFolder, tmp);
                    }
                } else {
                    uid = tmp;
                }
            }

            String folderId = sourceFolder;
            String mailId = uid;
            if (colorLabel != null) {
                /*
                 * Update color label
                 */
                mailInterface.updateMessageColorLabel(sourceFolder, uid == null ? null : new String[] { uid }, colorLabel.intValue());
            }
            if (flagBits != null) {
                /*
                 * Update system flags which are allowed to be altered by client
                 */
                mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, flagBits.intValue(), flagVal);
            }
            if (setUserFlags != null) {
                if (setFlags == null) {
                    // no system flags to add
                    setFlags = new Integer(0);
                }
                /*
                 * Add system and user flags
                 */
                mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, setFlags.intValue(), setUserFlags, true);
            } else if (setFlags != null) {
                /*
                 * Add system flags which are allowed to be altered by client
                 */
                mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, setFlags.intValue(), true);
            }
            if (clearUserFlags != null) {
                if (clearFlags == null) {
                    // no system flags to remove
                    clearFlags = new Integer(0);
                }
                /*
                 * Remove system and user flags
                 */
                mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, clearFlags.intValue(), clearUserFlags, false);
            } else if (clearFlags != null) {
                /*
                 * Remove system flags which are allowed to be altered by client
                 */
                mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, clearFlags.intValue(), false);
            }
            if (destFolder != null) {
                /*
                 * Perform move operation
                 */
                mailId = mailInterface.copyMessages(sourceFolder, destFolder, new String[] { uid }, true)[0];
                folderId = destFolder;
            }
            if (collectAddresses && null != uid) {
                // Trigger contact collector
                try {
                    ServerSession session = req.getSession();
                    boolean memorizeAddresses = ServerUserSetting.getInstance().isContactCollectOnMailAccess(session.getContextId(), session.getUserId()).booleanValue();
                    if (memorizeAddresses) {
                        MailMessage mail = mailInterface.getMessage(sourceFolder, mailId, false);
                        triggerContactCollector(session, mail, true, false);
                    }
                } catch (Exception e) {
                    LOG.warn("Contact collector could not be triggered.", e);
                }
            }
            return new AJAXRequestResult(new JSONObject(4).put(FolderChildFields.FOLDER_ID, folderId).put(DataFields.ID, mailId), "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
