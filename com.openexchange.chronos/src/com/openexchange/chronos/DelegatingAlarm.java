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
