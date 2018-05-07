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

/**
 * {@link Trigger}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.6.3">RFC 5545, section 3.8.6.3</a>
 */
public class Trigger {

    /**
     * {@link Related}
     *
     * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.2.14">RFC 5545, section 3.2.14</a>
     */
	public enum Related {

        /**
         * Sets the alarm to trigger off the start of the corresponding calendar component.
         */
		START,

        /**
         * Sets the alarm to trigger off the end of the corresponding calendar component.
         */
		END
	}

	private String duration;
	private Related related;
	private Date dateTime;

    /**
     * Initializes a new {@link Trigger}.
     */
    public Trigger() {
        super();
    }

    /**
     * Initializes a new {@link Trigger}.
     *
     * @param trigger The trigger to use for initialization
     */
    public Trigger(Trigger trigger) {
        super();
        this.duration = trigger.getDuration();
        this.related = trigger.getRelated();
        this.dateTime = trigger.getDateTime();
    }

    /**
     * Initializes a new {@link Trigger}.
     *
     * @param duration The duration to apply
     */
    public Trigger(String duration) {
        this();
        this.duration = duration;
    }

    /**
     * Initializes a new {@link Trigger}.
     *
     * @param dateTime The dateTime to apply
     */
    public Trigger(Date dateTime) {
        this();
        this.dateTime = dateTime;
    }

    /**
     * Gets the trigger's duration.
     *
     * @return The duration, or <code>null</code> if not set
     */
	public String getDuration() {
		return duration;
	}

    /**
     * Sets the trigger duration.
     *
     * @param duration The duration to set
     */
	public void setDuration(String duration) {
		this.duration = duration;
	}

    /**
     * Gets the <i>related</i> attribute indicating the relationship to the parent calendar component of the trigger.
     *
     * @return The <i>related</i> attribute, or <code>null</code> if not set
     */
	public Related getRelated() {
		return related;
	}

    /**
     * Sets the <i>related</i> attribute indicating the relationship to the parent calendar component of the trigger.
     *
     * @param related The {@link Related} attribute
     */
	public void setRelated(Related related) {
		this.related = related;
	}

    /**
     * Gets the fixed date-time of the trigger.
     *
     * @return The date-time, or <code>null</code> if no set
     */
	public Date getDateTime() {
		return dateTime;
	}

    /**
     * Sets the fixed date-time of the trigger.
     *
     * @param dateTime The {@link Date} to set
     */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

    /**
     * Matches the given object with this instance.
     * In contrast to {@link #equals(Object)} this method considers standard values for
     * specific fields
     *
     * @param obj The {@link Object} to match
     * @return <code>true</code> if the given object matches all criteria. It can be considered 'equal'.
     *         <code> false otherwise
     */
    public boolean matches(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Trigger other = (Trigger) obj;
        if (dateTime == null) {
            if (other.dateTime != null) {
                return false;
            }
        } else if (!dateTime.equals(other.dateTime)) {
            return false;
        }
        if (duration == null) {
            if (other.duration != null) {
                return false;
            }
        } else if (!duration.equals(other.duration)) {
            return false;
        }

        // 'null' ~ Related.START
        if (related == null) {
            if (null != other.related && !Related.START.equals(other.related)) {
                return false;
            }
        } else {
            if (null != other.related) {
                return related.equals(other.related);
            }
            return Related.START.equals(related);
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        result = prime * result + ((related == null) ? 0 : related.hashCode());
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
        Trigger other = (Trigger) obj;
        if (dateTime == null) {
            if (other.dateTime != null) {
                return false;
            }
        } else if (!dateTime.equals(other.dateTime)) {
            return false;
        }
        if (duration == null) {
            if (other.duration != null) {
                return false;
            }
        } else if (!duration.equals(other.duration)) {
            return false;
        }
        if (related != other.related) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Trigger [duration=" + duration + ", related=" + related + ", dateTime=" + dateTime + "]";
    }

}
