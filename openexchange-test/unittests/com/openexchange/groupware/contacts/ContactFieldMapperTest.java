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

package com.openexchange.groupware.contacts;

import junit.framework.TestCase;

import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * Tests the translations of several mappers (currently Outlook)
 * which map names for ContactFields from Outlook and back.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactFieldMapperTest extends TestCase {

	public static void testFrenchOutlook(){
		assertEquals("Checking title in French," , ContactField.TITLE, ContactField.getByFrenchOutlookName("Titre"));
		assertEquals("Checking middle name in French," , ContactField.MIDDLE_NAME, ContactField.getByFrenchOutlookName("Deuxi\u00e8me pr\u00e9nom"));

		assertEquals("Checking field of Titre," , "Titre" , ContactField.TITLE.getFrenchOutlookName());
		assertEquals("Checking field of Deuxi\u00e8me pr\u00e9nom," , "Deuxi\u00e8me pr\u00e9nom" , ContactField.MIDDLE_NAME.getFrenchOutlookName());
	}

	public static void testGermanOutlook(){
		assertEquals("Checking title in German," , ContactField.TITLE, ContactField.getByGermanOutlookName("Anrede"));
		assertEquals("Checking middle name in German," , ContactField.MIDDLE_NAME, ContactField.getByGermanOutlookName("Weitere Vornamen"));

		assertEquals("Checking field of Anrede," , "Anrede" , ContactField.TITLE.getGermanOutlookName());
		assertEquals("Checking field of Weitere Vornamen," , "Weitere Vornamen" , ContactField.MIDDLE_NAME.getGermanOutlookName());
	}

	public static void testEnglishOutlook(){
		assertEquals("Checking title in English," , ContactField.TITLE, ContactField.getByEnglishOutlookName("Title"));
		assertEquals("Checking middle name in English," , ContactField.MIDDLE_NAME, ContactField.getByEnglishOutlookName("Middle Name"));

		assertEquals("Checking field of Title," , "Title" , ContactField.TITLE.getEnglishOutlookName());
		assertEquals("Checking field of Middle Name," , "Middle Name" , ContactField.MIDDLE_NAME.getEnglishOutlookName());
	}
}

