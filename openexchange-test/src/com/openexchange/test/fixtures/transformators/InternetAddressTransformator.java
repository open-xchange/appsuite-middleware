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
 *     Copyright (C) 2004-2008 Open-Xchange, Inc.
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
package com.openexchange.test.fixtures.transformators;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.test.fixtures.FixtureException;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * Transforms strings of the kind "users:user_a" or "contacts:my_contact" into an internet
 * address (to be used as email address)
 *
 * @author tfriedrich
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class InternetAddressTransformator implements Transformator {

	private FixtureLoader fixtureLoader;
	
	public InternetAddressTransformator(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	public Object transform(final String value) throws FixtureException {
		if (null == value || 1 > value.length()) { return null; }

		if (false == value.contains(":")) {
			return getAddress(value);		// try to parseICal whole string
		}
		String fixtureName = "users";
		String fixtureEntry = "";
		final String[] splitted = value.split(",");
		
		final Pattern patFlags = Pattern.compile("\\((\\w+)\\)");
		final String notificationMarker = "(notification)";
		final boolean[] notificationFlags = new boolean[splitted.length];
		
		for (int i = 0; i < splitted.length; i++) {		
	        Matcher mFlag = patFlags.matcher(splitted[i]);
	        if(mFlag.find() && mFlag.group().contains(notificationMarker)) {
	        	notificationFlags[i] = true;
	        	splitted[i] = splitted[i].replace(notificationMarker, "").replace(" ", "");
	        }
		}
        
		final List<InternetAddress> addresses = new ArrayList<InternetAddress>(splitted.length);
		for (int i = 0; i < splitted.length; i++) {
			final int idx = splitted[i].indexOf(':');
			if (0 < idx && splitted[i].length() > idx) {
				fixtureName = splitted[i].substring(0, idx);
				fixtureEntry = splitted[i].substring(idx + 1);
			} else {
				fixtureEntry = splitted[i];
			}
			addresses.addAll(getAddresses(fixtureName, fixtureEntry, notificationFlags[i]));
		}
		return addresses.toArray(new InternetAddress[addresses.size()]);
    }
	
	private InternetAddress getAddress(final String address) throws FixtureException {
		try {
			return InternetAddress.parse(address)[0];
		} catch (Exception e) {
			throw new FixtureException("Unable to parse e-mail address from " + address);
		}
	}
	
	private List<InternetAddress> getAddresses(final String fixtureName, final String fixtureEntry, final boolean notificationFlags) throws FixtureException {
		final Contact contact = ("users".equals(fixtureName)) 
			? fixtureLoader.getFixtures(fixtureName, SimpleCredentials.class).getEntry(fixtureEntry).getEntry().asContact() //users
			: fixtureLoader.getFixtures(fixtureName, Contact.class).getEntry(fixtureEntry).getEntry(); //contacts
		if (null == contact) {
			throw new FixtureException("Unable to convert " + fixtureName + ":" + fixtureEntry + " into a contact.");
		} else {
			return getAddresses(contact, notificationFlags);
		}
	}

	private List<InternetAddress> getAddresses(final Contact contact, final boolean notificationFlags) throws FixtureException {
		final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
		if (contact.containsDistributionLists()) {
			final DistributionListEntryObject[] entries = contact.getDistributionList();
			if (null != entries && 0 < entries.length) {
				for (final DistributionListEntryObject entry : entries) {
					if (null != entry && entry.containsEmailaddress()) {
						try {
							addresses.add(new InternetAddress(entry.getEmailaddress()));
						} catch (AddressException e) {
							throw new FixtureException(e);
						}
					}
				}
			}
		} else {
			try {
				if (contact.containsEmail1()) {
					if (false == notificationFlags) {
						addresses.add(new InternetAddress(contact.getEmail1(), contact.getDisplayName()));
					} else {
						addresses.add(new InternetAddress(contact.getEmail1()));
					}
					
				} else if (contact.containsEmail2()) {
					if (false == notificationFlags) {
						addresses.add(new InternetAddress(contact.getEmail2(), contact.getDisplayName()));
					} else {
						addresses.add(new InternetAddress(contact.getEmail2()));
					}
				} else if (contact.containsEmail3()) {
					if (false == notificationFlags) {
						addresses.add(new InternetAddress(contact.getEmail3(), contact.getDisplayName()));
					} else {
						addresses.add(new InternetAddress(contact.getEmail3()));
					}
				}
			} catch (UnsupportedEncodingException e) {
				throw new FixtureException(e);
			} catch (AddressException e) {
				e.printStackTrace();
			}
		}
		if (1 > addresses.size()) {
			throw new FixtureException("no e-mail addresses found in contact");
		}
		return addresses;
	}
}
