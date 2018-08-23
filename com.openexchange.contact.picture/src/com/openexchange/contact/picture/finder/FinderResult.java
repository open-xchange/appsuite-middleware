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

package com.openexchange.contact.picture.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.ContactPictureRequestData.ContactPictureDataBuilder;
import com.openexchange.exception.OXException;

/**
 * {@link FinderResult}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class FinderResult {

    private final static Logger LOGGER = LoggerFactory.getLogger(FinderResult.class);

    final ContactPictureRequestData original;

    ContactPictureDataBuilder modified;

    ContactPicture picture;

    /**
     * Initializes a new {@link FinderResult}.
     * 
     * @param original The original and unmodifiable {@link ContactPictureRequestData}
     */
    public FinderResult(ContactPictureRequestData original) {
        super();
        this.original = original;
        this.modified = new ContactPictureDataBuilder() // @formatter:off
            .setContactId(original.getContactId())
            .setEmails(original.getEmails().toArray(new String[original.getEmails().size()]))
            .setETag(original.onlyETag())
            .setFolder(original.getFolderId())
            .setSession(original.getSession())
            .setUser(original.getUserId()); // @formatter:on
    }

    /**
     * Get the {@link ContactPictureDataBuilder} to modify values on
     * 
     * @return The modifiable {@link ContactPictureDataBuilder}
     */
    public ContactPictureDataBuilder modify() {
        return modified;
    }

    /**
     * Get the modified {@link ContactPictureRequestData}
     * 
     * @return The {@link ContactPictureRequestData}
     */
    public ContactPictureRequestData getModifications() {
        try {
            return modified.build();
        } catch (OXException e) {
            LOGGER.debug("Session not found in modified data. Using original data", e);
            return original;
        }
    }

    /**
     * Get the {@link ContactPicture}
     * 
     * @return The picture or <code>null</code>
     */
    public ContactPicture getContactPicture() {
        return picture;
    }

    /**
     * Set a picture to this result
     * 
     * @param picture A {@link ContactPicture}
     */
    public void setPicture(ContactPicture picture) {
        this.picture = picture;
    }

}
