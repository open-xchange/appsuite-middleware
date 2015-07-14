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

package com.openexchange.share.json.actions;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.DefaultRequestContext;
import com.openexchange.share.core.performer.CreatePerformer;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetLinkAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetLinkAction extends AbstractShareAction {

    /** The default permission bits to use if not supplied by the client */
    private static final int DEFAULT_READONLY_PERMISSION_BITS = Permissions.createPermissionBits(
        Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false);

    /**
     * Initializes a new {@link GetLinkAction}.
     *
     * @param services A service lookup reference
     */
    public GetLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            JSONObject json = (JSONObject) requestData.requireData();
            ShareTarget target = ShareJSONParser.parseTarget(json.getJSONObject("target"), getTimeZone(requestData, session),
                services.getService(ModuleSupport.class));

            ShareService shareService = services.getService(ShareService.class);
            List<ShareInfo> shares = shareService.getShares(session, Module.getModuleString(target.getModule(), Integer.parseInt(target.getFolder())), target.getFolder(), target.getItem());
            if (!shares.isEmpty()) {
                for (ShareInfo info : shares) {
                    if (RecipientType.ANONYMOUS.equals(info.getGuest().getRecipientType())) {
                        JSONObject jResult = new JSONObject();
                        jResult.put("url", info.getShareURL(DefaultRequestContext.newInstance(requestData)));
                        if (null != info.getShare().getTarget().getExpiryDate()) {
                            jResult.put("expiry_date", info.getShare().getTarget().getExpiryDate().getTime());
                        }
                        jResult.put("has_password", null != info.getGuest().getPassword());
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
            CreatePerformer createPerformer = new CreatePerformer(Collections.<ShareRecipient> singletonList(recipient), Collections.<ShareTarget> singletonList(target), session, services);
            CreatedShare share = createPerformer.perform().getShare(recipient);
            /*
             * wrap share token & url into JSON result & return
             */
            JSONObject jResult = new JSONObject();
            jResult.put("url", share.getUrl(DefaultRequestContext.newInstance(requestData)));
            if (null != target.getExpiryDate()) {
                jResult.put("expiry_date", target.getExpiryDate().getTime());
            }
            jResult.put("has_password", null != share.getGuestInfo().getPassword());
            return new AJAXRequestResult(jResult, new Date(), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } catch (ClassCastException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
