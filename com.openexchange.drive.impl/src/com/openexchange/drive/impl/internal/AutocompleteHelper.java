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
import com.openexchange.java.Streams;
import com.openexchange.share.ShareExceptionCodes;
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
        /*
         * only mode "invite" is supported for now
         */
        String mode = (String) parameters.get("mode");
        if (false == "invite".equals(mode)) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Mode \"" + mode + "\" is not supported.");
        }
        JSONArray jsonArray = new JSONArray();
        /*
         * spawn a separate "group" search task
         */
        Future<Group[]> future = DriveServiceLookup.getService(ThreadPoolService.class).getExecutor().submit(new AbstractTask<Group[]>() {

            @Override
            public Group[] call() throws Exception {
                return DriveServiceLookup.getService(GroupService.class).searchGroups(session.getServerSession(), query, false);
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
            try {
                String imageUrl = ContactUtil.generateImageUrl(session.getServerSession(), contact);
                if (null != imageUrl) {
                    jsonContact.putOpt(ContactFields.IMAGE1_URL, imageUrl);
                }
            } catch (OXException e) {
                getLogger(AutocompleteHelper.class).error("Error generating image URL for contact {}", I(contact.getObjectID()), e);
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
