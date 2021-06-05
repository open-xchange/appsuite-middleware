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

package com.openexchange.chronos;

import java.util.Date;
import java.util.List;

/**
 * {@link DelegatingAlarm}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class DelegatingAlarm extends Alarm {

    protected final Alarm delegate;

    /**
     * Initializes a new {@link DelegatingAlarm}.
     *
     * @param delegate The underlying alarm delegate
     */
    protected DelegatingAlarm(Alarm delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public boolean containsId() {
        return delegate.containsId();
    }

    @Override
    public String getUid() {
        return delegate.getUid();
    }

    @Override
    public boolean containsUid() {
        return delegate.containsUid();
    }

    @Override
    public RelatedTo getRelatedTo() {
        return delegate.getRelatedTo();
    }

    @Override
    public boolean containsRelatedTo() {
        return delegate.containsRelatedTo();
    }

    @Override
    public Date getAcknowledged() {
        return delegate.getAcknowledged();
    }

    @Override
    public boolean containsAcknowledged() {
        return delegate.containsAcknowledged();
    }

    @Override
    public AlarmAction getAction() {
        return delegate.getAction();
    }

    @Override
    public boolean containsAction() {
        return delegate.containsAction();
    }

    @Override
    public Repeat getRepeat() {
        return delegate.getRepeat();
    }

    @Override
    public boolean containsRepeat() {
        return delegate.containsRepeat();
    }

    @Override
    public Trigger getTrigger() {
        return delegate.getTrigger();
    }

    @Override
    public boolean containsTrigger() {
        return delegate.containsTrigger();
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public boolean containsExtendedProperties() {
        return delegate.containsExtendedProperties();
    }

    @Override
    public List<Attachment> getAttachments() {
        return delegate.getAttachments();
    }

    @Override
    public boolean containsAttachments() {
        return delegate.containsAttachments();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean containsDescription() {
        return delegate.containsDescription();
    }

    @Override
    public String getSummary() {
        return delegate.getSummary();
    }

    @Override
    public boolean containsSummary() {
        return delegate.containsSummary();
    }

    @Override
    public List<Attendee> getAttendees() {
        return delegate.getAttendees();
    }

    @Override
    public boolean containsAttendees() {
        return delegate.containsAttendees();
    }

    @Override
    public boolean equals(Object arg0) {
        return delegate.equals(arg0);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isSet(AlarmField field) {
        return delegate.isSet(field);
    }

    @Override
    public void setId(int value) {
        delegate.setId(value);
    }

    @Override
    public void removeId() {
        delegate.removeId();
    }

    @Override
    public void setUid(String value) {
        delegate.setUid(value);
    }

    @Override
    public void removeUid() {
        delegate.removeUid();
    }

    @Override
    public void setRelatedTo(RelatedTo value) {
        delegate.setRelatedTo(value);
    }

    @Override
    public void removeRelatedTo() {
        delegate.removeRelatedTo();
    }

    @Override
    public void setAcknowledged(Date value) {
        delegate.setAcknowledged(value);
    }

    @Override
    public void removeAcknowledged() {
        delegate.removeAcknowledged();
    }

    @Override
    public void setAction(AlarmAction value) {
        delegate.setAction(value);
    }

    @Override
    public void removeAction() {
        delegate.removeAction();
    }

    @Override
    public void setRepeat(Repeat value) {
        delegate.setRepeat(value);
    }

    @Override
    public void removeRepeat() {
        delegate.removeRepeat();
    }

    @Override
    public void setTrigger(Trigger value) {
        delegate.setTrigger(value);
    }

    @Override
    public void removeTrigger() {
        delegate.removeTrigger();
    }

    @Override
    public void setExtendedProperties(ExtendedProperties value) {
        delegate.setExtendedProperties(value);
    }

    @Override
    public void removeExtendedProperties() {
        delegate.removeExtendedProperties();
    }

    @Override
    public void setAttachments(List<Attachment> attachments) {
        delegate.setAttachments(attachments);
    }

    @Override
    public void removeAttachments() {
        delegate.removeAttachments();
    }

    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public void removeDescription() {
        delegate.removeDescription();
    }

    @Override
    public void setSummary(String summary) {
        delegate.setSummary(summary);
    }

    @Override
    public void removeSummary() {
        delegate.removeSummary();
    }

    @Override
    public void setAttendees(List<Attendee> attendees) {
        delegate.setAttendees(attendees);
    }

    @Override
    public void removeAttendees() {
        delegate.removeAttendees();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
