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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.fields;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link QueryFields} - Determines which {@link ContactField}s are needed in database query statements from each of the database 
 * tables where contact information is stored. By adding the appropriate fields on demand, it also ensures that the 
 * <code>ContactField.NUMBER_OF_IMAGES</code> and <code>ContactField.NUMBER_OF_DISTRIBUTIONLIST</code> are queried automatically when 
 * needed.    
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <E>
 */
public class QueryFields {
    
    private ContactField[] contactDataFields;
    private ContactField[] imageDataFields;
    private DistListMemberField[] distListDataFields;
    private boolean hasImageData;
    private boolean hasContactData;
    private boolean hasDistListData;
    
    /**
     * Initializes a new {@link QueryFields}.
     * 
     * @param fields
     */
    public QueryFields(final ContactField[] fields) {
        this.update(fields);
    }
    
    public void update(final ContactField[] fields) {
        if (null == fields) {
            throw new IllegalArgumentException("fields");
        }        
        /*
         * determine image data fields
         */
        imageDataFields = filter(fields, Fields.IMAGE_DATABASE_ADDITIONAL, ContactField.OBJECT_ID).toArray(new ContactField[0]);
        hasImageData = 1 < imageDataFields.length;
        /*
         * determine distlist data fields
         */
        hasDistListData = 0 < filter(fields, EnumSet.of(ContactField.DISTRIBUTIONLIST)).size();
        distListDataFields = hasDistListData ? Fields.DISTLIST_DATABASE_ARRAY : new DistListMemberField[0];
        /*
         * build required contact data fields
         */
        if (hasDistListData) {
            if (hasImageData) {
                contactDataFields = filter(fields, Fields.CONTACT_DATABASE, ContactField.NUMBER_OF_IMAGES, 
                    ContactField.NUMBER_OF_DISTRIBUTIONLIST).toArray(new ContactField[0]);
            } else {
                contactDataFields = filter(fields, Fields.CONTACT_DATABASE, ContactField.NUMBER_OF_DISTRIBUTIONLIST).toArray(new ContactField[0]);
            }
        } else {
            if (hasImageData) {
                contactDataFields = filter(fields, Fields.CONTACT_DATABASE, ContactField.NUMBER_OF_IMAGES).toArray(new ContactField[0]);
            } else {
                contactDataFields = filter(fields, Fields.CONTACT_DATABASE).toArray(new ContactField[0]);
            }
        }
        hasContactData = 0 < contactDataFields.length;
    }
    
    public ContactField[] getContactDataFields() {
        return this.contactDataFields;
    }
    
    public ContactField[] getImageDataFields() {
        return this.imageDataFields;
    }
    
    public DistListMemberField[] getDistListDataFields() {
        return this.distListDataFields;
    }    
    
    public boolean hasImageData() {
        return this.hasImageData;
    }

    public boolean hasContactData() {
        return this.hasContactData;
    }

    public boolean hasDistListData() {
        return this.hasDistListData;
    }

    public static <E extends Enum<E>> Set<E> filter(final E[] fields, final EnumSet<E> validFields) {
        return filter(fields, validFields, (E[])null);
    }
        
    public static <E extends Enum<E>> Set<E> filter(final E[] fields, final EnumSet<E> validFields, final E... mandatoryFields) {
        final Set<E> filteredFields = new HashSet<E>();
        if (null != fields) {
            for (final E field : fields) {
                if (validFields.contains(field)) {
                    filteredFields.add(field);
                }
            }
        }
        if (null != mandatoryFields) {
            for (final E field : mandatoryFields) {
                filteredFields.add(field);
            }
        }
        return filteredFields;
    }

}
