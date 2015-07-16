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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.drive.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.drive.DriveShare;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.folder.json.FolderField;
import com.openexchange.java.Enums;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.RequestContext;
import com.openexchange.share.Share;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link DriveShareParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DriveShareParser {

    /**
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public static List<ParsedDriveShareInfo> parseDriveShareInfos(JSONArray json) throws JSONException {
        List<ParsedDriveShareInfo> driveShareInfos = new ArrayList<ParsedDriveShareInfo>();
        for (int i = 0; i < json.length(); i++) {
            ParsedDriveShareInfo driveShareInfo = parseDriveShareInfo((JSONObject) json.get(i));
            driveShareInfos.add(driveShareInfo);
        }
        return driveShareInfos;
    }

    /**
     * @param object
     * @return
     * @throws JSONException
     */
    public static ParsedDriveShareInfo parseDriveShareInfo(final JSONObject json) throws JSONException {
        ParsedDriveShareInfo driveShareInfo = new ParsedDriveShareInfo(new ShareInfo() {

            @Override
            public Share getShare() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public GuestInfo getGuest() {
                return null;
            }

            @Override
            public String getToken() {
                if (json.hasAndNotNull("token")) {
                    try {
                        return json.getString("token");
                    } catch (JSONException e) {
                        //
                    }
                }
                return null;
            }

            @Override
            public String getShareURL(RequestContext context) {
                if (json.hasAndNotNull("share_url")) {
                    try {
                        return json.getString("share_url");
                    } catch (JSONException e) {
                        //
                    }
                }
                return null;
            }

        });

        if (json.hasAndNotNull("fileVersion")) {
            driveShareInfo.setDriveShare(parseDriveShare(json.getJSONObject("fileVersion")));
        } else if (json.hasAndNotNull("directoryVersion")) {
            driveShareInfo.setDriveShare(parseDriveShare(json.getJSONObject("directoryVersion")));
        }

        if (json.has("recipient")) {
            JSONObject jsonObject = json.getJSONObject("recipient");
            ShareRecipient recipient = null;
            switch (Enums.parse(RecipientType.class, jsonObject.getString("type"))) {
                case ANONYMOUS:
                    AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
                    anonymousRecipient.setPassword(jsonObject.optString("password", null));
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

            driveShareInfo.setRecipient(recipient);
        }

        return driveShareInfo;
    }

    /**
     * @param json
     * @return
     * @throws JSONException
     */
    public static DriveShare parseDriveShare(JSONObject json) throws JSONException {
        DriveShare driveShare = new DriveShare(new Share());

        DriveShareTarget driveShareTarget = new DriveShareTarget();
        driveShareTarget.setChecksum(json.getString("checksum"));
        driveShareTarget.setPath(json.getString("path"));
        if (json.hasAndNotNull("name")) {
            driveShareTarget.setName(json.getString("name"));
        }
        driveShare.setTarget(driveShareTarget);

        return driveShare;
    }

}
