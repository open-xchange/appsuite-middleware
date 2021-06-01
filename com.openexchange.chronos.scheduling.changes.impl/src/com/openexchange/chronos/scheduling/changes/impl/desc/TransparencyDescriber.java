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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;

/**
 * {@link TransparencyDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class TransparencyDescriber extends AbstractChangeDescriber<Transp> {

    /**
     * Initializes a new {@link TransparencyDescriber}.
     */
    public TransparencyDescriber() {
        super(EventField.TRANSP, Transp.class);
    }

    @Override
    public List<SentenceImpl> describe(Transp original, Transp updated) {
        SentenceImpl sentence = new SentenceImpl(Messages.HAS_CHANGED_SHOWN_AS).add(transpToString(updated), ArgumentType.SHOWN_AS, shown(updated));
        return Collections.singletonList(sentence);
    }

    private String transpToString(Transp transparency) {
        return null != transparency && Transp.TRANSPARENT.equals(transparency.getValue()) ? Messages.FREE : Messages.RESERVERD;
    }

    private ShownAsTransparency shown(Transp transparency) {
        return null != transparency && Transp.TRANSPARENT.equals(transparency.getValue()) ? ShownAsTransparency.FREE : ShownAsTransparency.RESERVED;
    }

}
