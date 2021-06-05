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

package com.openexchange.ajax.share.actions;

import static com.openexchange.java.Autoboxing.L;
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
        jRecipient.put("expiry_date", recipient.getExpiryDate() == null ? null : L(recipient.getExpiryDate().getTime()));
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
