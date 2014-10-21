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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.folder.json.FolderField;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Enums;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link ParsedShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParsedShare {

    private String token;
    private String shareURL;
    private AuthenticationMode authentication;
    private Date created;
    private int createdBy;
    private Date lastModified;
    private int modifiedBy;
    private List<ShareTarget> targets;
    private ShareRecipient recipient;
    private int guest;

    /**
     * Initializes a new {@link ParsedShare}.
     */
    public ParsedShare() {
        super();
    }

    /**
     * Initializes a new {@link ParsedShare}.
     *
     * @param json The JSON object to parse
     */
    public ParsedShare(JSONObject json) throws JSONException {
        super();
        token = json.optString("token");
        shareURL = json.optString("share_url");
        if (json.has("authentication")) {
            authentication = Enums.parse(AuthenticationMode.class, json.getString("authentication"));
        }
        if (json.has("created")) {
            created = new Date(json.getLong("created"));
        }
        createdBy = json.optInt("created_by");
        if (json.has("last_modified")) {
            lastModified = new Date(json.getLong("last_modified"));
        }
        modifiedBy = json.optInt("modified_by");
        if (json.has("targets")) {
            targets = new ArrayList<ShareTarget>();
            JSONArray jsonTargets = json.getJSONArray("targets");
            for (int i = 0; i < jsonTargets.length(); i++) {
                JSONObject jsonTarget = jsonTargets.getJSONObject(i);
                ShareTarget target = new ShareTarget(
                    Module.getModuleInteger(jsonTarget.optString("module")), jsonTarget.optString("folder"), jsonTarget.optString("item", null));
                if (jsonTarget.hasAndNotNull(FolderField.EXPIRY_DATE.getName())) {
                    target.setExpiryDate(new Date(jsonTarget.getLong(FolderField.EXPIRY_DATE.getName())));
                }
                if (jsonTarget.hasAndNotNull(FolderField.ACTIVATION_DATE.getName())) {
                    target.setActivationDate(new Date(jsonTarget.getLong(FolderField.ACTIVATION_DATE.getName())));
                }
                if (jsonTarget.hasAndNotNull("meta")) {
                    //TODO
                }
                targets.add(target);
            }
        }
        if (json.has("recipient")) {
            JSONObject jsonObject = json.getJSONObject("recipient");
            switch (Enums.parse(RecipientType.class, jsonObject.getString("type"))) {
            case ANONYMOUS:
                AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
                anonymousRecipient.setPassword(jsonObject.optString("password"));
                recipient = anonymousRecipient;
                break;
            case GUEST:
                GuestRecipient guestRecipient = new GuestRecipient();
                guestRecipient.setPassword(jsonObject.optString(FolderField.PASSWORD.getName(), null));
                if (false == jsonObject.hasAndNotNull(FolderField.EMAIL_ADDRESS.getName())) {
                    throw new JSONException("missing email address");
                }
                guestRecipient.setEmailAddress(jsonObject.getString(FolderField.EMAIL_ADDRESS.getName()));
                guestRecipient.setPassword(jsonObject.optString(FolderField.PASSWORD.getName(), null));
                guestRecipient.setDisplayName(jsonObject.optString(FolderField.DISPLAY_NAME.getName(), null));
                guestRecipient.setContactID(jsonObject.optString(FolderField.CONTACT_ID.getName(), null));
                guestRecipient.setContactFolder(jsonObject.optString(FolderField.CONTACT_FOLDER_ID.getName(), null));
                recipient = guestRecipient;
                break;
            default:
                Assert.fail("Unknown recipient type");
                break;
            }
            if (jsonObject.hasAndNotNull(FolderField.BITS.getName())) {
                recipient.setBits(jsonObject.getInt(FolderField.BITS.getName()));
            }
            guest = jsonObject.getInt(FolderField.ENTITY.getName());
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public AuthenticationMode getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationMode authentication) {
        this.authentication = authentication;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

    /**
     * Gets the targets
     *
     * @return The targets
     */
    public List<ShareTarget> getTargets() {
        return targets;
    }

    /**
     * Gets the recipient
     *
     * @return The recipient
     */
    public ShareRecipient getRecipient() {
        return recipient;
    }

    public int getGuest() {
        return guest;
    }

    /**
     * Sets the targets
     *
     * @param targets The targets to set
     */
    public void setTargets(List<ShareTarget> targets) {
        this.targets = targets;
    }

}
