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

package com.openexchange.publish.json;

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.Translator;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.interfaces.UserSpecificPublicationTarget;

/**
 * {@link PublicationTargetWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationTargetWriter {

    private static final String ID = "id";

    private static final String DISPLAY_NAME = "displayName";

    private static final String ICON = "icon";

    private static final String MODULE = "module";

    private static final String FORM_DESCRIPTION = "formDescription";

    private final Translator translator;

    public PublicationTargetWriter(Translator translator) {
        super();
        this.translator = translator;
    }

    public JSONObject write(PublicationTarget target, User user, UserPermissionBits permissionBits) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(ID, target.getId());
        object.put(DISPLAY_NAME, target.getDisplayName());
        object.put(ICON, target.getIcon());
        object.put(MODULE,target.getModule());
        object.put(FORM_DESCRIPTION, writeFormDescription(getFormDescription(target, user, permissionBits)));
        return object;
    }

    private DynamicFormDescription getFormDescription(PublicationTarget target, User user, UserPermissionBits permissionBits) {
        if(UserSpecificPublicationTarget.class.isInstance(target)) {
            UserSpecificPublicationTarget userSpecific = (UserSpecificPublicationTarget) target;
            return userSpecific.getUserSpecificDescription(user, permissionBits);
        }
        return target.getFormDescription();
    }

    private JSONArray writeFormDescription(DynamicFormDescription form) throws JSONException {
        return new FormDescriptionWriter(translator).write(form);
    }

    public JSONArray writeArray(PublicationTarget target, String[] columns, User user, UserPermissionBits permissionBits) throws JSONException, OXException {
        JSONArray array = new JSONArray();
        for (String column : columns) {
            if (column.equals(ID)) {
                array.put(target.getId());
            } else if (column.equals(DISPLAY_NAME)) {
                array.put(translator.translate(target.getDisplayName()));
            } else if (column.equals(ICON)) {
                array.put(target.getIcon());
            } else if (column.equals(MODULE)) {
                array.put(target.getModule());
            } else if (column.equals(FORM_DESCRIPTION)) {
                array.put(writeFormDescription(getFormDescription(target, user, permissionBits)));
            } else {
                throw PublicationJSONErrorMessage.UNKNOWN_COLUMN.create(column);
            }
        }
        return array;
    }

    public JSONArray writeJSONArray(Collection<PublicationTarget> targets, String[] columns, User user, UserPermissionBits permissionBits) throws JSONException, OXException {
        JSONArray array = new JSONArray();
        for (PublicationTarget publicationTarget : targets) {
            array.put(writeArray(publicationTarget, columns, user, permissionBits));
        }
        return array;
    }
}
