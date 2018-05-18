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

import java.util.List;

/**
 * {@link DelegatingCalendar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class DelegatingCalendar extends Calendar {

    protected final Calendar delegate;

    /**
     * Initializes a new {@link DelegatingCalendar}.
     *
     * @param delegate The underlying calendar delegate
     */
    protected DelegatingCalendar(Calendar delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String getProdId() {
        return delegate.getProdId();
    }

    @Override
    public void setProdId(String prodId) {
        delegate.setProdId(prodId);
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public void setVersion(String version) {
        delegate.setVersion(version);
    }

    @Override
    public String getCalScale() {
        return delegate.getCalScale();
    }

    @Override
    public void setCalScale(String calScale) {
        delegate.setCalScale(calScale);
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public void setMethod(String method) {
        delegate.setMethod(method);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public List<Event> getEvents() {
        return delegate.getEvents();
    }

    @Override
    public void setEvents(List<Event> events) {
        delegate.setEvents(events);
    }

    @Override
    public List<FreeBusyData> getFreeBusyDatas() {
        return delegate.getFreeBusyDatas();
    }

    @Override
    public void setFreeBusyDatas(List<FreeBusyData> freeBusyDatas) {
        delegate.setFreeBusyDatas(freeBusyDatas);
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public void setExtendedProperties(ExtendedProperties value) {
        delegate.setExtendedProperties(value);
    }

}
