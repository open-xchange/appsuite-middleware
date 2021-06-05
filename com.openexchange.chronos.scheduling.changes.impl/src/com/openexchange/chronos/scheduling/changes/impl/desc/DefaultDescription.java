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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.Sentence;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;

/**
 * {@link DefaultDescription}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DefaultDescription implements Description {

    private final List<EventField> fields;
    private final List<Sentence> sentences;

    /**
     * Initializes a new {@link DefaultDescription}.
     *
     * @param sentence The sentence
     * @param field The field
     */
    public DefaultDescription(SentenceImpl sentence, EventField field) {
        this(Collections.singletonList(sentence), Collections.singletonList(field));
    }

    /**
     * Initializes a new {@link DefaultDescription}.
     *
     * @param sentences The sentences
     * @param field The field
     */
    public DefaultDescription(List<SentenceImpl> sentences, EventField field) {
        this(sentences, Collections.singletonList(field));
    }

    /**
     * Initializes a new {@link DefaultDescription}.
     *
     * @param sentences The sentences
     * @param fields The fields
     */
    public DefaultDescription(List<SentenceImpl> sentences, List<EventField> fields) {
        super();
        this.sentences = new ArrayList<>(sentences);
        this.fields = fields;
    }

    @Override
    public List<EventField> getChangedFields() {
        return fields;
    }

    @Override
    public List<Sentence> getSentences() {
        return sentences;
    }

}
