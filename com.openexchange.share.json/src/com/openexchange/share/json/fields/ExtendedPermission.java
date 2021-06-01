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

package com.openexchange.share.json.fields;

import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.groupware.container.Contact;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.SubfolderAwareShareInfo;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link ExtendedPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ExtendedPermission {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedPermission.class);

    protected final PermissionResolver resolver;

    /**
     * Initializes a new {@link ExtendedPermission}.
     *
     * @param permissionResolver The permission resolver
     */
    protected ExtendedPermission(PermissionResolver permissionResolver) {
        super();
        this.resolver = permissionResolver;
    }

    protected void addGroupInfo(AJAXRequestData requestData, JSONObject jsonObject, Group group) throws JSONException, OXException {
        if (null != group) {
            /*
             * serialize anonymized or full group as needed
             */
            ServerSession session = requestData.getSession();
            if (Anonymizers.isGuest(session)) {
                addGroupInfo(jsonObject, Anonymizers.optAnonymize(group, Module.GROUP, session));
            } else {
                addGroupInfo(jsonObject, group);
            }
        }
    }

    protected void addEntityInfo(AJAXRequestData requestData, JSONObject jsonObject, EntityInfo entityInfo) throws JSONException {
        if (LinkEntityInfo.class.isInstance(entityInfo)) {
            jsonObject.put("type", "anonymous");
            LinkEntityInfo linkEntityInfo = (LinkEntityInfo) entityInfo;
            jsonObject.putOpt("share_url", linkEntityInfo.getShareUrl());
            Date expiryDate = linkEntityInfo.getExpiryDate();
            if (null != expiryDate) {
                long time = null != requestData ? addTimeZoneOffset(expiryDate.getTime(), getTimeZone(requestData)) : expiryDate.getTime();
                jsonObject.put("expiry_date", time);
            }
            jsonObject.putOpt("password", linkEntityInfo.getPassword());
            if (linkEntityInfo.isIncludeSubfolders()) {
                jsonObject.put("includeSubfolders", true);
            }
        } else {
            jsonObject.putOpt("type", null != entityInfo.getType() ? String.valueOf(entityInfo.getType()).toLowerCase() : null);
            addContactInfo(jsonObject, entityInfo);
        }
    }

    protected void addUserInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException, OXException {
        if (null != user) {
            Contact userContact = resolver.getUserContact(user.getId());
            if (null != userContact) {
                addContactInfo(requestData, jsonObject, userContact);
            } else {
                addContactInfo(requestData, jsonObject, user);
            }
        }
    }

    protected void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, Contact userContact) throws JSONException, OXException {
        if (null != userContact) {
            /*
             * serialize anonymized or full contact as needed
             */
            Contact toAdd = userContact;
            ServerSession session = requestData.getSession();
            if (null == session) {
                LOGGER.debug("No session");
                return;
            }
            if (Anonymizers.isGuest(session)) {
                if (session.getUserId() != toAdd.getInternalUserId()) {
                    Set<Integer> sharingUsers = Anonymizers.getSharingUsersFor(session.getContextId(), session.getUserId());
                    if (false == sharingUsers.contains(Integer.valueOf(toAdd.getInternalUserId()))) {
                        toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                    }
                }
            } else {
                if (session.getUserId() != toAdd.getInternalUserId() && Anonymizers.isNonVisibleGuest(toAdd.getInternalUserId(), session)) {
                    toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                }
            }
            addContactInfo(jsonObject, toAdd);
        }
    }

    protected void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException, OXException {
        if (null != user) {
            /*
             * serialize anonymized or full user as needed
             */
            User toAdd = user;
            ServerSession session = requestData.getSession();
            if (null == session) {
                LOGGER.debug("No session");
                return;
            }
            if (Anonymizers.isGuest(session)) {
                if (session.getUserId() != toAdd.getId()) {
                    Set<Integer> sharingUsers = Anonymizers.getSharingUsersFor(session.getContextId(), session.getUserId());
                    if (false == sharingUsers.contains(Integer.valueOf(toAdd.getId()))) {
                        toAdd = Anonymizers.optAnonymize(toAdd, Module.USER, session);
                    }
                }
            } else {
                if (session.getUserId() != toAdd.getId() && Anonymizers.isNonVisibleGuest(toAdd.getId(), session)) {
                    toAdd = Anonymizers.optAnonymize(toAdd, Module.USER, session);
                }
            }
            addContactInfo(jsonObject, toAdd);
        }
    }

    protected void addShareInfo(AJAXRequestData requestData, JSONObject jsonObject, ShareInfo share) throws JSONException, OXException {
        if (null != share) {
            if (null != requestData) {
                jsonObject.putOpt("share_url", share.getShareURL(requestData.getHostData()));
            }
            Date expiryDate = share.getGuest().getExpiryDate();
            if (null != expiryDate) {
                long time = null != requestData ? addTimeZoneOffset(expiryDate.getTime(), getTimeZone(requestData)) : expiryDate.getTime();
                jsonObject.put("expiry_date", time);
            }
            jsonObject.putOpt("password", share.getGuest().getPassword());
            if (share.getTarget().isFolder()) {
                boolean includeSubfolders = SubfolderAwareShareInfo.class.isInstance(share) ? ((SubfolderAwareShareInfo) share).isIncludeSubfolders() : false;
                jsonObject.putOpt("includeSubfolders", Boolean.valueOf(includeSubfolders));
            }
        }
    }

    private void addContactInfo(JSONObject jsonObject, User user) throws JSONException {
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, user.getDisplayName());
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, user.getMail());
        jsonContact.putOpt(ContactFields.LAST_NAME, user.getSurname());
        jsonContact.putOpt(ContactFields.FIRST_NAME, user.getGivenName());
        jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(user.getId()));
        jsonObject.put("contact", jsonContact);
    }

    private void addContactInfo(JSONObject jsonObject, Contact contact) throws JSONException {
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, contact.getDisplayName());
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, contact.getEmail1());
        jsonContact.putOpt(ContactFields.TITLE, contact.getTitle());
        jsonContact.putOpt(ContactFields.LAST_NAME, contact.getSurName());
        jsonContact.putOpt(ContactFields.FIRST_NAME, contact.getGivenName());
        jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(contact.getInternalUserId()));
        jsonObject.put("contact", jsonContact);
    }

    private void addContactInfo(JSONObject jsonObject, EntityInfo entityInfo) throws JSONException {
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, entityInfo.getDisplayName());
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, entityInfo.getEmail1());
        jsonContact.putOpt(ContactFields.TITLE, entityInfo.getTitle());
        jsonContact.putOpt(ContactFields.LAST_NAME, entityInfo.getLastName());
        jsonContact.putOpt(ContactFields.FIRST_NAME, entityInfo.getFirstName());
        jsonContact.putOpt(ContactFields.IMAGE1_URL, entityInfo.getImageUrl());
        jsonObject.put("contact", jsonContact);
    }

    private void addGroupInfo(JSONObject jsonObject, Group group) throws JSONException {
        jsonObject.put(ContactFields.DISPLAY_NAME, group.getDisplayName());
    }

    private static long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    private static TimeZone getTimeZone(AJAXRequestData requestData) {
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
            ServerSession session = requestData.getSession();
            if (null == session) {
                LOGGER.debug("No session");
                return null;
            }
            timeZoneID = session.getUser().getTimeZone();
        }
        return TimeZoneUtils.getTimeZone(timeZoneID);
    }
}
