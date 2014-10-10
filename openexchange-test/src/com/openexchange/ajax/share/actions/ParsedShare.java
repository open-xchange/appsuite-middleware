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

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Enums;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link ParsedShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParsedShare {

    private String token;
    private int module;
    private String folder;
    private String item;
    private Date created;
    private int createdBy;
    private Date lastModified;
    private int modifiedBy;
    private Date expires;
    private int guest;
    private AuthenticationMode authentication;
    private String guestMailAddress;
    private String guestDisplayName;
    private String guestPassword;
    private String shareURL;

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
        module = json.optInt("module");
        folder = json.optString("folder");
        item = json.optString("item");
        if (json.has("created")) {
            created = new Date(json.getLong("created"));
        }
        createdBy = json.optInt("created_by");
        if (json.has("last_modified")) {
            lastModified = new Date(json.getLong("last_modified"));
        }
        modifiedBy = json.optInt("modified_by");
        if (json.has("expires")) {
            expires = new Date(json.getLong("expires"));
        }
        guest = json.optInt("guest");
        if (json.has("authentication")) {
            authentication = Enums.parse(AuthenticationMode.class, json.getString("authentication"));
        }
        guestMailAddress = json.optString("guest_mail_address");
        guestDisplayName = json.optString("guest_display_name");
        guestPassword = json.optString("guest_password");
        shareURL = json.optString("share_url");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
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

    public int getGuest() {
        return guest;
    }

    public void setGuest(int guest) {
        this.guest = guest;
    }

    public AuthenticationMode getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationMode authentication) {
        this.authentication = authentication;
    }

    public String getGuestMailAddress() {
        return guestMailAddress;
    }

    public void setGuestMailAddress(String guestMailAddress) {
        this.guestMailAddress = guestMailAddress;
    }

    public String getGuestDisplayName() {
        return guestDisplayName;
    }

    public void setGuestDisplayName(String guestDisplayName) {
        this.guestDisplayName = guestDisplayName;
    }

    public String getGuestPassword() {
        return guestPassword;
    }

    public void setGuestPassword(String guestPassword) {
        this.guestPassword = guestPassword;
    }

    public Date getExpires() {
        return expires;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

}
