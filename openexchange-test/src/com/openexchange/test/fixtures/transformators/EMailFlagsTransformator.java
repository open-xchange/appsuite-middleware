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
package com.openexchange.test.fixtures.transformators;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * Transforms textual representations of email flags to the corresponding integer values defined
 * by the MailMessage class, e.g. "FLAG_DRAFT" becomes 4.
 *
 * @author tfriedrich
 */
public class EMailFlagsTransformator implements Transformator {

	@Override
    public Object transform(final String value) throws OXException {
		if (null == value || 1 > value.length()) { return 0; }
		int flags = 0;
		final String[] splitted = value.split(",");
		for (final String flag : splitted) {
			if (null != flag) {
				flags |= getFlag(flag.trim());
			}
		}
		return flags;
    }

	private int getFlag(final String flag) {
        if ("FLAG_ANSWERED".equalsIgnoreCase(flag) || "ANSWERED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_ANSWERED;
        } else if ("FLAG_DELETED".equalsIgnoreCase(flag) || "DELETED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_DELETED;
        } else if ("FLAG_DRAFT".equalsIgnoreCase(flag) || "DRAFT".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_DRAFT;
        } else if ("FLAG_FLAGGED".equalsIgnoreCase(flag) || "FLAGGED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_FLAGGED;
        } else if ("FLAG_RECENT".equalsIgnoreCase(flag) || "RECENT".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_RECENT;
        } else if ("FLAG_SEEN".equalsIgnoreCase(flag) || "SEEN".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_SEEN;
        } else if ("FLAG_USER".equalsIgnoreCase(flag) || "USER".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_USER;
        } else if ("FLAG_SPAM".equalsIgnoreCase(flag) || "SPAM".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_SPAM;
        } else if ("FLAG_FORWARDED".equalsIgnoreCase(flag) || "FORWARDED".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_FORWARDED;
        } else if ("FLAG_READ_ACK".equalsIgnoreCase(flag) || "READ_ACK".equalsIgnoreCase(flag)) {
            return MailMessage.FLAG_READ_ACK;
        } else {
        	return 0;
        }
	}
}
