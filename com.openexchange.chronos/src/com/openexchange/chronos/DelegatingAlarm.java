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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
    public boolean equals(Object arg0) {
        return delegate.equals(arg0);
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public void setId(int id) {
        delegate.setId(id);
    }

    @Override
    public String getUid() {
        return delegate.getUid();
    }

    @Override
    public void setUid(String uid) {
        delegate.setUid(uid);
    }

    @Override
    public int getEventId() {
        return delegate.getEventId();
    }

    @Override
    public void setEventId(int eventId) {
        delegate.setEventId(eventId);
    }

    @Override
    public String getiCalId() {
        return delegate.getiCalId();
    }

    @Override
    public void setiCalId(String iCalId) {
        delegate.setiCalId(iCalId);
    }

    @Override
    public String getRelatedTo() {
        return delegate.getRelatedTo();
    }

    @Override
    public void setRelatedTo(String relatedTo) {
        delegate.setRelatedTo(relatedTo);
    }

    @Override
    public Trigger getTrigger() {
        return delegate.getTrigger();
    }

    @Override
    public void setTrigger(Trigger trigger) {
        delegate.setTrigger(trigger);
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public AlarmAction getAction() {
        return delegate.getAction();
    }

    @Override
    public void setAction(AlarmAction action) {
        delegate.setAction(action);
    }

    @Override
    public String getDuration() {
        return delegate.getDuration();
    }

    @Override
    public void setDuration(String duration) {
        delegate.setDuration(duration);
    }

    @Override
    public int getRepeat() {
        return delegate.getRepeat();
    }

    @Override
    public Date getAcknowledged() {
        return delegate.getAcknowledged();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void setRepeat(int repeat) {
        delegate.setRepeat(repeat);
    }

    @Override
    public void setAcknowledged(Date acknowledged) {
        delegate.setAcknowledged(acknowledged);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
