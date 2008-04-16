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

package com.openexchange.mail.partmodifier;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.parser.MailMessageParser;

/**
 * {@link PartModifier} - Abstract class to modify a mail part prior to parsing
 * it by {@link MailMessageParser}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class PartModifier {

	private static PartModifier instance;

	/**
	 * Performs possible modifications on specified mail part
	 * 
	 * @param mailPart
	 *            The mail part to modify
	 * @return The modified mail part
	 * @throws MailException
	 *             If a mail error occurs
	 */
	public abstract MailPart modifyPart(final MailPart mailPart) throws MailException;

	/**
	 * Initializes part modifier
	 * 
	 * @param className
	 *            The class name of part modifier implementation
	 * @throws MailException
	 *             If part modifier cannot be initialized
	 */
	public static final void init(final String className) throws MailException {
		synchronized (PartModifier.class) {
			if (null == instance) {
				try {
					instance = (PartModifier) Class.forName(className).newInstance();
				} catch (final ClassNotFoundException e) {
					throw new MailException(MailException.Code.PART_MODIFIER_CREATION_FAILED, e, className);
				} catch (final InstantiationException e) {
					throw new MailException(MailException.Code.PART_MODIFIER_CREATION_FAILED, e, className);
				} catch (final IllegalAccessException e) {
					throw new MailException(MailException.Code.PART_MODIFIER_CREATION_FAILED, e, className);
				} catch (final Throwable e) {
					throw new MailException(MailException.Code.PART_MODIFIER_CREATION_FAILED, e, className);
				}
			}
		}
	}

	/**
	 * @return The part modifier instance
	 */
	public static final PartModifier getInstance() {
		return instance;
	}
}
