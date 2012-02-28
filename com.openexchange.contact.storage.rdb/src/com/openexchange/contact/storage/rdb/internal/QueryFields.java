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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.internal;

import java.util.Arrays;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link QueryFields} - Determines which {@link ContactField}s are needed from each of the database tables where contact information 
 * is stored. Also ensures that the <code>ContactField.NUMBER_OF_IMAGES</code> and <code>ContactField.NUMBER_OF_DISTRIBUTIONLIST</code> 
 * are queried automatically when needed.    
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryFields {
    
    private final ContactField[] contactDataFields;
    private final ContactField[] imageDataFields;
    private final ContactField[] distListDataFields;
    private final boolean needsImageData;
    private final boolean needsContactData;
    private final boolean needsDistListData;
    
    /**
     * Initializes a new {@link QueryFields}.
     * @param fields
     */
    public QueryFields(final ContactField[] fields) {
        if (null == fields) {
            throw new IllegalArgumentException("fields");
        }        
        final ContactField[] imageDataFieldsAdditional = Tools.filter(fields, Tools.IMAGE_DATABASE_FIELDS_ADDITIONAL);
        needsImageData = 0 < imageDataFieldsAdditional.length;
        if (needsImageData) {
            imageDataFields = Arrays.copyOf(imageDataFieldsAdditional, imageDataFieldsAdditional.length + 1);
            imageDataFields[imageDataFieldsAdditional.length] = ContactField.OBJECT_ID;            
        } else {
            imageDataFields = new ContactField[0]; // never null
        }
        needsDistListData = 0 < Tools.filter(fields, null, ContactField.DISTRIBUTIONLIST).length;
        if (needsDistListData) {
            distListDataFields = Tools.DISTLIST_DATABASE_FIELDS_ARRAY;
            if (needsImageData) {
                contactDataFields = Tools.filter(fields, Tools.CONTACT_DATABASE_FIELDS, ContactField.NUMBER_OF_IMAGES, 
                    ContactField.NUMBER_OF_DISTRIBUTIONLIST);
            } else {
                contactDataFields = Tools.filter(fields, Tools.CONTACT_DATABASE_FIELDS, ContactField.NUMBER_OF_DISTRIBUTIONLIST);
            }
        } else {
            distListDataFields = new ContactField[0]; // never null
            if (needsImageData) {
                contactDataFields = Tools.filter(fields, Tools.CONTACT_DATABASE_FIELDS, ContactField.NUMBER_OF_IMAGES);
            } else {
                contactDataFields = Tools.filter(fields, Tools.CONTACT_DATABASE_FIELDS);
            }
        }
        needsContactData = 0 < contactDataFields.length;
    }
    
    public ContactField[] getContactDataFields() {
        return this.contactDataFields;
    }
    
    public ContactField[] getImageDataFields() {
        return this.imageDataFields;
    }
    
    public ContactField[] getDistListDataFields() {
        return this.distListDataFields;
    }    
    
    public boolean needsImageData() {
        return this.needsImageData;
    }

    public boolean needsContactData() {
        return this.needsContactData;
    }

    public boolean needsDistListData() {
        return this.needsDistListData;
    }

}
