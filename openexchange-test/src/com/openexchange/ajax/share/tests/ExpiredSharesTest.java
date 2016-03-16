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

package com.openexchange.ajax.share.tests;

import java.util.Date;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ExpiredSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias
 *         Friedrich</a>
 */
public class ExpiredSharesTest extends ShareTest {

	/**
	 * Initializes a new {@link ExpiredSharesTest}.
	 *
	 * @param name
	 *            The test name
	 */
	public ExpiredSharesTest(String name) {
		super(name);
	}

	public void testAccessExpiredShareRandomly() throws Exception {
		testAccessExpiredShare(randomFolderAPI(), randomModule());
	}

	public void noTestAccessExpiredShareExtensively() throws Exception {
		for (EnumAPI api : TESTED_FOLDER_APIS) {
			for (int module : TESTED_MODULES) {
				testAccessExpiredShare(api, module);
			}
		}
	}

	private void testAccessExpiredShare(EnumAPI api, int module) throws Exception {
		testAccessExpiredShare(api, module, getDefaultFolder(module));
	}

	private void testAccessExpiredShare(EnumAPI api, int module, int parent) throws Exception {
		/*
		 * apply expiration time to permission
		 */
		long expirationTime = 10000L; // 10 seconds
		Date expires = new Date(System.currentTimeMillis() + expirationTime);
		OCLGuestPermission guestPermission = createAnonymousGuestPermission();
		((AnonymousRecipient) guestPermission.getRecipient()).setExpiryDate(expires);
		/*
		 * create folder shared to guest user
		 */
		FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
		/*
		 * check permissions
		 */
		OCLPermission matchingPermission = null;
		for (OCLPermission permission : folder.getPermissions()) {
			if (permission.getEntity() != client.getValues().getUserId()) {
				matchingPermission = permission;
				break;
			}
		}
		assertNotNull("No matching permission in created folder found", matchingPermission);
		checkPermissions(guestPermission, matchingPermission);
		/*
		 * discover & check guest
		 */
		ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(),
				matchingPermission.getEntity());
		checkGuestPermission(guestPermission, guest);
		/*
		 * check access to share
		 */
		String shareURL = discoverShareURL(guest);
		GuestClient guestClient = resolveShare(shareURL, guestPermission.getRecipient());
		guestClient.checkShareModuleAvailable();
		guestClient.checkShareAccessible(guestPermission);
		/*
		 * wait some time until the share is expired
		 */
		Thread.sleep(expirationTime);
		/*
		 * check if share link still accessible
		 */
		GuestClient revokedGuestClient = new GuestClient(shareURL, guestPermission.getRecipient(), false);
		System.out.println("Share url for revoked guest: " + shareURL);
		ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
		if (shareResolveResponse.getResponse() != null) {
			System.out.println("Share resolve response error messages: " + shareResolveResponse.getErrorMessage());
			System.out.println("Share resolve response exception: " + shareResolveResponse.getException().getMessage());
			System.out.println("Share resolve response content: " + shareResolveResponse.getResponse().getJSON());
		}
		if (null != shareResolveResponse.getConflicts()) {
		    System.out.println("Share resolve response conflicts: " + shareResolveResponse.getConflicts().size());
		}
		assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND, shareResolveResponse.getStatus());
		/*
		 * check permissions of previously shared folder
		 */
		folder = getFolder(api, folder.getObjectID());
		for (OCLPermission permission : folder.getPermissions()) {
			assertTrue("Guest permission still present", permission.getEntity() != matchingPermission.getEntity());
		}
		/*
		 * check guest entity
		 */
		guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
		assertNull("guest entity still found", guest);
		/*
		 * check guest access to share
		 */
		if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())
				&& null == ((AnonymousRecipient) guestPermission.getRecipient()).getPassword()) {
			// TODO: apply cookie expiry also for anonymous_password
			// authentication
			/*
			 * for anonymous guest user, check access with previous guest
			 * session (after waiting some time until background operations took
			 * place)
			 */
			checkGuestUserDeleted(matchingPermission.getEntity());
			guestClient.checkSessionAlive(true);
		} else {
			/*
			 * check if share target no longer accessible for non-anonymous
			 * guest user, since session may still be alive
			 */
			guestClient.checkFolderNotAccessible(String.valueOf(folder.getObjectID()));
			guestClient.logout();
		}
	}

}
