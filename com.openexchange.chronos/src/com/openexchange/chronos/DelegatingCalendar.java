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
