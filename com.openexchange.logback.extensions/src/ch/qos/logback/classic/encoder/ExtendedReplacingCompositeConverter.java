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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package ch.qos.logback.classic.encoder;

import java.util.List;
import java.util.regex.Pattern;
import ch.qos.logback.core.pattern.ReplacingCompositeConverter;

/**
 * {@link ExtendedReplacingCompositeConverter} - Extends <code>ReplacingCompositeConverter</code> by support for line-feeds specified in
 * replacement string; e.g.
 *
 * <pre>
 *  %ereplace(%msg){'n','\n'} // Replace every character 'n' in log message with a line-feed
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExtendedReplacingCompositeConverter<E> extends ReplacingCompositeConverter<E> {

    private Pattern pattern;
    private String replacement;

    /**
     * Initializes a new {@link ExtendedReplacingCompositeConverter}.
     */
    public ExtendedReplacingCompositeConverter() {
        super();
    }

    @Override
    public void start() {
        final List<String> optionList = getOptionList();
        if (optionList == null) {
            addError("at least two options are expected whereas you have declared none");
            return;
        }

        final int numOpts = optionList.size();

        if (numOpts < 2) {
            addError("at least two options are expected whereas you have declared only " + numOpts + "as [" + optionList + "]");
            return;
        }
        pattern = Pattern.compile(optionList.get(0));
        // Support line-feeds
        replacement = optionList.get(1).replaceAll("%n|(\\\\r)?\\\\n", System.getProperty("line.separator"));
        super.start();
    }

    @Override
    protected String transform(final E event, final String in) {
        if (!started) {
            return in;
        }
        return pattern.matcher(in).replaceAll(replacement);
    }

}
