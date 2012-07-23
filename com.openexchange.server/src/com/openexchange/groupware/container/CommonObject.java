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

package com.openexchange.groupware.container;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * CommonObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class CommonObject extends FolderChildObject implements Cloneable {

    /**
     * The available markers for a {@link CommonObject}.
     */
    public static enum Marker {
        /**
         * A common object.
         */
        COMMON,
        /**
         * A id-only object.
         */
        ID_ONLY, ;
    }

    public static final int LABEL_NONE = 0;

    public static final int LABEL_1 = 1;

    public static final int LABEL_2 = 2;

    public static final int LABEL_3 = 3;

    public static final int LABEL_4 = 4;

    public static final int LABEL_5 = 5;

    public static final int LABEL_6 = 6;

    public static final int LABEL_7 = 7;

    public static final int LABEL_8 = 8;

    public static final int LABEL_9 = 9;

    public static final int LABEL_10 = 10;

    public static final int CATEGORIES = 100;

    public static final int PRIVATE_FLAG = 101;

    public static final int COLOR_LABEL = 102;

    public static final int NUMBER_OF_LINKS = 103;

    public static final int NUMBER_OF_ATTACHMENTS = 104;

    public static final int LAST_MODIFIED_OF_NEWEST_ATTACHMENT = 105;

    public static final int FILENAME = 106;

    public static final int EXTENDED_PROPERTIES = 107;

    public static final int UID = 223;

    protected Marker marker;

    protected int personal_folder_id;

    protected int number_of_attachments;

    protected Date lastModifiedOfNewestAttachment;

    protected int number_of_links;

    protected String categories;

    protected boolean privateFlag;

    protected int label;

    protected String uid;

    protected String filename;

    protected Map<String, Object> extendedProperties;

    protected boolean b_personal_folder_id;

    protected boolean b_number_of_attachments;

    protected boolean containsLastModifiedOfNewestAttachment;

    protected boolean b_number_of_links;

    protected boolean b_categories;

    protected boolean b_private_flag;

    protected boolean bLabel;

    protected boolean b_uid;

    protected boolean b_filename;

    protected boolean b_extendedProperties;

    /**
     * Initializes a new {@link CommonObject}.
     */
    protected CommonObject() {
        super();
        marker = Marker.COMMON;
    }

    // GET METHODS

    /**
     * Gets the extended properties
     * 
     * @return The extended properties
     */
    public Map<String, Object> getExtendedProperties() {
        return extendedProperties;
    }

    public String getCategories() {
        return categories;
    }

    /**
     * Gets the marker
     * 
     * @return The marker
     */
    public Marker getMarker() {
        return marker;
    }

    public int getPersonalFolderID() {
        return personal_folder_id;
    }

    public int getNumberOfAttachments() {
        return number_of_attachments;
    }

    public Date getLastModifiedOfNewestAttachment() {
        return lastModifiedOfNewestAttachment;
    }

    public int getNumberOfLinks() {
        return number_of_links;
    }

    public boolean getPrivateFlag() {
        return privateFlag;
    }

    public int getLabel() {
        return label;
    }

    public String getUid() {
        return uid;
    }

    public String getFilename() {
        return filename;
    }

    // SET METHODS
    /**
     * Sets extended properties.
     * 
     * @param extendedProperties The extended properties to set
     */
    public void setExtendedProperties(Map<String, Object> extendedProperties) {
        if (null == extendedProperties) {
            this.extendedProperties = null;
        } else {
            this.extendedProperties = new HashMap<String, Object>(extendedProperties);
        }
        b_extendedProperties = true;
    }

    /**
     * Adds extended properties. Existing key-value-pairs are replaced.
     * 
     * @param extendedProperties The extended properties to add
     */
    public void addExtendedProperties(Map<String, Object> extendedProperties) {
        if (null != extendedProperties) {
            if (null == this.extendedProperties) {
                this.extendedProperties = new HashMap<String, Object>(extendedProperties);
            } else {
                this.extendedProperties.putAll(extendedProperties);
            }
        }
        b_extendedProperties = true;
    }

    public void setCategories(final String categories) {
        this.categories = categories;
        b_categories = true;
    }

    /**
     * Sets the marker
     * 
     * @param marker The marker to set
     */
    public void setMarker(final Marker marker) {
        this.marker = marker;
    }

    public void setPersonalFolderID(final int personal_folder_id) {
        this.personal_folder_id = personal_folder_id;
        b_personal_folder_id = true;
    }

    public void setNumberOfAttachments(final int number_of_attachments) {
        this.number_of_attachments = number_of_attachments;
        b_number_of_attachments = true;
    }

    public void setLastModifiedOfNewestAttachment(final Date lastModifiedOfNewestAttachment) {
        this.lastModifiedOfNewestAttachment = lastModifiedOfNewestAttachment;
        containsLastModifiedOfNewestAttachment = true;
    }

    public void setNumberOfLinks(final int number_of_links) {
        this.number_of_links = number_of_links;
        b_number_of_links = true;
    }

    public void setPrivateFlag(final boolean privateFlag) {
        this.privateFlag = privateFlag;
        b_private_flag = true;
    }

    public void setLabel(final int label) {
        this.label = label;
        bLabel = true;
    }

    public void setUid(String uid) {
        this.uid = uid;
        b_uid = true;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        b_filename = true;
    }

    // REMOVE METHODS
    public void removeExtendedProperties() {
        extendedProperties = null;
        b_extendedProperties = false;
    }

    public void removeCategories() {
        categories = null;
        b_categories = false;
    }

    public void removePersonalFolderID() {
        personal_folder_id = 0;
        b_personal_folder_id = false;
    }

    public void removeNumberOfAttachments() {
        number_of_attachments = 0;
        b_number_of_attachments = false;
    }

    public void removeLastModifiedOfNewestAttachment() {
        lastModifiedOfNewestAttachment = null;
        containsLastModifiedOfNewestAttachment = false;
    }

    public void removeNumberOfLinks() {
        number_of_links = 0;
        b_number_of_links = false;
    }

    public void removePrivateFlag() {
        privateFlag = false;
        b_private_flag = false;
    }

    public void removeLabel() {
        label = 0;
        bLabel = false;
    }

    public void removeUid() {
        uid = null;
        b_uid = false;
    }

    public void removeFilename() {
        filename = null;
        b_filename = false;
    }

    // CONTAINS METHODS
    public boolean containsExtendedProperties() {
        return b_extendedProperties;
    }

    public boolean containsCategories() {
        return b_categories;
    }

    public boolean containsPersonalFolderID() {
        return b_personal_folder_id;
    }

    public boolean containsNumberOfAttachments() {
        return b_number_of_attachments;
    }

    public boolean containsLastModifiedOfNewestAttachment() {
        return containsLastModifiedOfNewestAttachment;
    }

    public boolean containsNumberOfLinks() {
        return b_number_of_links;
    }

    public boolean containsPrivateFlag() {
        return b_private_flag;
    }

    public boolean containsLabel() {
        return bLabel;
    }

    public boolean containsUid() {
        return b_uid;
    }

    public boolean containsFilename() {
        return b_filename;
    }

    @Override
    public void reset() {
        super.reset();

        personal_folder_id = 0;
        number_of_attachments = 0;
        lastModifiedOfNewestAttachment = null;
        number_of_links = 0;
        categories = null;
        label = 0;
        uid = null;
        filename = null;
        extendedProperties = null;

        b_personal_folder_id = false;
        b_number_of_attachments = false;
        containsLastModifiedOfNewestAttachment = false;
        b_number_of_links = false;
        b_categories = false;
        bLabel = false;
        b_uid = false;
        b_filename = false;
        b_extendedProperties = false;
    }

    @Override
    public Set<Integer> findDifferingFields(final DataObject dataObject) {

        final Set<Integer> differingFields = super.findDifferingFields(dataObject);

        if (!getClass().isAssignableFrom(dataObject.getClass())) {
            return differingFields;
        }

        final CommonObject other = (CommonObject) dataObject;

        if ((!containsCategories() && other.containsCategories()) || (containsCategories() && other.containsCategories() && getCategories() != other.getCategories() && (getCategories() == null || !getCategories().equals(
            other.getCategories())))) {
            differingFields.add(I(CATEGORIES));
        }

        if ((!containsLabel() && other.containsLabel()) || (containsLabel() && other.containsLabel() && getLabel() != other.getLabel())) {
            differingFields.add(I(COLOR_LABEL));
        }

        if ((!containsNumberOfAttachments() && other.containsNumberOfAttachments()) || (containsNumberOfAttachments() && other.containsNumberOfAttachments() && getNumberOfAttachments() != other.getNumberOfAttachments())) {
            differingFields.add(I(NUMBER_OF_ATTACHMENTS));
        }

        if ((!containsLastModifiedOfNewestAttachment() && other.containsLastModifiedOfNewestAttachment()) || (containsLastModifiedOfNewestAttachment() && other.containsLastModifiedOfNewestAttachment() && getLastModifiedOfNewestAttachment() != other.getLastModifiedOfNewestAttachment())) {
            differingFields.add(I(LAST_MODIFIED_OF_NEWEST_ATTACHMENT));
        }

        if ((!containsNumberOfLinks() && other.containsNumberOfLinks()) || (containsNumberOfLinks() && other.containsNumberOfLinks() && getNumberOfLinks() != other.getNumberOfLinks())) {
            differingFields.add(I(NUMBER_OF_LINKS));
        }

        if ((!containsPrivateFlag() && other.containsPrivateFlag()) || (containsPrivateFlag() && other.containsPrivateFlag() && getPrivateFlag() != other.getPrivateFlag())) {
            differingFields.add(I(PRIVATE_FLAG));
        }

        if ((!containsUid() && other.containsUid()) || (containsUid() && other.containsUid() && getUid() != other.getUid())) {
            differingFields.add(I(UID));
        }

        if ((!containsFilename() && other.containsFilename()) || (containsFilename() && other.containsFilename() && getFilename() != other.getFilename())) {
            differingFields.add(I(FILENAME));
        }

        return differingFields;
    }

    @Override
    public void set(final int field, final Object value) {
        switch (field) {
        case COLOR_LABEL:
            setLabel(((Integer) value).intValue());
            break;
        case CATEGORIES:
            setCategories((String) value);
            break;
        case NUMBER_OF_LINKS:
            setNumberOfLinks(((Integer) value).intValue());
            break;
        case NUMBER_OF_ATTACHMENTS:
            setNumberOfAttachments(((Integer) value).intValue());
            break;
        case LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
            setLastModifiedOfNewestAttachment((Date) value);
            break;
        case PRIVATE_FLAG:
            setPrivateFlag(((Boolean) value).booleanValue());
            break;
        case UID:
            setUid((String) value);
            break;
        case FILENAME:
            setFilename((String) value);
            break;
        case EXTENDED_PROPERTIES:
            {
                @SuppressWarnings("unchecked")
                final Map<String, Object> properties = (Map<String, Object>) value;
                setExtendedProperties(properties);
            }
            break;
        default:
            super.set(field, value);

        }
    }

    @Override
    public Object get(final int field) {
        switch (field) {
        case COLOR_LABEL:
            return I(getLabel());
        case CATEGORIES:
            return getCategories();
        case NUMBER_OF_LINKS:
            return I(getNumberOfLinks());
        case NUMBER_OF_ATTACHMENTS:
            return I(getNumberOfAttachments());
        case LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
            return getLastModifiedOfNewestAttachment();
        case PRIVATE_FLAG:
            return B(getPrivateFlag());
        case UID:
            return getUid();
        case FILENAME:
            return getFilename();
        case EXTENDED_PROPERTIES:
            return getExtendedProperties();
        default:
            return super.get(field);

        }
    }

    @Override
    public boolean contains(final int field) {
        switch (field) {
        case COLOR_LABEL:
            return containsLabel();
        case CATEGORIES:
            return containsCategories();
        case NUMBER_OF_LINKS:
            return containsNumberOfLinks();
        case NUMBER_OF_ATTACHMENTS:
            return containsNumberOfAttachments();
        case LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
            return containsLastModifiedOfNewestAttachment;
        case PRIVATE_FLAG:
            return containsPrivateFlag();
        case UID:
            return containsUid();
        case FILENAME:
            return containsFilename();
        case EXTENDED_PROPERTIES:
            return containsExtendedProperties();
        default:
            return super.contains(field);

        }
    }

    @Override
    public void remove(final int field) {
        switch (field) {
        case COLOR_LABEL:
            removeLabel();
            break;
        case CATEGORIES:
            removeCategories();
            break;
        case NUMBER_OF_LINKS:
            removeNumberOfLinks();
            break;
        case NUMBER_OF_ATTACHMENTS:
            removeNumberOfAttachments();
            break;
        case LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
            removeLastModifiedOfNewestAttachment();
            break;
        case PRIVATE_FLAG:
            removePrivateFlag();
            break;
        case UID:
            removeUid();
            break;
        case FILENAME:
            removeFilename();
            break;
        case EXTENDED_PROPERTIES:
            removeExtendedProperties();
            break;
        default:
            super.remove(field);
        }

    }

}
