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


/**
 * IMAPCapabilities
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPCapabilities {
	
	/*
	 * String Constants
	 */
	public static final String CAP_ACL = "ACL";
	
	public static final String CAP_THREAD_REFERENCES = "THREAD=REFERENCES";
	
	public static final String CAP_THREAD_ORDEREDSUBJECT = "THREAD=ORDEREDSUBJECT";
	
	public static final String CAP_QUOTA = "QUOTA";
	
	public static final String CAP_IMAP4 = "IMAP4";
	
	public static final String CAP_IMAP4_REV1 = "IMAP4rev1";
	
	public static final String CAP_UIDPLUS = "UIDPLUS";
	
	public static final String CAP_SORT = "SORT";
	
	/*
	 * Bit Constants
	 */
	public static final int BIT_ACL = 1;
	
	public static final int BIT_THREAD_REFERENCES = 2;
	
	public static final int BIT_THREAD_ORDEREDSUBJECT = 4;
	
	public static final int BIT_QUOTA = 8;
	
	public static final int BIT_IMAP4 = 16;
	
	public static final int BIT_IMAP4_REV1 = 32;
	
	public static final int BIT_UIDPLUS = 64;
	
	public static final int BIT_SORT = 128;
	
	public static final int BIT_SUBSCRIPTION = 256;
	
	/*
	 * Members
	 */
	private boolean hasACL;
	
	private boolean hasQuota;
	
	private boolean hasThreadReferences;
	
	private boolean hasThreadOrderedSubject;
	
	private boolean hasSort;
	
	private boolean hasIMAP4;
	
	private boolean hasIMAP4rev1;
	
	private boolean hasUIDPlus;
	
	private boolean hasSubscription;

	public IMAPCapabilities() {
		super();
	}

	public boolean hasACL() {
		return hasACL;
	}

	/**
	 * Sets ACL support to given value
	 * 
	 * @param hasACL
	 */
	public void setACL(final boolean hasACL) {
		this.hasACL = hasACL;
	}

	public boolean hasIMAP4() {
		return hasIMAP4;
	}

	public void setIMAP4(final boolean hasIMAP4) {
		this.hasIMAP4 = hasIMAP4;
	}

	public boolean hasIMAP4rev1() {
		return hasIMAP4rev1;
	}

	public void setIMAP4rev1(final boolean hasIMAP4rev1) {
		this.hasIMAP4rev1 = hasIMAP4rev1;
	}

	public boolean hasQuota() {
		return hasQuota;
	}

	public void setQuota(final boolean hasQuota) {
		this.hasQuota = hasQuota;
	}

	public boolean hasSort() {
		return hasSort;
	}

	public void setSort(final boolean hasSort) {
		this.hasSort = hasSort;
	}

	public boolean hasThreadOrderedSubject() {
		return hasThreadOrderedSubject;
	}

	public void setThreadOrderedSubject(final boolean hasThreadOrderedSubject) {
		this.hasThreadOrderedSubject = hasThreadOrderedSubject;
	}

	public boolean hasThreadReferences() {
		return hasThreadReferences;
	}

	public void setThreadReferences(final boolean hasThreadReferences) {
		this.hasThreadReferences = hasThreadReferences;
	}

	public boolean hasUIDPlus() {
		return hasUIDPlus;
	}

	public void setUIDPlus(final boolean hasUIDPlus) {
		this.hasUIDPlus = hasUIDPlus;
	}
	
	public boolean hasSubscription() {
		return hasSubscription;
	}

	public void setHasSubscription(final boolean hasSubscription) {
		this.hasSubscription = hasSubscription;
	}

	public final int getCapabilities() {
		int retval = 0;
		retval |= hasACL ? BIT_ACL : 0;
		retval |= hasIMAP4 ? BIT_IMAP4 : 0;
		retval |= hasIMAP4rev1 ? BIT_IMAP4_REV1 : 0;
		retval |= hasQuota ? BIT_QUOTA : 0;
		retval |= hasSort ? BIT_SORT : 0;
		retval |= hasThreadOrderedSubject ? BIT_THREAD_ORDEREDSUBJECT : 0;
		retval |= hasThreadReferences ? BIT_THREAD_REFERENCES : 0;
		retval |= hasUIDPlus ? BIT_UIDPLUS : 0;
		retval |= hasSubscription ? BIT_SUBSCRIPTION : 0;
		return retval;
	}
	
	public final void parseCapabilities(final int caps) {
		hasACL = ((caps & BIT_ACL) > 0);
		hasIMAP4 = ((caps & BIT_IMAP4) > 0);
		hasIMAP4rev1 = ((caps & BIT_IMAP4_REV1) > 0);
		hasQuota = ((caps & BIT_QUOTA) > 0);
		hasSort = ((caps & BIT_SORT) > 0);
		hasThreadOrderedSubject = ((caps & BIT_THREAD_ORDEREDSUBJECT) > 0);
		hasThreadReferences = ((caps & BIT_THREAD_REFERENCES) > 0);
		hasUIDPlus = ((caps & BIT_UIDPLUS) > 0);
		hasSubscription = ((caps & BIT_SUBSCRIPTION) > 0);
	}

}
