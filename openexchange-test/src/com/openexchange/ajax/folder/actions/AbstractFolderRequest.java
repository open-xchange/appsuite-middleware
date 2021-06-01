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

package com.openexchange.ajax.folder.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractFolderRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    private final API api;
    private AllowedModules[] allowedModules;
    private final TimeZone timeZone;
    protected boolean ignorePermissions = false;

    protected AbstractFolderRequest(final API api) {
        this(api, null);
    }

    protected AbstractFolderRequest(final API api, TimeZone timeZone) {
        super();
        this.api = api;
        this.timeZone = timeZone;
    }

    @Override
    public String getServletPath() {
        return api.getUrl();
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected JSONObject convert(final FolderObject folder) throws JSONException {
        final JSONObject jsonFolder = new JSONObject();
        if (folder.containsFolderName()) {
            jsonFolder.put(FolderFields.TITLE, folder.getFolderName());
        }
        if (folder.containsPermissions() && !ignorePermissions) {
            final JSONArray jsonPerms = new JSONArray();
            for (final OCLPermission perm : folder.getPermissions()) {
                final JSONObject jsonPermission = new JSONObject();
                if (OCLGuestPermission.class.isInstance(perm)) {
                    OCLGuestPermission guestPerm = (OCLGuestPermission) perm;
                    ShareRecipient recipient = guestPerm.getRecipient();
                    jsonPermission.put("type", recipient.getType());
                    switch (recipient.getType()) {
                        case ANONYMOUS:
                            AnonymousRecipient anonymousRecipient = (AnonymousRecipient) recipient;
                            jsonPermission.putOpt(FolderField.PASSWORD.getName(), anonymousRecipient.getPassword());
                            if (null != anonymousRecipient.getExpiryDate()) {
                                long date = anonymousRecipient.getExpiryDate().getTime();
                                if (null != timeZone) {
                                    date += timeZone.getOffset(date);
                                }
                                jsonPermission.put(FolderField.EXPIRY_DATE.getName(), date);
                            }
                            break;
                        case GUEST:
                            GuestRecipient guestRecipient = (GuestRecipient) recipient;
                            jsonPermission.putOpt(FolderField.EMAIL_ADDRESS.getName(), guestRecipient.getEmailAddress());
                            jsonPermission.putOpt(FolderField.PASSWORD.getName(), guestRecipient.getPassword());
                            jsonPermission.putOpt(FolderField.DISPLAY_NAME.getName(), guestRecipient.getDisplayName());
                            jsonPermission.putOpt(FolderField.CONTACT_FOLDER_ID.getName(), guestRecipient.getContactFolder());
                            jsonPermission.putOpt(FolderField.CONTACT_ID.getName(), guestRecipient.getContactID());
                            break;
                        default:
                            Assert.fail("Unsupported recipient: " + recipient.getType());
                            break;
                    }
                } else {
                    jsonPermission.put(FolderFields.ENTITY, perm.getEntity());
                    jsonPermission.put(FolderFields.GROUP, perm.isGroupPermission());
                }
                jsonPermission.put(FolderFields.BITS, Permissions.createPermissionBits(perm.getFolderPermission(), perm.getReadPermission(), perm.getWritePermission(), perm.getDeletePermission(), perm.isFolderAdmin()));
                jsonPerms.put(jsonPermission);
            }
            jsonFolder.put(FolderFields.PERMISSIONS, jsonPerms);
        }
        if (folder.containsModule()) {
            jsonFolder.put(FolderFields.MODULE, convertModule(folder.getModule()));
        }
        if (folder.containsType()) {
            jsonFolder.put(FolderFields.TYPE, folder.getType());
        }
        if (folder.containsParentFolderID()) {
            jsonFolder.put(FolderFields.FOLDER_ID, folder.getParentFolderID());
        }
        if (folder.containsMeta()) {
            jsonFolder.put(FolderFields.META, JSONCoercion.coerceToJSON(folder.getMeta()));
        }

        return jsonFolder;
    }

    private String convertModule(final int module) {
        final String retval;
        switch (module) {
            case FolderObject.TASK:
                retval = Folder.MODULE_TASK;
                break;
            case FolderObject.CALENDAR:
                retval = Folder.MODULE_CALENDAR;
                break;
            case FolderObject.CONTACT:
                retval = Folder.MODULE_CONTACT;
                break;
            case FolderObject.MAIL:
                retval = Folder.MODULE_MAIL;
                break;
            case FolderObject.INFOSTORE:
                retval = Folder.MODULE_INFOSTORE;
                break;
            default:
                retval = "";
        }
        return retval;
    }

    public void setAllowedModules(final AllowedModules... allowedModules) {
        this.allowedModules = allowedModules;
    }

    @Override
    public final Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        addParameters(params);
        if (api.getTreeId() != -1) {
            params.add(new Parameter("tree", api.getTreeId()));
        }
        if (null != allowedModules) {
            final String[] tmp = new String[allowedModules.length];
            for (int i = 0; i < allowedModules.length; i++) {
                tmp[i] = allowedModules[i].getJSONValue();
            }
            params.add(new Parameter("allowed_modules", Strings.join(tmp, ",")));
        }

        return params.toArray(new Parameter[params.size()]);
    }

    protected abstract void addParameters(List<Parameter> params);

    protected static String[] i2s(final int[] intArr) {
        final String[] strArr = new String[intArr.length];
        for (int i = 0; i < intArr.length; i++) {
            strArr[i] = Integer.valueOf(intArr[i]).toString();
        }
        return strArr;
    }
}
