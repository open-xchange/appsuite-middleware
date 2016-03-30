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

package com.openexchange.groupware.notify;

import java.util.Locale;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PooledNotification} - Holds all necessary information about the most up-to-date object status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PooledNotification implements Delayed {

    /**
     * 2 minutes delay
     */
    private static final long MSEC_DELAY = 120000L;

    private final AtomicLong stamp;

    private final EmailableParticipant p;

    private State state;

    private Locale locale;

    private final ServerSession session;

    private final CalendarObject obj;

    private final RenderMap renderMap;

    private final int hash;

    private String title;

    /**
     * Initializes a new {@link PooledNotification} and sets its last-accessed time stamp to now.
     *
     * @param p The participant to notify
     * @param title The objects's title
     * @param state The notification state
     * @param locale The locale
     * @param renderMap The render map
     * @param objectId The object's ID
     * @param contextId The context ID
     * @param session The session
     * @param obj The calendar object
     */
    public PooledNotification(final EmailableParticipant p, final String title, final State state, final Locale locale, final RenderMap renderMap, final ServerSession session, final CalendarObject obj) {
        super();
        stamp = new AtomicLong(System.currentTimeMillis());
        this.p = p;
        this.state = state;
        this.locale = locale;
        this.renderMap = renderMap;
        this.title = title;
        this.session = session;
        this.obj = obj;
        this.hash = _hashCode();
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(MSEC_DELAY - (System.currentTimeMillis() - stamp.get()), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = this.stamp.get();
        final long otherStamp = ((PooledNotification) o).stamp.get();
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    /**
     * Touches this pooled notification; meaning its last-accessed time stamp is set to now.
     */
    public void touch() {
        stamp.set(System.currentTimeMillis());
    }

    /**
     * Merges this pooled notification with specified pooled notification.
     *
     * @param other The other pooled notification
     */
    public void merge(final PooledNotification other) {
        this.title = other.title;
        this.locale = other.locale;
        this.state = other.state;
        this.renderMap.merge(other.renderMap);
    }

    /**
     * Sets the title.
     *
     * @param title The title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Sets the locale.
     *
     * @param locale The locale
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the state.
     *
     * @param state The state
     */
    public void setState(final State state) {
        this.state = state;
    }

    /**
     * Gets the participant.
     *
     * @return the participant
     */
    public EmailableParticipant getParticipant() {
        return p;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the render map.
     *
     * @return the render map
     */
    public RenderMap getRenderMap() {
        return renderMap;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the session.
     *
     * @return the session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the calendar object.
     *
     * @return the calendar object
     */
    public CalendarObject getCalendarObject() {
        return obj;
    }

    /**
     * Gets this pooled notification's last-accessed time stamp.
     *
     * @return The last-accessed time stamp.
     */
    public long lastAccessed() {
        return stamp.get();
    }

    /**
     * Checks if the calendar object held by this pooled notification is denoted by specified object ID and context ID.
     *
     * @param objectId The calendar object's ID
     * @param contextId The calendar object's context ID
     * @return <code>true</code> if the calendar object held by this pooled notification denotes the specified calendar object; otherwise
     *         <code>false</code>
     */
    public boolean equalsByObject(final int objectId, final int contextId) {
        return obj.getObjectID() == objectId && session.getContextId() == contextId;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private int _hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + session.getContextId();
        result = prime * result + session.getUserId();
        result = prime * result + obj.getObjectID();
        result = prime * result + ((p == null) ? 0 : p.hashCode());
        return result;
    }

    /**
     * Indicates whether specified object is "equal to" this one.
     * <p>
     * Since a pooled notification is used as a key in {@link NotificationPool notification pool}, only relevant fields are considered:
     * <ul>
     * <li>The context ID</li>
     * <li>The ID of the user which triggered this notification</li>
     * <li>The ID of the calendar object</li>
     * <li>The addressable participant which shall be notified via email</li>
     * </ul>
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PooledNotification other = (PooledNotification) obj;
        if (session.getContextId() != other.session.getContextId()) {
            return false;
        }
        if (session.getUserId() != other.session.getUserId()) {
            return false;
        }
        if (this.obj.getObjectID() != other.obj.getObjectID()) {
            return false;
        }
        if (p == null) {
            if (other.p != null) {
                return false;
            }
        } else if (!p.equals(other.p)) {
            return false;
        }
        return true;
    }

}
