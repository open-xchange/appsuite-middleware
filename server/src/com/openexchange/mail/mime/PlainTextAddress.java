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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.mime;

import javax.mail.internet.InternetAddress;

import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link PlainTextAddress} - A plain text internet address without a personal
 * part.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class PlainTextAddress extends InternetAddress {

	/**
	 * Creates a newly allocated array of {@link PlainTextAddress} generated
	 * from specified addresses.
	 * 
	 * @param addresses
	 *            The source addresses as an array of {@link String}
	 * @return A newly allocated array of {@link PlainTextAddress}
	 */
	public static PlainTextAddress[] getAddresses(final String[] addresses) {
		if ((addresses == null) || (addresses.length == 0)) {
			return new PlainTextAddress[0];
		}
		final PlainTextAddress[] retval = new PlainTextAddress[addresses.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = new PlainTextAddress(addresses[i]);
		}
		return retval;
	}

	private static final long serialVersionUID = -3276144799717449603L;

	private static final String TYPE = "rfc822";

	private final String address;

	private final int hashCode;

	public PlainTextAddress(final String address) {
		this.address = MessageUtility.decodeMultiEncodedHeader(address);
		hashCode = address.toLowerCase().hashCode();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return address;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public String getPersonal() {
		return null;
	}

	@Override
	public boolean equals(final Object address) {
		if (address instanceof InternetAddress) {
			final InternetAddress ia = (InternetAddress) address;
			return this.address.equalsIgnoreCase(ia.getAddress());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

}
