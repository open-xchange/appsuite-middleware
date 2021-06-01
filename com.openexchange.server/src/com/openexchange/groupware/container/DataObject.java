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

package com.openexchange.groupware.container;

import java.util.Date;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link DataObject} - The root-level data object.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class DataObject extends SystemObject {

    private static final long serialVersionUID = 4792432831176202196L;

    public static final int OBJECT_ID = 1;

    public static final int CREATED_BY = 2;

    public static final int MODIFIED_BY = 3;

    public static final int CREATION_DATE = 4;

    public static final int LAST_MODIFIED = 5;

    public static final int LAST_MODIFIED_UTC = 6;

    public static final int META = 23;

    public static final int CREATED_FROM = 51;

    public static final int MODIFIED_FROM = 52;

    protected int objectId;

    protected int createdBy;

    protected int modifiedBy;

    protected Date creationDate;

    protected Date lastModified;

    /** The data object's contribution topic */
    protected String topic;

    protected EntityInfo createdFrom;

    protected EntityInfo modifiedFrom;

    protected boolean b_objectId;

    protected boolean b_createdBy;

    protected boolean b_modifiedBy;

    protected boolean b_creationDate;

    protected boolean b_lastModified;

    protected boolean b_topic;

    protected boolean b_createdFrom;

    protected boolean b_modifiedFrom;

    // GET METHODS
    public int getObjectID() {
        return objectId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Gets the topic
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    public EntityInfo getCreatedFrom() {
        return createdFrom;
    }

    public EntityInfo getModifiedFrom() {
        return modifiedFrom;
    }

    // SET METHODS
    public void setObjectID(final int object_id) {
        objectId = object_id;
        b_objectId = true;
    }

    public void setCreatedBy(final int created_by) {
        createdBy = created_by;
        b_createdBy = true;
    }

    public void setModifiedBy(final int modified_by) {
        modifiedBy = modified_by;
        b_modifiedBy = true;
    }

    public void setCreationDate(final Date creation_date) {
        creationDate = creation_date;
        b_creationDate = true;
    }

    public void setLastModified(final Date last_modified) {
        lastModified = last_modified;
        b_lastModified = true;
    }

    /**
     * Sets the topic
     *
     * @param topic The topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCreatedFrom(EntityInfo createdFrom) {
        this.createdFrom = createdFrom;
        b_createdFrom = null != createdFrom;
    }

    public void setModifiedFrom(EntityInfo modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
        b_modifiedFrom = null != modifiedFrom;
    }

    // REMOVE METHODS
    public void removeObjectID() {
        objectId = 0;
        b_objectId = false;
    }

    public void removeCreatedBy() {
        createdBy = 0;
        b_createdBy = false;
    }

    public void removeModifiedBy() {
        modifiedBy = 0;
        b_modifiedBy = false;
    }

    public void removeCreationDate() {
        creationDate = null;
        b_creationDate = false;
    }

    public void removeLastModified() {
        lastModified = null;
        b_lastModified = false;
    }

    public void removeTopic() {
        topic = null;
        b_topic = false;
    }

    public void removeCreatedFrom() {
        createdFrom = null;
        b_createdFrom = false;
    }

    public void removeModifiedFrom() {
        modifiedFrom = null;
        b_modifiedFrom = false;
    }

    // CONTAINS METHODS
    public boolean containsObjectID() {
        return b_objectId;
    }

    public boolean containsCreatedBy() {
        return b_createdBy;
    }

    public boolean containsModifiedBy() {
        return b_modifiedBy;
    }

    public boolean containsCreationDate() {
        return b_creationDate;
    }

    public boolean containsLastModified() {
        return b_lastModified;
    }

    public boolean containsTopic() {
        return b_topic;
    }

    public boolean containsCreatedFrom() {
        return b_createdFrom;
    }

    public boolean containsModifiedFrom() {
        return b_modifiedFrom;
    }

    public void reset() {
        objectId = 0;
        createdBy = 0;
        modifiedBy = 0;
        creationDate = null;
        lastModified = null;
        topic = null;
        createdFrom = null;
        modifiedFrom = null;
        b_objectId = false;
        b_createdBy = false;
        b_modifiedBy = false;
        b_creationDate = false;
        b_lastModified = false;
        b_topic = false;
        b_createdFrom = false;
        b_modifiedFrom = false;
    }

    public void set(int field, Object value) {
        switch (field) {
        case LAST_MODIFIED:
        case LAST_MODIFIED_UTC:
            setLastModified((Date) value);
            break;
        case OBJECT_ID:
            if (null == value) {
                setObjectID(0);
                break;
            }
            if (value instanceof Integer) {
                setObjectID(Integer.class.cast(value).intValue());
            } else if (value instanceof String) {
                setObjectID(Integer.parseInt(String.class.cast(value)));
            }
            break;
        case MODIFIED_BY:
            setModifiedBy(null == value ? 0 : ((Integer) value).intValue());
            break;
        case CREATION_DATE:
            setCreationDate((Date) value);
            break;
        case CREATED_BY:
            setCreatedBy(null == value ? 0 : ((Integer) value).intValue());
            break;
        case CREATED_FROM:
            setCreatedFrom(EntityInfo.class.isInstance(value) ? (EntityInfo) value : null);
            break;
        case MODIFIED_FROM:
            setModifiedFrom(EntityInfo.class.isInstance(value) ? (EntityInfo) value : null);
            break;
        default:
            throw new IllegalArgumentException("I don't know how to set " + field);
        }
    }

    public Object get(int field) {
        switch (field) {
        case LAST_MODIFIED:
        case LAST_MODIFIED_UTC:
            return getLastModified();
        case OBJECT_ID:
            return Integer.valueOf(getObjectID());
        case MODIFIED_BY:
            return Integer.valueOf(getModifiedBy());
        case CREATION_DATE:
            return getCreationDate();
        case CREATED_BY:
            return Integer.valueOf(getCreatedBy());
        case CREATED_FROM:
            return getCreatedFrom();
        case MODIFIED_FROM:
            return getModifiedFrom();
        default:
            throw new IllegalArgumentException("I don't know how to get " + field);
        }
    }

    public boolean contains(int field) {
        switch (field) {
        case LAST_MODIFIED:
        case LAST_MODIFIED_UTC:
            return containsLastModified();
        case OBJECT_ID:
            return containsObjectID();
        case MODIFIED_BY:
            return containsModifiedBy();
        case CREATION_DATE:
            return containsCreationDate();
        case CREATED_BY:
            return containsCreatedBy();
        case CREATED_FROM:
            return containsCreatedFrom();
        case MODIFIED_FROM:
            return containsModifiedFrom();
        default:
            throw new IllegalArgumentException("I don't know about field " + field);
        }
    }

    public void remove(int field) {
        switch (field) {
        case LAST_MODIFIED:
            removeLastModified();
            break;
        case OBJECT_ID:
            removeObjectID();
            break;
        case MODIFIED_BY:
            removeModifiedBy();
            break;
        case CREATION_DATE:
            removeCreationDate();
            break;
        case CREATED_BY:
            removeCreatedBy();
            break;
        case LAST_MODIFIED_UTC:
            removeLastModified();
            break;
        case CREATED_FROM:
            removeCreatedFrom();
            break;
        case MODIFIED_FROM:
            removeModifiedFrom();
            break;
        default:
            throw new IllegalArgumentException("I don't know how to remove field " + field);
        }
    }
}
