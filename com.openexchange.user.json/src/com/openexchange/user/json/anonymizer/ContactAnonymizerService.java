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

package com.openexchange.user.json.anonymizer;

import java.util.Set;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.json.osgi.Services;

/**
 * {@link ContactAnonymizerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ContactAnonymizerService implements AnonymizerService<Contact> {

    private static final ContactField[] WHITELIST_FIELDS = new ContactField[] {
        ContactField.INTERNAL_USERID, ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.UID, ContactField.CREATED_BY,
        ContactField.CREATION_DATE, ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED, ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT,
        ContactField.LAST_MODIFIED_UTC };

    /**
     * Initializes a new {@link ContactAnonymizerService}.
     */
    public ContactAnonymizerService() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.CONTACT;
    }

    @Override
    public Contact anonymize(Contact entity, Session session) throws OXException {
        if (null == entity) {
            return null;
        }

        // Do not anonymize yourself
        if (session.getUserId() == entity.getInternalUserId()) {
            return entity;
        }

        // Check if associated guest was invited by given user entity
        if (entity.getInternalUserId() > 0) {
            if (entity.getInternalUserId() == ServerSessionAdapter.valueOf(session).getUser().getCreatedBy()) {
                return entity;
            }
            ShareService shareService = Services.getService(ShareService.class);
            if (null != shareService) {
                Set<Integer> userIds = shareService.getSharingUsersFor(session.getContextId(), session.getUserId());
                if (userIds.contains(Integer.valueOf(entity.getInternalUserId()))) {
                    return entity;
                }
            }
        }

        // Otherwise anonymize the user contact
        Contact anonymizedContact = new Contact();
        ContactMapper instance = ContactMapper.getInstance();
        for (ContactField contactField : WHITELIST_FIELDS) {
            try {
                JsonMapping<? extends Object, Contact> mapping = instance.get(contactField);
                mapping.copy(entity, anonymizedContact);
            } catch (Exception e) {
                // Failed to copy value
            }
        }

        int userId = entity.getInternalUserId();
        String i18n = Anonymizers.getUserI18nFor(session);

        anonymizedContact.setDisplayName(new StringBuilder(i18n).append(' ').append(userId).toString());
        anonymizedContact.setGivenName(Integer.toString(userId));
        anonymizedContact.setSurName(i18n);

        return anonymizedContact;
    }

}
