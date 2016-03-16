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

package com.openexchange.realtime.handle.impl.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.realtime.directory.ChangeListener;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.handle.TimedStanza;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.packet.ID;


/**
 * {@link ResourceListener}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResourceListener implements ChangeListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResourceListener.class);

    public ResourceListener() {
        super();
    }

    @Override
    public void added(ID id, Resource value) {
        ID general = id.toGeneralForm();
        try {
            StanzaStorage stanzaStorage = Services.getService(StanzaStorage.class);
            List<TimedStanza> stanzas = stanzaStorage.popStanzas(general);
            for (TimedStanza stanza : stanzas) {
                retrySendOrRestoreMessage(id, stanza, 2, null);
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("Could not handle added resource.",t);
        }
    }

    private void retrySendOrRestoreMessage(ID receiver, TimedStanza stanza, int retryCount, OXException exception) {
        if (retryCount == 0) {
            if (exception == null) {
                LOG.warn("Could not send message to {}. Message was put back to the store.", receiver);
            } else {
                LOG.warn("Could not send message to {}. Message was put back to the store.", receiver, exception);
            }

            StanzaStorage stanzaStorage = Services.getService(StanzaStorage.class);
            try {
                stanzaStorage.pushStanza(receiver.toGeneralForm(), stanza);
            } catch (OXException e) {
                LOG.error("Could not put message back to store.", e);
            }

            return;
        }

        try {
            LocalMessageDispatcher dispatcher = Services.getService(LocalMessageDispatcher.class);
            Map<ID, OXException> sent = dispatcher.send(stanza.getStanza(), Collections.singleton(receiver));
            if (sent.size() == 1) {
                retrySendOrRestoreMessage(receiver, stanza, --retryCount, null);
            }
        } catch (OXException e) {
            retrySendOrRestoreMessage(receiver, stanza, --retryCount, e);
        }
    }

    @Override
    public void updated(ID id, Resource value, Resource previousValue) {
        LOG.debug("Updated called for ResourceListener.");
        added(id, previousValue);
    }

    @Override
    public void removed(ID id, Resource value) {
        // Nothing to do here
    }

}
