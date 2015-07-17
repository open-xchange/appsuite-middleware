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

package com.openexchange.drive.json.action.share;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveShareInfo;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link GetLinkAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkAction extends AbstractDriveShareAction {

    private static final int DEFAULT_READONLY_PERMISSION_BITS = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        try {
            JSONObject json = (JSONObject) requestData.requireData();
            DriveShareTarget target = DriveShareJSONParser.parseTarget(json, getTimeZone(requestData, session.getServerSession()));
            DriveService driveService = Services.getService(DriveService.class, true);

            List<DriveShareInfo> shares = driveService.getAllLinks(session);
            if (!shares.isEmpty()) {
                for (DriveShareInfo info : shares) {
                    if (info.getDriveShare().getTarget().equals(target) && RecipientType.ANONYMOUS.equals(info.getGuest().getRecipientType())) {
                        JSONObject jResult = new JSONObject();
                        jResult.put("url", info.getShareURL(session.getHostData()));
                        if (null != info.getDriveShare().getTarget().getExpiryDate()) {
                            jResult.put("expiry_date", info.getDriveShare().getTarget().getExpiryDate().getTime());
                        }
                        if (null != info.getGuest().getPassword()) {
                            jResult.put("password", info.getGuest().getPassword());
                        }
                        return new AJAXRequestResult(jResult, new Date(), "json");
                    }
                }
            }

            String password = json.hasAndNotNull("password") ? json.getString("password") : null;
            /*
             * prepare anonymous recipient
             */
            AnonymousRecipient recipient = new AnonymousRecipient();
            recipient.setBits(DEFAULT_READONLY_PERMISSION_BITS);
            recipient.setPassword(password);
            /*
             * create share
             */
            CreatedShares createdShares = driveService.createShare(session, Collections.<ShareRecipient> singletonList(recipient), Collections.<DriveShareTarget> singletonList(target));
            CreatedShare share = createdShares.getShare(recipient);
            /*
             * wrap share token & url into JSON result & return
             */
            JSONObject jResult = new JSONObject();
            jResult.put("url", share.getUrl(session.getHostData()));
            if (null != target.getExpiryDate()) {
                jResult.put("expiry_date", target.getExpiryDate().getTime());
            }
            return new AJAXRequestResult(jResult, new Date(), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } catch (ClassCastException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
