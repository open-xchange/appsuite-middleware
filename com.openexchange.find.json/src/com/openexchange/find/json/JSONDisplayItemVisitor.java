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

package com.openexchange.find.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.DefaultFolderType;
import com.openexchange.find.common.FolderDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Contact;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONDisplayItemVisitor implements DisplayItemVisitor {

    private final List<JSONException> errors;

    private final JSONObject json;

    public JSONDisplayItemVisitor() {
        super();
        json = new JSONObject();
        errors = new ArrayList<JSONException>();
    }

    @Override
    public void visit(FolderDisplayItem item) {
        UserizedFolder folder = item.getFolder();
        try {
            addDefaultValue(item);
            json.put("accountName", convertString(item.getAccountName()));
            json.put("isDefaultAccount", item.isDefaultAccount());
            json.put("folderName", convertString(folder.getLocalizedName(folder.getLocale())));
            if (item.getDefaultType() != DefaultFolderType.NONE) {
                json.put("defaultType", item.getDefaultType().getTypeName());
            }
        } catch (JSONException e) {
            errors.add(e);
        }
    }

    @Override
    public void visit(ContactDisplayItem item) {
        Contact contact = item.getContact();
        try {
            addDefaultValue(item);
            json.put("givenName", convertString(contact.getGivenName()));
            json.put("surName", convertString(contact.getSurName()));
            json.put("displayName", convertString(contact.getDisplayName()));
            json.put("email1", convertString(contact.getEmail1()));
            json.put("email2", convertString(contact.getEmail2()));
            json.put("email3", convertString(contact.getEmail3()));
        } catch (JSONException e) {
            errors.add(e);
        }
    }

    @Override
    public void visit(SimpleDisplayItem item) {
        try {
            addDefaultValue(item);
        } catch (JSONException e) {
            errors.add(e);
        }
    }

    private void addDefaultValue(DisplayItem item) throws JSONException {
        json.put("defaultValue", item.getDefaultValue());
    }

    public JSONObject getJSONObject() {
        return json;
    }

    public List<JSONException> getErrors() {
        return errors;
    }

    private Object convertString(String str) {
        return str == null ? JSONObject.NULL : str;
    }

}
