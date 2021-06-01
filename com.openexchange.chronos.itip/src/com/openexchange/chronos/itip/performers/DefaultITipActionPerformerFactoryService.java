/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.itip.performers;

import java.util.Collection;
import java.util.EnumMap;
import com.openexchange.chronos.itip.CalendarITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipActionPerformer;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.sender.MailSenderService;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class DefaultITipActionPerformerFactoryService implements ITipActionPerformerFactoryService {

    public static final Integer RANKING = Integer.valueOf(0);

    private final EnumMap<ITipAction, ITipActionPerformer> performerRegistry = new EnumMap<>(ITipAction.class);

    public DefaultITipActionPerformerFactoryService(CalendarITipIntegrationUtility util, MailSenderService sender, ITipMailGeneratorFactory generators) {
        addPerformer(new UpdatePerformer(util, sender, generators));
        addPerformer(new CancelPerformer(util, sender, generators));
        addPerformer(new MailPerformer(util, sender, generators));
    }

    public void addPerformer(ITipActionPerformer performer) {
        Collection<ITipAction> supportedActions = performer.getSupportedActions();
        for (ITipAction action : supportedActions) {
            performerRegistry.put(action, performer);
        }
    }

    @Override
    public ITipActionPerformer getPerformer(ITipAction action) {
        return performerRegistry.get(action);
    }

    @Override
    public Collection<ITipAction> getSupportedActions() {
        return performerRegistry.keySet();
    }

}
