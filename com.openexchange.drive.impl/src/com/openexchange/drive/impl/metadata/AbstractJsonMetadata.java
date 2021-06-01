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

package com.openexchange.drive.impl.metadata;

import static com.openexchange.java.Autoboxing.B;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.SubfolderAwareShareInfo;
import com.openexchange.user.User;

/**
 * {@link AbstractJsonMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractJsonMetadata  {

    protected final SyncSession session;

    /**
     * Initializes a new {@link AbstractJsonMetadata}.
     *
     * @param session The sync session
     * @throws OXException
     */
    protected AbstractJsonMetadata(SyncSession session) {
        super();
        this.session = session;
    }

    protected void addGroupInfo(JSONObject jsonObject, Group group) throws JSONException {
        if (null != group) {
            jsonObject.put(ContactFields.DISPLAY_NAME, group.getDisplayName());
        }
    }

    protected void addUserInfo(JSONObject jsonObject, User user) throws JSONException {
        if (null != user) {
            Contact userContact = session.getPermissionResolver().getUserContact(user.getId());
            if (null != userContact) {
                addContactInfo(jsonObject, userContact);
            } else {
                addContactInfo(jsonObject, user);
            }
        }
    }

    protected void addContactInfo(JSONObject jsonObject, Contact userContact) throws JSONException {
        if (null != userContact) {
            jsonObject.putOpt(ContactFields.DISPLAY_NAME, userContact.getDisplayName());
            JSONObject jsonContact = new JSONObject();
            jsonContact.putOpt(ContactFields.EMAIL1, userContact.getEmail1());
            jsonContact.putOpt(ContactFields.TITLE, userContact.getTitle());
            jsonContact.putOpt(ContactFields.LAST_NAME, userContact.getSurName());
            jsonContact.putOpt(ContactFields.FIRST_NAME, userContact.getGivenName());
            jsonContact.putOpt(ContactFields.IMAGE1_URL, session.getPermissionResolver().getImageURL(userContact.getInternalUserId()));
            jsonObject.put("contact", jsonContact);
        }
    }

    protected void addContactInfo(JSONObject jsonObject, User user) throws JSONException {
        if (null != user) {
            jsonObject.putOpt(ContactFields.DISPLAY_NAME, user.getDisplayName());
            JSONObject jsonContact = new JSONObject();
            jsonContact.putOpt(ContactFields.EMAIL1, user.getMail());
            jsonContact.putOpt(ContactFields.LAST_NAME, user.getSurname());
            jsonContact.putOpt(ContactFields.FIRST_NAME, user.getGivenName());
            jsonContact.putOpt(ContactFields.IMAGE1_URL, session.getPermissionResolver().getImageURL(user.getId()));
            jsonObject.put("contact", jsonContact);
        }
    }

    protected void addShareInfo(JSONObject jsonObject, ShareInfo share) throws JSONException, OXException {
        if (null != share) {
            HostData hostData = session.getDriveSession().getHostData();
            if (null != hostData) {
                jsonObject.putOpt("share_url", share.getShareURL(hostData));
            }
            Date expiryDate = share.getGuest().getExpiryDate();
            if (null != expiryDate) {
                jsonObject.put("expiry_date", expiryDate.getTime());
            }
            jsonObject.putOpt("password", share.getGuest().getPassword());
            if (share.getTarget().isFolder()) {
                boolean includeSubfolders = SubfolderAwareShareInfo.class.isInstance(share) ? ((SubfolderAwareShareInfo) share).isIncludeSubfolders() : false;
                jsonObject.putOpt("includeSubfolders", B(includeSubfolders));
            }
        }
    }

}
