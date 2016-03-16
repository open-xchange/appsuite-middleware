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

package com.openexchange.data.conversion.ical;

import java.io.Serializable;
import java.util.Date;

import com.openexchange.groupware.container.Appointment;

/**
 * {@link FreeBusySlot} - Represents a free/busy-timeslot.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusySlot implements Serializable {

    private static final long serialVersionUID = -1812165912130637079L;

    private Date start;
	private Date end;
	private int shownAs;

	/**
	 * Initializes a new {@link FreeBusySlot}.
	 */
	public FreeBusySlot() {
		super();
	}

	/**
	 * Initializes a new {@link FreeBusySlot}.
	 *
	 * @param start The start date
	 * @param end The end date
	 * @param shownAs The "shown-as" flag
	 */
	public FreeBusySlot(Date start, Date end, int shownAs) {
		super();
		this.start = start;
		this.end = end;
		this.shownAs = shownAs;
	}

	public static FreeBusySlot create(Appointment appointment) {
		return new FreeBusySlot(appointment.getStartDate(), appointment.getEndDate(), appointment.getShownAs());
	}

	/**
	 * @return the start
	 */
	public Date getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public Date getEnd() {
		return end;
	}
	/**
	 * @param end the shown as to set
	 */
	public void setEnd(Date end) {
		this.end = end;
	}
	/**
	 * @return the type
	 */
	public int getShownAs() {
		return shownAs;
	}
	/**
	 * @param shownAs the shown as to set
	 */
	public void setShownAs(int shownAs) {
		this.shownAs = shownAs;
	}

}
