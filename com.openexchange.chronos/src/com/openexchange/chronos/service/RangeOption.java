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

package com.openexchange.chronos.service;

import java.util.Date;

/**
 * {@link RangeOption}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RangeOption {

    /** Empty sort options */
    public static final RangeOption EMPTY = new RangeOption();

    private Date from;
    private Date until;

    /**
     * Initializes a new {@link RangeOption}.
     */
    public RangeOption() {
        super();
    }

    /**
     * Initializes a new {@link RangeOption} base on the supplied calendar parameters.
     *
     * @param parameters The calendar parameters to extract the sort options from
     */
    public RangeOption(CalendarParameters parameters) {
        this();
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        setRange(from, until);
    }



    /**
     * Sets the from/to range based on the supplied <i>from</i> and <i>until</i> values..
     *
     * @param from The lower inclusive limit of the queried range, or <code>null</code> if not set
     * @param until The upper exclusive limit of the queried range, or <code>null</code> if not set
     * @return A self reference
     */
    public RangeOption setRange(Date from, Date until) {
        this.from = from;
        this.until = until;
        return this;
    }

    /**
     * Gets the lower inclusive limit of the queried range.
     *
     * @return The lower inclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Gets the upper exclusive limit of the queried range.
     *
     * @return The upper exclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getUntil() {
        return until;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((until == null) ? 0 : until.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RangeOption other = (RangeOption) obj;
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (until == null) {
            if (other.until != null) {
                return false;
            }
        } else if (!until.equals(other.until)) {
            return false;
        }
        return true;
    }

}
