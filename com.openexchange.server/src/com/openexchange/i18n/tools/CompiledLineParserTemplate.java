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

package com.openexchange.i18n.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link CompiledLineParserTemplate} - Compiles a template as per
 * LineParserUtility syntax, with a simple substitution of [variables]. Allows
 * escaping via \
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class CompiledLineParserTemplate extends AbstractTemplate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompiledLineParserTemplate.class);

    private static final String STR_EMPTY = "";

    /**
     * Constructor for subclassing.
     */
    protected CompiledLineParserTemplate() {
        super();
    }

    private int[][] positions;

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(final Locale locale, final RenderMap renderMap) {
        final char[] content = StringHelper.valueOf(locale).getString(getContent()).toCharArray();
        if (null == content) {
            return STR_EMPTY;
        }

        if (positions == null) {
            positions = load(content);
        }

        final StringBuilder result = new StringBuilder(content.length + 1024);
        int off = 0;
        for (int i = 0; i < positions.length; i++) {
            final int bracketS = positions[i][0];
            final int bracketE = positions[i][1];
            result.append(content, off, bracketS - off);
            final String toReplace = new String(content, bracketS + 1, bracketE - bracketS - 1);
            final TemplateReplacement repl = renderMap.get(toReplace);
            if (repl == null) {
                result.append(STR_EMPTY);
            } else {
                result.append(repl.getReplacement());
            }
            off = bracketE + 1;
        }
        if (off < content.length) {
            result.append(content, off, content.length - off);
        }
        return result.toString();
    }

    private static final int[][] load(final char[] content) {
        final List<int[]> positions = new ArrayList<int[]>();
        // Lexer for the poor
        // LABSKAUSS!!!!! ;-)
        boolean escaped = false;
        int lineCount = 1;
        int columnCount = 1;
        int[] open = null;
        int firstPos = -1;

        for (int i = 0; i < content.length; i++) {
            final char c = content[i];
            switch (c) {
            case '[':
                if (escaped) {
                    escaped = false;
                } else {
                    firstPos = i;
                    open = new int[] { lineCount, columnCount };
                }
                columnCount++;
                break;
            case ']':
                if (escaped) {
                    escaped = false;
                } else {
                    if(firstPos == -1) {
                        LOG.error("Parser Error: Missing opening bracket in line: {}", lineCount, new Throwable());
                        open = null;
                    } else {
                        positions.add(new int[] { firstPos, i });
                        firstPos = -1;
                        open = null;
                    }
                }
                columnCount++;
                break;
            case '\\':
                if (escaped) {
                    escaped = false;
                } else {
                    escaped = true;
                }
                columnCount++;
                break;
            case '\n':
                lineCount++;
                columnCount = 0;
                break;
            default:
                if (escaped) {
                    escaped = false;
                }
                columnCount++;
            }
        }

        if (open != null) {
            LOG.error("Parser Error: Seems that the bracket opened on line {} column {} is never closed.", open[0], open[1], new Throwable());
            return new int[0][];
        }

        final int[][] positionsArr = new int[positions.size()][];
        for (int i = 0; i < positionsArr.length; i++) {
            positionsArr[i] = positions.get(i);
        }
        return positionsArr;
    }

    protected abstract String getContent();

}
