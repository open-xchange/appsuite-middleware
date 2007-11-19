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

package com.openexchange.groupware.upload.quotachecker;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.upload.impl.UploadQuotaChecker;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * MailUploadQuotaChecker
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailUploadQuotaChecker extends UploadQuotaChecker {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailUploadQuotaChecker.class);

	private final long uploadQuota;

	private final long uploadQuotaPerFile;

	public MailUploadQuotaChecker(final Session session) {
		super();
		UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(),
				session.getContext());
		if (settings.getUploadQuota() > 0) {
			uploadQuota = settings.getUploadQuota();
		} else if (settings.getUploadQuota() == 0) {
			uploadQuota = -1;
		} else {
			/*
			 * Fallback to global upload quota
			 */
			int globalQuota;
			try {
				globalQuota = ServerConfig.getInteger(Property.MAX_UPLOAD_SIZE);
			} catch (final ConfigurationException e) {
				LOG.error(e.getLocalizedMessage(), e);
				globalQuota = 0;
			}
			if (globalQuota > 0) {
				uploadQuota = globalQuota;
			} else {
				uploadQuota = -1;
			}
		}
		uploadQuotaPerFile = settings.getUploadQuotaPerFile() > 0 ? settings.getUploadQuotaPerFile() : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadQuotaChecker#getFileQuotaMax()
	 */
	@Override
	public long getFileQuotaMax() {
		return uploadQuotaPerFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadQuotaChecker#getQuotaMax()
	 */
	@Override
	public long getQuotaMax() {
		return uploadQuota;
	}

}
