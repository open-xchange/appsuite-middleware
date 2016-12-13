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

package com.openexchange.chronos.ical;

import java.io.Closeable;
import java.util.List;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.java.Streams;

/**
 * {@link EventComponent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventComponent extends DelegatingEvent implements ComponentData {

    private IFileHolder iCalHolder;
    private List<ICalProperty> properties;

    /**
     * Initializes a new {@link EventComponent}.
     */
    public EventComponent() {
        this(new Event(), null);
    }

    /**
     * Initializes a new {@link EventComponent}.
     *
     * @param delegate The underlying event delegate
     * @param iCalHolder A file holder storing the original iCal component, or <code>null</code> if not available
     */
    public EventComponent(Event delegate, ThresholdFileHolder iCalHolder) {
        super(delegate);
        this.iCalHolder = iCalHolder;
    }

    /**
     * Sets the file holder storing the original iCal component.
     *
     * @param iCalHolder A file holder storing the original iCal component
     */
    public void setComponent(IFileHolder iCalHolder) {
        this.iCalHolder = iCalHolder;
    }

    @Override
    public IFileHolder getComponent() {
        return iCalHolder;
    }

    @Override
    public void close() {
        Streams.close(iCalHolder);
        List<Alarm> alarms = getAlarms();
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                if (Closeable.class.isInstance(alarm)) {
                    Streams.close((Closeable) alarm);
                }
            }
        }
    }

    @Override
    public List<ICalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the list of further arbitrary iCalendar properties associated with the component.
     *
     * @param properties The extra properties to set
     */
    public void setProperties(List<ICalProperty> properties) {
        this.properties = properties;
    }

}
