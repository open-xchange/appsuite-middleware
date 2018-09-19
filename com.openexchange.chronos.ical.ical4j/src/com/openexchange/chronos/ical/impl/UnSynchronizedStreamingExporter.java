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

package com.openexchange.chronos.ical.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * {@link UnSynchronizedStreamingExporter} - Unsynchronized iCal export. Timezone definitions will be written on runtime. This means the
 * timezone definitions are <b>unsorted</b> and will appear <b>between</b> the <code>VEVENT</code> definitions
 * 
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class UnSynchronizedStreamingExporter extends AbstractStreamingExporter {

    private final Set<VTimeZone> timeZones;

    /**
     * Initializes a new {@link UnSynchronizedStreamingExporter}.
     * 
     * @param iCalUtilities The {@link ICalUtilities}
     * @param parameters The {@link ICalParameters}
     */
    public UnSynchronizedStreamingExporter(ICalUtilities iCalUtilities, ICalParameters parameters) {
        super(iCalUtilities, parameters);
        this.timeZones = new HashSet<>();
    }

    @Override
    public void streamChunk(List<Event> events) throws OXException {
        Set<VTimeZone> timeZones = new HashSet<>(3);
        for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
            setTimeZones(iterator.next(), timeZones);
            writeMissingTimeZones(timeZones);
            timeZones.clear();
        }

        super.streamChunk(events);
    }

    /**
     * Adds all missing timezone definitions to the stream. Will write the timezone between different section, <b>unsorted</b>
     * 
     * @param timeZones The timezones to add
     * @throws OXException If writing fails
     */
    private void writeMissingTimeZones(Set<VTimeZone> timeZones) throws OXException {
        if (false == this.timeZones.containsAll(timeZones)) {
            for (Iterator<VTimeZone> iteratror = timeZones.iterator(); iteratror.hasNext();) {
                VTimeZone zone = iteratror.next();
                if (false == this.timeZones.contains(zone)) {
                    write(zone.toString());
                    timeZones.add(zone);
                }
            }
        }
    }

}
