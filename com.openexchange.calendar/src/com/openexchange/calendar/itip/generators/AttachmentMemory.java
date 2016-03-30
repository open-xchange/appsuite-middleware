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

package com.openexchange.calendar.itip.generators;

import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.timer.TimerService;

/**
 * The attachment memory holds information about attachment changes (for a certain timeframe)
 */
public class AttachmentMemory {

	private final ConcurrentHashMap<TimestampedAttachmentChange, TimestampedAttachmentChange> memory = new ConcurrentHashMap<AttachmentMemory.TimestampedAttachmentChange, AttachmentMemory.TimestampedAttachmentChange>();

	private int purgeInterval;

	public AttachmentMemory(int purgeInterval, TimerService timer) {
		timer.scheduleAtFixedRate(new Runnable() {

			@Override
            public void run() {
				purge();
			}

		}, purgeInterval, purgeInterval);
	}

	public void rememberAttachmentChange(int objectId, int ctxId) {
		TimestampedAttachmentChange tac = new TimestampedAttachmentChange(objectId, ctxId);
		TimestampedAttachmentChange existing = memory.putIfAbsent(tac, tac);
		if (existing != null) {
			existing.timestamp = System.currentTimeMillis();
		}
	}

	public void forgetAttachmentChange(int objectId, int ctxId) {
		memory.remove(new TimestampedAttachmentChange(objectId, ctxId));
	}

	public boolean hasAttachmentChanged(int objectId, int ctxId) {
		return memory.containsKey(new TimestampedAttachmentChange(objectId, ctxId));
	}

	private void purge() {
		long now = System.currentTimeMillis();

		for (TimestampedAttachmentChange change : memory.keySet()) {
			if (now - change.timestamp > purgeInterval) {
				memory.remove(change);
			}
		}
	}

	private static class TimestampedAttachmentChange {
		public int objectId;
		public int ctxId;
		public long timestamp = System.currentTimeMillis();

		public TimestampedAttachmentChange(int objectId, int ctxId) {
			super();
			this.objectId = objectId;
			this.ctxId = ctxId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ctxId;
			result = prime * result + objectId;
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
			TimestampedAttachmentChange other = (TimestampedAttachmentChange) obj;
			if (ctxId != other.ctxId) {
                return false;
            }
			if (objectId != other.objectId) {
                return false;
            }
			return true;
		}
		@Override
		public String toString() {
			return "TimestampedAttachmentChange [objectId=" + objectId
					+ ", ctxId=" + ctxId + ", timestamp=" + timestamp + "]";
		}


	}



}
