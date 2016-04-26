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

package com.openexchange.mail.json.compose.share.osgi;

import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.share.ShareComposeHandler;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistryImpl;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistryImpl;
import com.openexchange.mail.json.compose.share.internal.ShareLinkGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareLinkGeneratorRegistryImpl;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.json.compose.share.spi.ShareLinkGenerator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ShareComposeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ShareComposeActivator}.
     */
    public ShareComposeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        RankingAwareNearRegistryServiceTracker<ShareLinkGenerator> shareLinkGeneratorTracker = new RankingAwareNearRegistryServiceTracker<>(context, ShareLinkGenerator.class);
        rememberTracker(shareLinkGeneratorTracker);

        RankingAwareNearRegistryServiceTracker<MessageGenerator> messageGeneratorTracker = new RankingAwareNearRegistryServiceTracker<>(context, MessageGenerator.class);
        rememberTracker(messageGeneratorTracker);

        RankingAwareNearRegistryServiceTracker<AttachmentStorage> attachmentStorageTracker = new RankingAwareNearRegistryServiceTracker<>(context, AttachmentStorage.class);
        rememberTracker(attachmentStorageTracker);

        openTrackers();

        ShareLinkGeneratorRegistryImpl shareLinkGeneratorRegistry = new ShareLinkGeneratorRegistryImpl(shareLinkGeneratorTracker);
        registerService(ShareLinkGeneratorRegistry.class, shareLinkGeneratorRegistry);
        ServerServiceRegistry.getInstance().addService(ShareLinkGeneratorRegistry.class, shareLinkGeneratorRegistry);

        MessageGeneratorRegistryImpl messageGeneratorRegistry = new MessageGeneratorRegistryImpl(messageGeneratorTracker);
        registerService(MessageGeneratorRegistry.class, messageGeneratorRegistry);
        ServerServiceRegistry.getInstance().addService(MessageGeneratorRegistry.class, messageGeneratorRegistry);

        AttachmentStorageRegistryImpl attachmentStorageRegistry = new AttachmentStorageRegistryImpl(attachmentStorageTracker);
        registerService(AttachmentStorageRegistry.class, attachmentStorageRegistry);
        ServerServiceRegistry.getInstance().addService(AttachmentStorageRegistry.class, attachmentStorageRegistry);

        registerService(ComposeHandler.class, new ShareComposeHandler());
    }

    @Override
    protected void stopBundle() throws Exception {
        ServerServiceRegistry.getInstance().removeService(ShareLinkGeneratorRegistry.class);
        super.stopBundle();
    }

}
