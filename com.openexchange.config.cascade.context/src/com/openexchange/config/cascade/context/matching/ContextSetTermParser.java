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

package com.openexchange.config.cascade.context.matching;

import com.openexchange.java.Strings;

/**
 * {@link ContextSetTermParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextSetTermParser {

    public ContextSetTerm parse(String string) {
        Lexer lexer = new Lexer(string);
        ContextSetTerm term = terms(lexer);
        return term;
    }

    private ContextSetTerm terms(Lexer lexer) {
        ContextSetTerm term = term(lexer);
        while (lexer.lookahead(Terminal.OPERATOR)) {
            Operator op = (Operator) lexer.next();
            switch (op) {
            case AND:
                term = new And(term, term(lexer));
                break;
            case OR:
                term = new Or(term, term(lexer));
                break;
            }
        }

        return term;
    }


    private ContextSetTerm term(Lexer lexer) {
        if (lexer.lookahead(Terminal.TAG)) {
            return tagOrOperation(lexer);
        } else if (lexer.lookahead(Terminal.NEGATION)) {
            lexer.next();
            return new Negation(tagOrBracket(lexer));
        } else if (lexer.lookahead(Terminal.OPEN_BRACKET)) {
            lexer.next();
            ContextSetTerm terms = terms(lexer);
            lexer.expect(Terminal.CLOSE_BRACKET);
            lexer.next();
            return terms;
        }
        return null;
    }

    private ContextSetTerm tagOrBracket(Lexer lexer) {
        if (lexer.lookahead(Terminal.TAG)) {
            return tag(lexer);
        } else if (lexer.lookahead(Terminal.OPEN_BRACKET)) {
            lexer.next();
            ContextSetTerm terms = terms(lexer);
            lexer.expect(Terminal.CLOSE_BRACKET);
            lexer.next();
            return terms;
        }
        return null;
    }

    private ContextSetTerm tagOrOperation(Lexer lexer) {
        ContextSetTerm term = tag(lexer);
        while (lexer.lookahead(Terminal.OPERATOR)) {
            Operator op = (Operator) lexer.next();
            switch (op) {
            case AND:
                term = new And(term, term(lexer));
                break;
            case OR:
                term = new Or(term, term(lexer));
                break;
            }
        }

        return term;
    }

    private ContextSetTerm tag(Lexer lexer) {
        return new HasTag((String) lexer.next());
    }

    private enum Operator {
        AND, OR;

    }

    private enum Terminal {
        TAG, OPERATOR, NEGATION, OPEN_BRACKET, CLOSE_BRACKET, EOF
    }

    private static class Lexer {

        private final String string;

        private int index;

        private Object current;

        private Terminal currentType;

        public Lexer(String string) {
            this.string = string;
            this.index = 0;
            next();
        }

        public String tag() {
            return (String) current;
        }

        public Operator operator() {
            return (Operator) current;
        }

        public boolean lookahead(Terminal type) {
            return currentType == type;
        }

        public void expect(Terminal type) {
            if (!lookahead(type)) {
                throw new IllegalArgumentException("I expected a " + type + " but instead got a " + currentType);
            }
        }

        public Object next() {
            Object retval = current;
            if (index >= string.length()) {
                currentType = Terminal.EOF;
                current = null;
                return retval;
            }
            int length = string.length();
            StringBuilder tagBuilder = new StringBuilder();
            while (index < length) {
                char ch = string.charAt(index++);
                if (Strings.isWhitespace(ch)) {
                    if (!finishTag(tagBuilder)) {
                        continue;
                    }
                    return retval;
                }
                switch (ch) {
                case '&':
                    if (finishTag(tagBuilder)) {
                        index--; // Handle this one later
                    } else {
                        current = Operator.AND;
                        currentType = Terminal.OPERATOR;
                    }
                    return retval;
                case '|':
                    if (finishTag(tagBuilder)) {
                        index--; // Handle this one later
                    } else {
                        current = Operator.OR;
                        currentType = Terminal.OPERATOR;
                    }
                    return retval;
                case '!':
                    if (finishTag(tagBuilder)) {
                        index--; // Handle this one later
                    } else {
                        current = null;
                        currentType = Terminal.NEGATION;
                    }
                    return retval;
                case '(':
                    if (finishTag(tagBuilder)) {
                        index--; // Handle this one later
                    } else {
                        current = null;
                        currentType = Terminal.OPEN_BRACKET;
                    }
                    return retval;
                case ')':
                    if (finishTag(tagBuilder)) {
                        index--; // Handle this one later
                    } else {
                        current = null;
                        currentType = Terminal.CLOSE_BRACKET;
                    }
                    return retval;
                default:
                    tagBuilder.append(ch);
                    break;
                }
            }
            if (!finishTag(tagBuilder)) {
                currentType = Terminal.EOF;
                current = null;
                return null;
            }
            return retval;
        }

        private boolean finishTag(StringBuilder tagBuilder) {
            if (tagBuilder.length() != 0) {
                currentType = Terminal.TAG;
                current = tagBuilder.toString();
                return true;
            }
            return false;
        }

        public boolean eof() {
            return currentType == Terminal.EOF;
        }
    }

}
