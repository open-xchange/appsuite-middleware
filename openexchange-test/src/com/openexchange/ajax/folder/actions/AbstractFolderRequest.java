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
        if (folder.containsPermissions()) {
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
                jsonPermission.put(FolderFields.BITS, Permissions.createPermissionBits(
                    perm.getFolderPermission(),
                    perm.getReadPermission(),
                    perm.getWritePermission(),
                    perm.getDeletePermission(),
                    perm.isFolderAdmin()));
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
