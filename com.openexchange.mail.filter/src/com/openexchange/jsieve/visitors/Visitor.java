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
package com.openexchange.jsieve.visitors;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.parser.generated.ASTargument;
import org.apache.jsieve.parser.generated.ASTarguments;
import org.apache.jsieve.parser.generated.ASTblock;
import org.apache.jsieve.parser.generated.ASTcommand;
import org.apache.jsieve.parser.generated.ASTcommands;
import org.apache.jsieve.parser.generated.ASTstart;
import org.apache.jsieve.parser.generated.ASTstring;
import org.apache.jsieve.parser.generated.ASTstring_list;
import org.apache.jsieve.parser.generated.ASTtest;
import org.apache.jsieve.parser.generated.ASTtest_list;
import org.apache.jsieve.parser.generated.Node;
import org.apache.jsieve.parser.generated.SieveParserVisitor;
import org.apache.jsieve.parser.generated.SimpleNode;
import org.apache.jsieve.parser.generated.Token;

/**
 * This class uses the visitor pattern to get a sieve script out of the jjtree
 * representation
 *
 * @author d7
 *
 */
public class Visitor implements SieveParserVisitor {

    private static final String CRLF = "\r\n";

    public static class OwnType {
        private StringBuilder output;

        private int linenumber;

        public OwnType() {
            super();
            this.output = new StringBuilder();
            this.linenumber = -1;
        }

        public OwnType(final StringBuilder output, final int linenumber) {
            super();
            this.output = output;
            this.linenumber = linenumber;
        }

        public StringBuilder getOutput() {
            return output;
        }

        public void setOutput(final StringBuilder output) {
            this.output = output;
        }

        public int getLinenumber() {
            return linenumber;
        }

        public void setLinenumber(final int linenumber) {
            this.linenumber = linenumber;
        }

        @Override
        public String toString() {
            return this.output.toString();
        }
    }

    private boolean inblock = false;

    protected Object visitChildren(final SimpleNode node, final Object data) throws SieveException {
        Object jjtAccept = data;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            jjtAccept = child.jjtAccept(this, jjtAccept);
        }
        return jjtAccept;
    }

    @Override
    public Object visit(final SimpleNode node, final Object data) throws SieveException {
        return visitChildren(node, data);
    }

    @Override
    public Object visit(final ASTstart node, final Object data) throws SieveException {
        final Object visitChildren = visitChildren(node, data);
        return visitChildren;
    }

    @Override
    public Object visit(final ASTcommands node, final Object data) throws SieveException {
        if (null != data) {
            return visitChildren(node, data);
        } else {
            return visitChildren(node, new ArrayList<OwnType>(node.jjtGetNumChildren()));
        }
    }

    @Override
    public Object visit(final ASTcommand node, final Object data) throws SieveException {
        ((List<OwnType>) data).add(new OwnType(new StringBuilder((this.inblock ? "    " : "") + node.getName() + " "), node.getCoordinate().getStartLineNumber()));
        final List<OwnType> visitChildren = (List<OwnType>) visitChildren(node, data);
        final StringBuilder output = visitChildren.get(visitChildren.size() - 1).getOutput();
        // Consider variables in form of ${variable}
        if ((output.charAt(output.length() - 3) == '}' && output.charAt(output.length() - 2) == '"') || output.charAt(output.length() - 3) != '}') {
            output.append(node.getLastToken());
        }
        return visitChildren;
    }

    @Override
    public Object visit(final ASTblock node, final Object data) throws SieveException {
        final Token firstToken = node.getFirstToken();
        final Token lastToken = node.getLastToken();
        this.inblock = true;
        if (firstToken != lastToken) {
            final OwnType ownType = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1);
            ownType.getOutput().append(CRLF + firstToken);
        }
        final Object visitChildren = visitChildren(node, data);
        if (firstToken != lastToken) {
            final OwnType ownType = ((List<OwnType>) visitChildren).get(((List<OwnType>) visitChildren).size() - 1);
            ownType.getOutput().append(CRLF + lastToken + CRLF);
        }

        this.inblock = false;
        return visitChildren;
    }

    @Override
    public Object visit(final ASTarguments node, final Object data) throws SieveException {
        return visitChildren(node, data);
    }

    @Override
    public Object visit(final ASTargument node, final Object data) throws SieveException {
        final Object value = node.getValue();
        if (null != value) {
            final OwnType ownType = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1);
            ownType.getOutput().append(value + " ");
            return data;
        }
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(final ASTtest node, final Object data) throws SieveException {
        final OwnType ownType = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1);
        ownType.getOutput().append(node.getName() + " ");
        return visitChildren(node, data);
    }

    @Override
    public Object visit(final ASTtest_list node, final Object data) throws SieveException {
        final Token firstToken = node.getFirstToken();
        final Token lastToken = node.getLastToken();
        final StringBuilder output = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1).getOutput();
        if (firstToken != lastToken) {
            output.append(firstToken + " ");
        }
        Object jjtAccept = data;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            jjtAccept = child.jjtAccept(this, jjtAccept);
            output.append(", ");
        }
        output.delete(output.length() - 2, output.length());
        final Object visitChildren = jjtAccept;
        if (firstToken != lastToken) {
            output.append(lastToken + " ");
        }

        return visitChildren;
    }

    @Override
    public Object visit(final ASTstring node, final Object data) throws SieveException {
        final OwnType ownType = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1);
        final Object value = node.getValue();
        final String string = value.toString();
        if ((string.charAt(string.length() - 1) == '\"') || (string.matches("[1-9]*[0-9]"))) {
            ownType.getOutput().append(value + " ");
        } else {
            ownType.getOutput().append(value);
        }
        return data;
    }

    @Override
    public Object visit(final ASTstring_list node, final Object data) throws SieveException {
        final Token firstToken = node.getFirstToken();
        final Token lastToken = node.getLastToken();
        if (firstToken != lastToken) {
            final OwnType ownType = ((List<OwnType>) data).get(((List<OwnType>) data).size() - 1);
            ownType.getOutput().append(firstToken + " ");
        }
        Object jjtAccept = data;
        StringBuilder output = null;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            jjtAccept = child.jjtAccept(this, jjtAccept);
            output = ((List<OwnType>) jjtAccept).get(((List<OwnType>) jjtAccept).size() - 1).getOutput();
            output.append(", ");
        }
        if (null != output) {
            output.delete(output.length() - 2, output.length());
        }
        final Object visitChildren = jjtAccept;
        if (firstToken != lastToken) {
            final OwnType ownType = ((List<OwnType>) visitChildren).get(((List<OwnType>) data).size() - 1);
            ownType.getOutput().append(lastToken + " ");
        }

        return visitChildren;
    }
}
