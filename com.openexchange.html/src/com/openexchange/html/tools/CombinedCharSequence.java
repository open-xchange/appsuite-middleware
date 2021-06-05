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

package com.openexchange.html.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link CombinedCharSequence}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CombinedCharSequence implements CharSequence {

    private final Collection<CharSequence> sequences;

    /**
     * Initializes a new {@link CombinedCharSequence}.
     */
    public CombinedCharSequence(CharSequence... sequences) {
        super();
        this.sequences = Arrays.asList(sequences);
    }

    /**
     * Initializes a new {@link CombinedCharSequence}.
     */
    CombinedCharSequence(Collection<CharSequence> sequences) {
        super();
        this.sequences = sequences;
    }


    @Override
    public int length() {
        int length = 0;
        for (CharSequence sequence : sequences) {
            length += sequence.length();
        }
        return length;
    }

    @Override
    public char charAt(int index) {
        int start = 0;
        int end;
        for (CharSequence sequence : sequences) {
            end = sequence.length() + start;
            if (index >= start && index < end) {
                return sequence.charAt(index - start);
            }
            start = end;
        }
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }

    @Override
    public CharSequence subSequence(int st, int en) {
        List<CharSequence> seqs = new LinkedList<>();

        int start = 0;
        int end;
        for (CharSequence sequence : sequences) {
            end = sequence.length() + start;
            if (st >= start && en <= end) {
                return sequence.subSequence(st - start, en - start);
            }
            if (st >= start && st < end && en > end) {
                seqs.add(sequence.subSequence(st - start, end));
            } else if (st < start && st < end && en <= end) {
                seqs.add(sequence.subSequence(0, en - start));
            } else if (st < start && st < end && en > end) {
                seqs.add(sequence);
            }
            start = end;
        }

        return new CombinedCharSequence(seqs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(sequences.size() << 4);
        for (CharSequence sequence : sequences) {
            sb.append(sequence);
        }
        return sb.toString();
    }

}
