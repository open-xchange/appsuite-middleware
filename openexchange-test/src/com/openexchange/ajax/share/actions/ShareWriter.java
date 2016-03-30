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

package com.openexchange.ajax.share.actions;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link ShareWriter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareWriter {

    public static JSONArray writeRecipients(List<ShareRecipient> recipients) throws JSONException {
        JSONArray jRecipients = new JSONArray();
        for (ShareRecipient recipient : recipients) {
            jRecipients.put(ShareWriter.writeRecipient(recipient));
        }

        return jRecipients;
    }

    public static JSONObject writeRecipient(ShareRecipient recipient) throws JSONException {
        JSONObject jRecipient = new JSONObject(5);
        RecipientType type = recipient.getType();
        jRecipient.put("type", type.name().toLowerCase());
        jRecipient.put("bits", recipient.getBits());
        switch (type) {
            case USER:
            case GROUP:
                writeInternalRecipient((InternalRecipient) recipient, jRecipient);
                break;
            case ANONYMOUS:
                writeAnonymousRecipient((AnonymousRecipient) recipient, jRecipient);
                break;
            case GUEST:
                writeGuestRecipient((GuestRecipient) recipient, jRecipient);
                break;
        }

        return jRecipient;
    }

    public static void writeGuestRecipient(GuestRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("email_address", recipient.getEmailAddress());
        jRecipient.put("override_password", recipient.getPassword());
        jRecipient.put("display_name", recipient.getDisplayName());
        jRecipient.put("contact_id", recipient.getContactID());
        jRecipient.put("contact_folder", recipient.getContactFolder());
    }

    public static void writeAnonymousRecipient(AnonymousRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("password", recipient.getPassword());
        jRecipient.put("expiry_date", recipient.getExpiryDate() == null ? null : recipient.getExpiryDate().getTime());
    }

    public static void writeInternalRecipient(InternalRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("entity", recipient.getEntity());
    }

    public static JSONArray writeTargets(List<ShareTarget> targets) throws JSONException {
        JSONArray jTargets = new JSONArray();
        for (ShareTarget target : targets) {
            jTargets.put(ShareWriter.writeTarget(target));
        }

        return jTargets;
    }

    public static JSONObject writeTarget(ShareTarget target) throws JSONException {
        JSONObject jTarget = new JSONObject(6);
        jTarget.put("module", Module.getModuleString(target.getModule(), -1));
        jTarget.put("folder", target.getFolder());
        jTarget.put("item", target.getItem());
        return jTarget;
    }

    public static JSONObject writeDriveTarget(DriveShareTarget target) throws JSONException {
        JSONObject jTarget = new JSONObject(6);
        jTarget.put("path", target.getDrivePath());
        jTarget.putOpt("name", target.getName());
        jTarget.put("checksum", target.getChecksum());
        return jTarget;
    }

}
