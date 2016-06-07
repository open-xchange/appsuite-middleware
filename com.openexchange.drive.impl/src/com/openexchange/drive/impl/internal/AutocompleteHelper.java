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

package com.openexchange.drive.impl.internal;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.drive.DriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Pair;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link AutocompleteHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class AutocompleteHelper {

    public static JSONArray autocomplete(final DriveSession session, final String query, Map<String, Object> parameters) throws OXException {
        String context = (String) parameters.get("context");
        if (false == "invite".equals(context)) {
            throw new UnsupportedOperationException(context);
        }
        JSONArray jsonArray = new JSONArray();
        /*
         * spawn a separate "group" search task
         */
        Future<Group[]> future = DriveServiceLookup.getService(ThreadPoolService.class).getExecutor().submit(new AbstractTask<Group[]>() {

            @Override
            public Group[] call() throws Exception {
                return DriveServiceLookup.getService(GroupService.class).search(session.getServerSession().getContext(), query, false);
            }
        });
        /*
         * check user's capabilities for contact search
         */
        CapabilitySet capabilitySet = DriveServiceLookup.getService(CapabilityService.class).getCapabilities(session.getServerSession());
        if (capabilitySet.contains("contacts")) {
            ContactService contactService = DriveServiceLookup.getService(ContactService.class);
            AutocompleteParameters autocompleteParameters = AutocompleteParameters.newInstance();
            autocompleteParameters.put(AutocompleteParameters.IGNORE_DISTRIBUTION_LISTS, Boolean.TRUE);
            autocompleteParameters.put(AutocompleteParameters.REQUIRE_EMAIL, Boolean.TRUE);
            ContactField[] fields = { ContactField.INTERNAL_USERID, ContactField.OBJECT_ID, ContactField.FOLDER_ID,
                ContactField.NUMBER_OF_IMAGES, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,
                ContactField.TITLE, ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME, ContactField.SUR_NAME
            };
            SortOptions sortOptions = new SortOptions(ContactField.DISPLAY_NAME, Order.ASCENDING);
            sortOptions.setLimit(100);
            SearchIterator<Contact> searchIterator = null;
            try {
                if (capabilitySet.contains("invite_guests")) {
                    /*
                     * search in all visible folders is possible
                     */
                    searchIterator = contactService.autocompleteContacts(session.getServerSession(), query, autocompleteParameters, fields, sortOptions);
                } else if (capabilitySet.contains("gab")) {
                    /*
                     * search in global addressbook is possible
                     */
                    List<String> folderIDs = Collections.singletonList(String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID));
                    searchIterator = contactService.autocompleteContacts(session.getServerSession(), folderIDs, query, autocompleteParameters, fields, sortOptions);
                }
                /*
                 * convert contacts & add to results
                 */
                if (null != searchIterator) {
                    while (searchIterator.hasNext()) {
                        Contact contact = searchIterator.next();
                        try {
                            jsonArray.put(convertContact(session, contact));
                        } catch (JSONException e) {
                            getLogger(AutocompleteHelper.class).error("Error processing search result", e);
                        }
                    }
                }
            } catch (OXException e) {
                if ("CON-1000".equals(e.getErrorCode())) {
                    // TOO_FEW_SEARCH_CHARS, ignore
                } else {
                    throw e;
                }
            } finally {
                Streams.close(searchIterator);
            }
        }
        /*
         * add found groups, too
         */
        Group[] groups = null;
        try {
            groups = future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            getLogger(AutocompleteHelper.class).error("Error searching groups", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException) cause;
            }
            getLogger(AutocompleteHelper.class).error("Error searching groups", e);
        }
        if (null != groups && 0 < groups.length) {
            for (Group group : groups) {
                try {
                    jsonArray.put(convertGroup(group));
                } catch (JSONException e) {
                    getLogger(AutocompleteHelper.class).error("Error processing search result", e);
                }
            }
        }
        return jsonArray;
    }

    private static JSONObject convertGroup(Group group) throws JSONException {
        return new JSONObject()
            .put("entity", group.getIdentifier())
            .put("type", "group")
            .putOpt(ContactFields.DISPLAY_NAME, group.getDisplayName());
    }

    private static JSONObject convertContact(DriveSession session, Contact contact) throws JSONException {
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, contact.getEmail1());
        jsonContact.putOpt(ContactFields.EMAIL2, contact.getEmail2());
        jsonContact.putOpt(ContactFields.EMAIL3, contact.getEmail3());
        jsonContact.putOpt(ContactFields.TITLE, contact.getTitle());
        jsonContact.putOpt(ContactFields.LAST_NAME, contact.getSurName());
        jsonContact.putOpt(ContactFields.FIRST_NAME, contact.getGivenName());
        if (0 < contact.getNumberOfImages()) {
            Pair<ImageDataSource, ImageLocation> imageData = ContactUtil.prepareImageData(contact);
            if (null != imageData) {
                try {
                    jsonContact.putOpt(ContactFields.IMAGE1_URL, imageData.getFirst().generateUrl(imageData.getSecond(), session.getServerSession()));
                } catch (OXException e) {
                    getLogger(AutocompleteHelper.class).error("Error generating image URL for contact {}", I(contact.getObjectID()), e);
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contact", jsonContact);
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, contact.getDisplayName());
        if (0 < contact.getInternalUserId()) {
            jsonObject.put("type", "user");
            jsonObject.put("entity", contact.getInternalUserId());
        }
        return jsonObject;
    }

}
