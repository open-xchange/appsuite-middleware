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

package com.openexchange.groupware.reminder;

import java.text.DateFormat;
import java.util.Date;
import com.openexchange.tools.TimeZoneUtils;

/**
 * Data object for a reminder.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class ReminderObject implements Cloneable {

    private Date lastModified;

    private int userId;

    private Date date;

    private int objectId;

    private int targetId;

    private int module;

    private String description;

    private int folder;

    private boolean isRecurrenceAppointment;

    private int recurrencePosition;

    public ReminderObject() {
        super();
    }

    public void setUser(final int userId) {
        this.userId = userId;
    }

    public int getUser() {
        return userId;
    }

    public void setRecurrenceAppointment(final boolean isRecurrenceAppointment) {
        this.isRecurrenceAppointment = isRecurrenceAppointment;
    }

    public boolean isRecurrenceAppointment() {
        return isRecurrenceAppointment;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setTargetId(final int targetId) {
        this.targetId = targetId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setObjectId(final int objectId) {
        this.objectId = objectId;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setModule(final int module) {
        this.module = module;
    }

    public int getModule() {
        return module;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFolder(final int folder) {
        this.folder = folder;
    }

    public int getFolder() {
        return folder;
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = new Date(lastModified.getTime());
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setRecurrencePosition(final int recurrencePosition) {
        this.recurrencePosition = recurrencePosition;
    }

    public int getRecurrencePosition() {
        return recurrencePosition;
    }

    private static final DateFormat format;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Reminder: ");
        if (null == date) {
            sb.append("no date");
        } else {
            synchronized (format) {
                sb.append(format.format(getDate()));
            }
        }
        return sb.toString();
    }

    @Override
    public ReminderObject clone() {
        try {
            final ReminderObject clone = (ReminderObject) super.clone();
            clone.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
            clone.date = date == null ? null : new Date(date.getTime());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Cloneable.");
        }
    }

    static {
        format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        format.setTimeZone(TimeZoneUtils.getTimeZone("UTC"));
    }
}
