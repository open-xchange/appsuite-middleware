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



package com.openexchange.imap;

import javax.mail.MessagingException;
import javax.mail.Part;

import com.openexchange.api2.OXException;
import com.openexchange.imap.OXMailException.MailCode;

/**
 * PartModifier
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class PartModifier {

	/**
	 * <p>
	 * On traversal of a <code>javax.mail.Message</code> instance this method
	 * is invoked everytime when a valid instance of
	 * <code>javax.mail.Part</code> is available. The part can be of any
	 * content type (e.g. <code>text/*</code>, <code>multipart/*</code>,
	 * <code>message/rfc822</code>, and so on)
	 * </p>
	 * <p>
	 * This implies that the <code>javax.mail.Part</code> instance should be
	 * modified dependent on its class (e.g. <code>javax.mail.Multipart</code>
	 * or <code>javax.mail.Message</code>), its characteristics (e.g.
	 * disposition is inline or attachment) and/or dependent on its MIME type.
	 * </p>
	 * 
	 * @see method <code>PartModifier.isInline(Part part)</code>
	 * @see method <code>javax.mail.Part.isMimeType(String mimeType)</code>
	 * @return the modified instance of <code>javax.mailPart</code>
	 */
	public abstract Part modifyPart(final Part part) throws OXException;

	/**
	 * @return the underlying implementation of <code>PartModifier</code>
	 */
	public static final PartModifier getImpl(final String className) throws OXException {
		try {
			return (PartModifier) Class.forName(className).newInstance();
		} catch (ClassNotFoundException e) {
			throw new OXMailException(MailCode.PART_MODIFIER_CREATION_FAILED, e, className);
		} catch (InstantiationException e) {
			throw new OXMailException(MailCode.PART_MODIFIER_CREATION_FAILED, e, className);
		} catch (IllegalAccessException e) {
			throw new OXMailException(MailCode.PART_MODIFIER_CREATION_FAILED, e, className);
		} catch (Throwable e) {
			throw new OXMailException(MailCode.PART_MODIFIER_CREATION_FAILED, e, className);
		}
	}

	/**
	 * Determines if given <code>javax.mail.Part</code> instance is seen as
	 * INLINE.
	 * 
	 * @param the
	 *            part
	 * @return whether given <code>javax.mail.Part</code> instance is seen as
	 *         INLINE or not
	 * @throws MessagingException
	 */
	protected boolean isInline(final Part part) throws MessagingException {
		return ((part.getDisposition() == null || Part.INLINE.equalsIgnoreCase(part.getDisposition())) && part
				.getFileName() == null);
	}
}
