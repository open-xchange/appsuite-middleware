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

package com.openexchange.drive.impl.metadata;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.ShareInfo;

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
    protected AbstractJsonMetadata(SyncSession session) throws OXException {
        super();
        this.session = session;
    }

    /**
     * Puts a user-, guest- or group entity into the supplied JSON object.
     *
     * @param jsonObject The JSON object to put the entity into
     * @param entity The entity identifier
     * @param group <code>true</code> if the entity points to a group, <code>false</code>, otherwise
     * @return The JSON object
     */
//    protected JSONObject putEntity(JSONObject jsonObject, int entity, boolean group) throws OXException, JSONException {
//        jsonObject.put("entity", entity);
//        if (group) {
//            Group resolvedGroup = session.getPermissionResolver().getGroup(entity);
//            jsonObject.put("display_name", resolvedGroup.getDisplayName());
//            jsonObject.put("type", "group");
//        } else {
//            User user = session.getPermissionResolver().getUser(entity);
//            jsonObject.put("display_name", user.getDisplayName());
//            if (false == Strings.isEmpty(user.getMail())) {
//                jsonObject.put("email_address", user.getMail());
//            }
//            if (user.isGuest()) {
//                jsonObject.put("type", ShareTool.isAnonymousGuest(user) ? "anonymous" : "guest");
//            } else {
//                jsonObject.put("type", "user");
//            }
//        }
//        return jsonObject;
//    }

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

    protected void addShareInfo(JSONObject jsonObject, ShareInfo share) throws JSONException {
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
        }
    }

}
