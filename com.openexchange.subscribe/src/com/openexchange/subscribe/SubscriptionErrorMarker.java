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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;

/**
 * {@link SubscriptionErrorMarker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
class SubscriptionErrorMarker {

    private static final String ERRORS_KEY = "errors";
    private final AbstractSubscribeService subscribeService;
    private final FolderService folderService;

    /**
     * Initializes a new {@link SubscriptionErrorMarker}.
     */
    SubscriptionErrorMarker(AbstractSubscribeService subscribeService, FolderService folderService) {
        super();
        Objects.requireNonNull(subscribeService);
        Objects.requireNonNull(folderService);
        this.subscribeService = subscribeService;
        this.folderService = folderService;
    }

    /**
     * Marks the specified subscription and its folder with an error
     *
     * @param subscription The subscription to mark
     * @throws OXException if the subscription cannot be marked
     */
    void mark(Subscription subscription) throws OXException {
        markFolderWithError(subscription);
        markSubscriptionWithError(subscription);
    }

    /**
     * Unmarks the specified subscription and its folder as error-free
     *
     * @param subscription The subscription to unmark
     * @throws OXException if the subscription cannot be unmarked
     */
    void unmark(Subscription subscription) throws OXException {
        unmarkFolder(subscription);
        unmarkSubscription(subscription);
    }

    ///////////////////////////////// MARK //////////////////////////////////////

    /**
     * Marks the folder of the specified subscription with an error.
     *
     * @param subscription The subscription
     * @throws OXException if updating the folder's metadata fails
     */
    private void markFolderWithError(Subscription subscription) throws OXException {
        Folder folder = folderService.getFolder(FolderStorage.PRIVATE_ID, subscription.getFolderId(), subscription.getSession(), null);
        Map<String, Object> originalMeta = folder.getMeta();
        // No meta yet
        if (originalMeta == null) {
            folder.setMeta(ImmutableMap.of(ERRORS_KEY, "true"));
            folderService.updateFolder(folder, null, subscription.getSession(), null);
            return;
        }
        // Has meta, but not an 'error' field
        if (false == originalMeta.containsKey(ERRORS_KEY)) {
            updateFolder(subscription, folder, new HashMap<>(originalMeta), true);
            return;
        }

        Object value = originalMeta.get(ERRORS_KEY);
        // Has a string 'error' field
        if (value instanceof String) {
            markFolderIfNecessary(subscription, folder, originalMeta, Boolean.valueOf((String) value));
            return;
        }
        // Has a boolean 'error' field
        if (value instanceof Boolean) {
            markFolderIfNecessary(subscription, folder, originalMeta, (Boolean) value);
            return;
        }
        // Everything else
        updateFolder(subscription, folder, new HashMap<>(originalMeta), true);
    }

    /**
     * Marks the specified folder with errors only if necessary
     *
     * @param subscription The subscription
     * @param folder The folder
     * @param originalMeta The original metadata
     * @param b Whether the folder already has errors
     * @throws OXException if updating the folder's metadata fails
     */
    private void markFolderIfNecessary(Subscription subscription, Folder folder, Map<String, Object> originalMeta, Boolean b) throws OXException {
        if (b.booleanValue()) {
            return;
        }
        updateFolder(subscription, folder, new HashMap<>(originalMeta), true);
    }

    /**
     * Marks the specified subscription with an error.
     *
     * @param subscription The subscription
     * @throws OXException if updating the subscription's configuration fails
     */
    private void markSubscriptionWithError(Subscription subscription) throws OXException {
        if (subscription.getConfiguration() == null) {
            updateSubscription(subscription, new HashMap<>(), true);
            return;
        }
        if (false == subscription.getConfiguration().containsKey(ERRORS_KEY)) {
            updateSubscription(subscription, new HashMap<>(), true);
            return;
        }

        Object value = subscription.getConfiguration().get(ERRORS_KEY);
        if (value instanceof String) {
            markSubscriptionIfNecessary(subscription, Boolean.valueOf((String) value));
            return;
        }
        if (value instanceof Boolean) {
            markSubscriptionIfNecessary(subscription, (Boolean) value);
            return;
        }
        updateSubscription(subscription, subscription.getConfiguration(), false);
    }

    /**
     * Marks the specified subscription with errors only if necessary
     *
     * @param subscription The subscription
     * @param b Whether the subscription already has errors
     * @throws OXException if updating the subscription's configuration fails
     */
    private void markSubscriptionIfNecessary(Subscription subscription, Boolean b) throws OXException {
        if (b.booleanValue()) {
            return;
        }
        updateSubscription(subscription, subscription.getConfiguration(), true);
    }

    /////////////////////////////////// UNMARK //////////////////////////////////////

    /**
     * Marks the folder as error-free
     * 
     * @param subscription The subscription with the folder
     * @throws OXException if an error is occurred
     */
    private void unmarkFolder(Subscription subscription) throws OXException {
        Folder folder = folderService.getFolder(FolderStorage.PRIVATE_ID, subscription.getFolderId(), subscription.getSession(), null);
        Map<String, Object> originalMeta = folder.getMeta();
        // No meta yet
        if (originalMeta == null) {
            return;
        }
        // Has meta, but not an 'error' field
        if (false == originalMeta.containsKey(ERRORS_KEY)) {
            return;
        }
        updateFolder(subscription, folder, new HashMap<>(originalMeta), false);
    }

    /**
     * Marks the specified subscription with an error.
     *
     * @param subscription The subscription
     * @throws OXException if updating the subscription's configuration fails
     */
    private void unmarkSubscription(Subscription subscription) throws OXException {
        if (subscription.getConfiguration() == null) {
            updateSubscription(subscription, new HashMap<>(), false);
            return;
        }
        if (false == subscription.getConfiguration().containsKey(ERRORS_KEY)) {
            return;
        }
        updateSubscription(subscription, subscription.getConfiguration(), false);
    }

    ///////////////////////////////// STORAGE /////////////////////////////////////

    /**
     * Updates the specified folder
     *
     * @param subscription The subscription
     * @param folder The folder
     * @param meta The metadata to write to the folder
     * @param errorsFlag The errors flag
     * @throws OXException if updating the folder's metadata fails
     */
    private void updateFolder(Subscription subscription, Folder folder, Map<String, Object> meta, boolean errorsFlag) throws OXException {
        if (errorsFlag) {
            meta.put(ERRORS_KEY, Boolean.toString(errorsFlag));
        } else {
            meta.remove(ERRORS_KEY);
        }
        folder.setMeta(meta);
        folderService.updateFolder(folder, null, subscription.getSession(), null);
    }

    /**
     * Updates the specified subscription
     *
     * @param subscription The subscription
     * @param errorsFlag The errors flag
     * @param meta The configuration with the error flag to write to the subscription
     * @throws OXException if updating the subscription's configuration fails
     */
    private void updateSubscription(Subscription subscription, Map<String, Object> config, boolean errorsFlag) throws OXException {
        config.put(ERRORS_KEY, Boolean.toString(errorsFlag));
        subscription.setConfiguration(config);
        subscribeService.doUpdate(subscription, subscription);
    }
}
