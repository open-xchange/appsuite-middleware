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

package com.openexchange.realtime.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;


/**
 * {@link Duration} - Duration utility used within inactivity detection/notification.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class Duration {
    
    public static final Duration NONE = new Duration();
    public static final Duration TEN_SECONDS = new Duration(10, SECONDS);
    public static final Duration THIRTY_SECONDS = new Duration(30, SECONDS);
    public static final Duration ONE_MINUTE = new Duration(1, MINUTES);
    public static final Duration TWO_MINUTES = new Duration(2, MINUTES);
    public static final Duration THREE_MINUTES = new Duration(3, MINUTES);
    public static final Duration FOUR_MINUTES = new Duration(4, MINUTES);
    public static final Duration FIVE_MINUTES = new Duration(5, MINUTES);
    public static final Duration TEN_MINUTES = new Duration(10, MINUTES);
    public static final Duration FIVETEEN_MINUTES = new Duration(15, MINUTES);
    public static final Duration TWENTY_MINUTES = new Duration(20, MINUTES);
    public static final Duration TWENTYFIVE_MINUTES = new Duration(25, MINUTES);
    public static final Duration THIRTY_MINUTES = new Duration(30, MINUTES);

    private final long value;
    private final TimeUnit unit;

    private Duration() {
        value=0;
        unit=SECONDS;
    }

    public Duration(long value, TimeUnit unit) {
        Validate.isTrue(value > 0, "Duration value must be > 0.");
        Validate.notNull(unit, "Duration TimeUnit must not be null.");
        this.value = value;
        this.unit = unit;
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getTimeUnit() {
        return unit;
    }

    public long getValueInMS() {
        return MILLISECONDS.convert(value, unit);
    }

    public long getValueInS() {
        return SECONDS.convert(value, unit);
    }

    /**
     * Round down to the nearest predefined Duration.
     * 
     * Examples:
     * <ul>
     *  <li>roundDownTo(25, SECONDS) -> Duration.TEN_SECONDS</li>
     *  <li>roundDownTo(32, SECONDS) -> Duration.THIRTY_SECONDS</li>
     *  <li>roundDownTo(22, MINUTES) -> Duration.TWENTY_MINUTES</li>
     * </ul>
     *  
     * @param value The duration value 
     * @param unit The TimeUnit of the {@link Duration}
     * @return The nearest predefined Duration found by rounding down.
     */
    public static Duration roundDownTo(long value, TimeUnit unit) {
        Validate.isTrue(value >= 0, "Duration value must be >= 0.");
        Validate.notNull(unit, "Duration TimeUnit must not be null.");
        long incomingInS = unit.toSeconds(value);
        
        if(incomingInS < TEN_SECONDS.getValueInS()) {
            return NONE;
        }
        if(incomingInS >= TEN_SECONDS.getValueInS() && incomingInS < THIRTY_SECONDS.getValueInS()) {
            return TEN_SECONDS;
        }
        if(incomingInS >= THIRTY_SECONDS.getValueInS() && incomingInS < ONE_MINUTE.getValueInS()) {
            return THIRTY_SECONDS;
        }
        if(incomingInS >= ONE_MINUTE.getValueInS() && incomingInS < TWO_MINUTES.getValueInS()) {
            return ONE_MINUTE;
        }
        if(incomingInS >= TWO_MINUTES.getValueInS() && incomingInS < THREE_MINUTES.getValueInS()) {
            return TWO_MINUTES;
        }
        if(incomingInS >= THREE_MINUTES.getValueInS() && incomingInS < FOUR_MINUTES.getValueInS()) {
            return THREE_MINUTES;
        }
        if(incomingInS >= FOUR_MINUTES.getValueInS() && incomingInS < FIVE_MINUTES.getValueInS()) {
            return FOUR_MINUTES;
        }
        if(incomingInS >= FIVE_MINUTES.getValueInS() && incomingInS < TEN_MINUTES.getValueInS()) {
            return FIVE_MINUTES;
        }
        if(incomingInS >= TEN_MINUTES.getValueInS() && incomingInS < FIVETEEN_MINUTES.getValueInS()) {
            return TEN_MINUTES;
        }
        if(incomingInS >= FIVETEEN_MINUTES.getValueInS() && incomingInS < TWENTY_MINUTES.getValueInS()) {
            return FIVETEEN_MINUTES;
        }
        if(incomingInS >= TWENTY_MINUTES.getValueInS() && incomingInS < TWENTYFIVE_MINUTES.getValueInS()) {
            return TWENTY_MINUTES;
        }
        if(incomingInS >= TWENTYFIVE_MINUTES.getValueInS() && incomingInS < THIRTY_MINUTES.getValueInS()) {
            return TWENTYFIVE_MINUTES;
        }
        return THIRTY_MINUTES;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Duration))
            return false;
        Duration other = (Duration) obj;
        if (unit != other.unit)
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value + " " + unit.toString();
    }

}
