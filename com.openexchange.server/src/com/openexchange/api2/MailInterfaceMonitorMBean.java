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



package com.openexchange.api2;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public interface MailInterfaceMonitorMBean {

	/**
	 *
	 * @return the number of active connections which were opened from
	 *         <code>Store</code>, <code>Folder</code> or
	 *         <code>Transport</code> instances
	 */
	int getNumActive();

	/**
	 * @return the average use time of most important IMAP commands
	 */
	double getAvgUseTime();

	/**
	 * @return the longest time of most important IMAP commands
	 */
	long getMaxUseTime();

	/**
	 * Resets the maximum use time.
	 */
	void resetMaxUseTime();

	/**
	 * @return the minimal use time of most important IMAP commands
	 */
	long getMinUseTime();

	/**
	 * Resets the minimum use time.
	 */
	void resetMinUseTime();

	/**
	 * @return the number of broken connections
	 */
	int getNumBrokenConnections();

	/**
	 * Resets the number of broken connections
	 *
	 */
	void resetNumBrokenConnections();

	/**
	 *
	 * @return the number of timed-out connections
	 */
	int getNumTimeoutConnections();

	/**
	 * Resets the number of timed-out connections
	 *
	 */
	void resetNumTimeoutConnections();

	/**
	 *
	 * @return the number of successfull logins
	 */
	int getNumSuccessfulLogins();

	/**
	 * Resets the number of successful logins
	 *
	 */
	void resetNumSuccessfulLogins();

	/**
	 *
	 * @return the number of failed logins
	 */
	int getNumFailedLogins();

	/**
	 * Resets the number of failed logins
	 *
	 */
	void resetNumFailedLogins();

	/**
	 * @return the occurrences of unsupported encoding exceptions as a
	 *         comma-separated string of the form:
	 *         <code>[encoding1]: [num1] times, [encoding2]: [num2] times, ...</code>
	 */
	String getUnsupportedEncodingExceptions();

}
