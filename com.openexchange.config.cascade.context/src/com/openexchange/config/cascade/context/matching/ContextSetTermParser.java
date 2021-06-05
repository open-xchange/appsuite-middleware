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

    @SuppressWarnings("unused")
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
