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

package com.openexchange.imap;

import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import gnu.trove.map.TIntObjectMap;

/**
 * {@link SpecialUseDefaultFolderChecker} - The IMAP default folder checker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpecialUseDefaultFolderChecker extends IMAPDefaultFolderChecker {

    /** The logger constant */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SpecialUseDefaultFolderChecker.class);
    private static final String SET_SPECIAL_USE_PROPERTY = "com.openexchange.imap.setSpecialUseFlags";

    // -------------------------------------------------------------------------------------------------------------------------------- //

    private final boolean hasCreateSpecialUse;

    /**
     * Initializes a new {@link SpecialUseDefaultFolderChecker}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapAccess The IMAP access
     * @param hasCreateSpecialUse Whether the IMAP server advertises "CREATE-SPECIAL-USE" capability string
     * @param hasMetadata Whether the IMAP server advertises "METADATA" capability string
     */
    public SpecialUseDefaultFolderChecker(int accountId, Session session, Context ctx, IMAPStore imapStore, IMAPAccess imapAccess, boolean hasCreateSpecialUse, boolean hasMetadata) {
        super(accountId, session, ctx, imapStore, imapAccess, hasMetadata);
        this.hasCreateSpecialUse = hasCreateSpecialUse;
    }

    @Override
    protected boolean setSpecialUseForExisting() {
        return hasCreateSpecialUse;
    }

    @Override
    protected void createIfNonExisting(IMAPFolder f, int type, char sep, String namespace, int index, TIntObjectMap<String> specialUseInfo) throws MessagingException {
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        boolean setSpecialUseFlags = false;
        try {
            ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            setSpecialUseFlags = view.opt(SET_SPECIAL_USE_PROPERTY, Boolean.class, false);
        } catch (OXException e1) {
            LOG.error(e1.getMessage());
            // continue with default value
        }

        if (!f.exists()) {
            try {
                if (hasCreateSpecialUse && setSpecialUseFlags && specialUseInfo.get(index) == null) {
                    // E.g. CREATE MyDrafts (USE (\Drafts))
                    List<String> specialUses = index <= StorageUtility.INDEX_TRASH ? Collections.singletonList(SPECIAL_USES[index]) : null;
                    IMAPCommandsCollection.createFolder(f, sep, type, false, specialUses);
                } else {
                    IMAPCommandsCollection.createFolder(f, sep, type, false);
                    if (hasMetadata && setSpecialUseFlags && index <= StorageUtility.INDEX_TRASH && specialUseInfo.get(index) == null) {
                        // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                        String flag = SPECIAL_USES[index];
                        try {
                            IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(flag));
                        } catch (Exception e) {
                            LOG.debug("Failed to set {} flag for new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                        }
                    }
                }
                LOG.info("Created new standard {} folder (full-name={}, namespace={}) for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            } catch (MessagingException e) {
                LOG.warn("Failed to create new standard {} folder (full-name={}, namespace={}) for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                throw e;
            }
        } else {
            if (hasMetadata && setSpecialUseFlags && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(flag));
                } catch (Exception e) {
                    LOG.debug("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                }
            }
        }
    }

}
