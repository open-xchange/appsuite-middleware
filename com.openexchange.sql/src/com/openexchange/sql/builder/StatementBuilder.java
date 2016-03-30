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

package com.openexchange.sql.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.openexchange.sql.grammar.ABS;
import com.openexchange.sql.grammar.ALL;
import com.openexchange.sql.grammar.AND;
import com.openexchange.sql.grammar.ANY;
import com.openexchange.sql.grammar.AVG;
import com.openexchange.sql.grammar.Assignment;
import com.openexchange.sql.grammar.BETWEEN;
import com.openexchange.sql.grammar.BinaryArithmeticExpression;
import com.openexchange.sql.grammar.BinaryBitFunction;
import com.openexchange.sql.grammar.BinaryFunction;
import com.openexchange.sql.grammar.BinaryPredicate;
import com.openexchange.sql.grammar.BitAND;
import com.openexchange.sql.grammar.BitLSHIFT;
import com.openexchange.sql.grammar.BitOR;
import com.openexchange.sql.grammar.BitRSHIFT;
import com.openexchange.sql.grammar.BitXOR;
import com.openexchange.sql.grammar.CONCAT;
import com.openexchange.sql.grammar.COUNT;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.Condition;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.DISTINCT;
import com.openexchange.sql.grammar.DIVIDE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.EXISTS;
import com.openexchange.sql.grammar.Element;
import com.openexchange.sql.grammar.Expression;
import com.openexchange.sql.grammar.FROM;
import com.openexchange.sql.grammar.GREATER;
import com.openexchange.sql.grammar.GREATEROREQUAL;
import com.openexchange.sql.grammar.GROUPBY;
import com.openexchange.sql.grammar.GenericFunction;
import com.openexchange.sql.grammar.HAVING;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.INTO;
import com.openexchange.sql.grammar.INVERT;
import com.openexchange.sql.grammar.ISNULL;
import com.openexchange.sql.grammar.Join;
import com.openexchange.sql.grammar.LENGTH;
import com.openexchange.sql.grammar.LIKE;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.sql.grammar.LOCATE;
import com.openexchange.sql.grammar.LeftOuterJoin;
import com.openexchange.sql.grammar.MAX;
import com.openexchange.sql.grammar.MIN;
import com.openexchange.sql.grammar.MINUS;
import com.openexchange.sql.grammar.NOT;
import com.openexchange.sql.grammar.NOTEQUALS;
import com.openexchange.sql.grammar.NOTEXISTS;
import com.openexchange.sql.grammar.NOTIN;
import com.openexchange.sql.grammar.NOTLIKE;
import com.openexchange.sql.grammar.NOTNULL;
import com.openexchange.sql.grammar.ON;
import com.openexchange.sql.grammar.OR;
import com.openexchange.sql.grammar.ORDERBY;
import com.openexchange.sql.grammar.Operator;
import com.openexchange.sql.grammar.PLUS;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.SMALLER;
import com.openexchange.sql.grammar.SMALLEROREQUAL;
import com.openexchange.sql.grammar.SQRT;
import com.openexchange.sql.grammar.SUBSTRING;
import com.openexchange.sql.grammar.SUM;
import com.openexchange.sql.grammar.TIMES;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.TernaryFunction;
import com.openexchange.sql.grammar.TernaryPredicate;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.sql.grammar.UnaryArithmeticExpression;
import com.openexchange.sql.grammar.UnaryBitFunction;
import com.openexchange.sql.grammar.UnaryFunction;
import com.openexchange.sql.grammar.UnaryMINUS;
import com.openexchange.sql.grammar.UnaryPLUS;
import com.openexchange.sql.grammar.WHERE;
import com.openexchange.sql.tools.SQLTools;

public class StatementBuilder implements IStatementBuilder {
	protected StringBuffer fStringBuffer;
	protected Command fCommand;
	protected boolean upperCase = false;
	protected PreparedStatement stmt = null;

	public StatementBuilder() {
		super();
	}

	public StatementBuilder(boolean upperCase) {
        this();
	    this.upperCase = upperCase;
	}

	public String getStatement() {
		return fStringBuffer.toString();
	}

	protected void append(String s) {
		fStringBuffer.append(upperCase ? s.toUpperCase() : s);
	}

	protected void appendList(List<String> list) {
		for (Iterator<String> elementIt = list.iterator(); elementIt.hasNext();) {
			append(elementIt.next());
			if (elementIt.hasNext()) {
				append(", ");
			}
		}
	}

	protected void blank() {
		fStringBuffer.append(' ');
	}

	protected void reset() {
		fStringBuffer = new StringBuffer();
	}

	/*
	 * Commands
	 */
	@Override
    public String buildCommand(Command element) {
		fCommand = element;
		reset();
		element.build(this);
		return getStatement();
	}

	@Override
    public PreparedStatement prepareStatement(Connection con, Command element, List<? extends Object> values) throws SQLException {
	    String command = buildCommand(element);
	    PreparedStatement statement = con.prepareStatement(command);
	    for (int i = 0; i < values.size(); i++) {
	        statement.setObject(i+1, values.get(i));
	    }
	    return statement;
	}

	public int executeStatement(Connection con, Command element, List<? extends Object> values) throws SQLException {
	    PreparedStatement stmt = null;
	    try {
	        stmt = prepareStatement(con, element, values);
	        return stmt.executeUpdate();
	    } finally {
	        SQLTools.closeSQLStuff(null, stmt, null);
	    }
	}

	public ResultSet executeQuery(Connection con, Command element, List<? extends Object> values) throws SQLException {
	    stmt = prepareStatement(con, element, values);
	    return stmt.executeQuery();
	}

	/**
	 * Closes the underlying PreparedStatment and also the given Connection and ResultSet in correct order.
	 * Just set Connection or ResultSet to null, if no closing of them is needed.
	 * @param con
	 * @param rs
	 * @throws SQLException
	 */
	public void closePreparedStatement(Connection con, ResultSet rs) throws SQLException {
	    SQLTools.closeSQLStuff(con, stmt, rs);
	}

	@Override
    public void buildDELETE(DELETE delete) {
		append("DELETE");
		delete.getFrom().build(this);
		if (delete.getWhere() != null) {
            delete.getWhere().build(this);
        }
	}

	@Override
    public void buildINSERT(INSERT insert) {
		append("INSERT");
		insert.getInto().build(this);
		if (insert.getSubSelect() != null) {
		    blank();
		    buildSELECT(insert.getSubSelect());
		} else {
    		append(" (");
    		appendList(insert.getColumns());
    		append(") VALUES ");
    		buildINSERTValues(insert);
		}
	}

	protected void buildINSERTValues(INSERT insert) {
        for (Iterator<List<Expression>> expressionLists = insert.getValues().iterator(); expressionLists.hasNext();) {
            append("(");
            buildElementList(expressionLists.next());
            append(")");
            if (expressionLists.hasNext()) {
                append(", ");
            }
        }
	}

	@Override
    public void buildSELECT(SELECT select) {
		append("SELECT ");
		if (select.getExpression() != null) {
		    select.getExpression().build(this);
		} else {
		    appendList(select.getColumns());
		}
		select.getFrom().build(this);
		if (select.getWhere() != null) {
            select.getWhere().build(this);
        }
	}

	@Override
    public void buildUPDATE(UPDATE update) {
		append("UPDATE ");
		append(update.getTableName());
		append(" SET ");
		buildElementList(update.getAssignments());
		if (update.getWhere() != null) {
            update.getWhere().build(this);
        }
	}

	/*
	 * Clauses
	 */
	@Override
    public void buildDISTINCT(DISTINCT element) {
	}

	@Override
    public void buildFROM(FROM from) {
		append(" FROM ");
		buildElementList(from.getTables());
	}

	@Override
    public void buildGROUPBY(GROUPBY element) {
	}

	@Override
    public void buildHAVING(HAVING element) {
	}

	@Override
    public void buildINTO(INTO element) {
		append(" INTO ");
		append(element.getTableName());
	}

	@Override
    public void buildON(ON on) {
		append(" ON ");
		on.getPredicate().build(this);
	}

	@Override
    public void buildORDERBY(ORDERBY element) {
	}

	@Override
    public void buildWHERE(WHERE where) {
		append(" WHERE ");
		where.getPredicate().build(this);
	}

	/*
	 * Named Values
	 */
	@Override
    public void buildColumn(Column column) {
		append(column.getName());
	}

	@Override
    public void buildConstant(Constant constant) {
		if (constant == Constant.PLACEHOLDER) {
            append("?");
        } else if (constant == Constant.ASTERISK) {
            append("*");
        } else if (constant.getValue() == null) {
            append("NULL");
        } else {
			Object value = constant.getValue();
			if (value instanceof String || value instanceof Character
					|| value instanceof Time || value instanceof Date
					|| value instanceof Timestamp) {
				append("'");
				append(value.toString());
				append("'");
			} else {
				append(value.toString());
			}
		}
	}

	@Override
    public void buildTable(Table table) {
		append(table.getName());
		if (table.getAlias() != null) {
		    blank();
		    append(table.getAlias());
		}
	}

	@Override
    public void buildJoin(Join join) {
		append("(");
		join.getLeftTable().build(this);
		append(" JOIN ");
		join.getRightTable().build(this);
		join.getON().build(this);
		append(")");
	}

	@Override
    public void buildLeftOuterJoin(LeftOuterJoin join) {
		append("(");
		join.getLeftTable().build(this);
		append(" LEFT OUTER JOIN ");
		join.getRightTable().build(this);
		join.getON().build(this);
		append(")");
	}

	@Override
    public void buildAssignment(Assignment element) {
		element.getLeftExpression().build(this);
		blank();
		append(element.getSqlKeyword());
		blank();
		element.getRightExpression().build(this);
	}

	/*
	 * Lists
	 */
	protected void buildElementList(List<? extends Element> elements) {
		for (Iterator<? extends Element> elementIt = elements.iterator(); elementIt
				.hasNext();) {
			elementIt.next().build(this);
			if (elementIt.hasNext()) {
				append(", ");
			}
		}
	}

	/*
	 * Conditions
	 */
	protected void buildCondition(Condition condition) {
		append("(");
		condition.getLeftPredicate().build(this);
		blank();
		append(condition.getSqlKeyword());
		blank();
		condition.getRightPredicate().build(this);
		append(")");
	}

	@Override
    public void buildAND(AND and) {
		buildCondition(and);
	}

	@Override
    public void buildOR(OR or) {
		buildCondition(or);
	}

	/*
	 * Predicates
	 */
	protected void buildBinaryPredicate(BinaryPredicate element) {
		element.getLeftExpression().build(this);
		blank();
		append(element.getSqlKeyword());
		blank();
		element.getRightExpression().build(this);
	}

	protected void buildTernaryPredicate(TernaryPredicate predicate) {
		predicate.getLeftExpression().build(this);
		blank();
		append(predicate.getSqlKeyword());
		blank();
		predicate.getMiddleExpression().build(this);
		if (predicate.getRightExpression() != null) {
			blank();
			append(predicate.getSecondSqlKeyword());
			blank();
			predicate.getRightExpression().build(this);
		}
	}

	/*
	 * Unary Predicates
	 */
	@Override
    public void buildISNULL(ISNULL element) {
		element.getExpression().build(this);
		blank();
		append(element.getSqlKeyword());
	}

	@Override
    public void buildNOTNULL(NOTNULL element) {
		element.getExpression().build(this);
		blank();
		append(element.getSqlKeyword());
	}

	@Override
    public void buildNOT(NOT element) {
		append(element.getSqlKeyword());
		append("(");
		element.getExpression().build(this);
		append(")");
	}

	/*
	 * Binary Predicates
	 */
	@Override
    public void buildEQUALS(EQUALS element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildGREATER(GREATER element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildGREATEROREQUAL(GREATEROREQUAL element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildIN(IN element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildNOTEQUALS(NOTEQUALS element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildNOTIN(NOTIN element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildSMALLER(SMALLER element) {
		buildBinaryPredicate(element);
	}

	@Override
    public void buildSMALLEROREQUAL(SMALLEROREQUAL element) {
		buildBinaryPredicate(element);
	}

	/*
	 * Ternary Predicates
	 */
	@Override
    public void buildBETWEEN(BETWEEN element) {
		buildTernaryPredicate(element);
	}

	@Override
    public void buildLIKE(LIKE element) {
		buildTernaryPredicate(element);
	}

	@Override
    public void buildNOTLIKE(NOTLIKE element) {
		buildTernaryPredicate(element);
	}

	/*
	 * Operators
	 */
	public void buildOperator(Operator operator) {
		append(operator.getSqlKeyword());
		append("(");
		operator.getExpression().build(this);
		append(")");
	}

	@Override
    public void buildALL(ALL element) {
		buildOperator(element);
	}

	@Override
    public void buildANY(ANY element) {
		buildOperator(element);
	}

	/*
	 * Existence
	 */
	@Override
    public void buildEXISTS(EXISTS element) {
		append(element.getSqlKeyword());
		append("(");
		element.getSelect().build(this);
		append(")");

	}

	@Override
    public void buildNOTEXISTS(NOTEXISTS element) {
		append(element.getSqlKeyword());
		append("(");
		element.getSelect().build(this);
		append(")");
	}

	/*
	 * Arithmetic Expressions
	 */
	protected void buildUnaryArithmeticExpression(
			UnaryArithmeticExpression expression) {
		append(expression.getSqlKeyword());
		expression.getExpression().build(this);
	}

	protected void buildBinaryArithmeticExpression(
			BinaryArithmeticExpression expression) {
		append("(");
		expression.getLeftExpression().build(this);
		blank();
		append(expression.getSqlKeyword());
		blank();
		expression.getRightExpression().build(this);
		append(")");
	}

	/*
	 * Unary Arithmetic Expressions
	 */
	@Override
    public void buildUnaryMINUS(UnaryMINUS element) {
		buildUnaryArithmeticExpression(element);
	}

	@Override
    public void buildUnaryPLUS(UnaryPLUS element) {
		buildUnaryArithmeticExpression(element);
	}

	/*
	 * Binary Arithmetic Expressions
	 */
	@Override
    public void buildDIVIDE(DIVIDE element) {
		buildBinaryArithmeticExpression(element);
	}

	@Override
    public void buildMINUS(MINUS element) {
		buildBinaryArithmeticExpression(element);
	}

	@Override
    public void buildPLUS(PLUS element) {
		buildBinaryArithmeticExpression(element);
	}

	@Override
    public void buildTIMES(TIMES element) {
		buildBinaryArithmeticExpression(element);
	}

    // Bit Functions

    protected void buildUnaryBitFunction(UnaryBitFunction function) {
        append(function.getSqlKeyword());
        function.getExpression().build(this);
    }

    protected void buildBinaryBitFunction(BinaryBitFunction function) {
        append("(");
        function.getLeftExpression().build(this);
        blank();
        append(function.getSqlKeyword());
        blank();
        function.getRightExpression().build(this);
        append(")");
    }

    @Override
    public void buildBitAND(BitAND function) {
        buildBinaryBitFunction(function);
    }

    @Override
    public void buildBitLSHIFT(BitLSHIFT function) {
        buildBinaryBitFunction(function);
    }

    @Override
    public void buildBitOR(BitOR function) {
        buildBinaryBitFunction(function);
    }

    @Override
    public void buildBitRSHIFT(BitRSHIFT function) {
        buildBinaryBitFunction(function);
    }

    @Override
    public void buildBitXOR(BitXOR function) {
        buildBinaryBitFunction(function);
    }

    @Override
    public void buildINVERT(INVERT function) {
        buildUnaryBitFunction(function);
    }

	/*
	 * Functions
	 */
	@Override
    public void buildGenericFunction(GenericFunction element) {
		append(element.getSqlKeyword());
		append("(");
		buildElementList(Arrays.asList(element.getArguments()));
		append(")");
	}

	protected void buildUnaryFunction(UnaryFunction function) {
		append(function.getSqlKeyword());
		append("(");
		function.getExpression().build(this);
		append(")");
	}

	protected void buildBinaryFunction(BinaryFunction function) {
		append(function.getSqlKeyword());
		append("(");
		function.getLeftExpression().build(this);
		append(",");
		function.getRightExpression().build(this);
		append(")");
	}

	protected void buildTernaryFunction(TernaryFunction function) {
		append(function.getSqlKeyword());
		append("(");
		function.getLeftExpression().build(this);
		append(",");
		function.getMiddleExpression().build(this);
		if (function.getRightExpression() != null) {
			append(",");
			function.getRightExpression().build(this);
		}
		append(")");
	}

	/*
	 * Unary Functions
	 */
	@Override
    public void buildABS(ABS element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildAVG(AVG element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildCOUNT(COUNT element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildLENGTH(LENGTH element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildMAX(MAX element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildMIN(MIN element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildSQRT(SQRT element) {
		buildUnaryFunction(element);
	}

	@Override
    public void buildSUM(SUM element) {
		buildUnaryFunction(element);
	}

	/*
	 * Binary Functions
	 */
	@Override
    public void buildCONCAT(CONCAT element) {
		buildBinaryFunction(element);
	}

	/*
	 * Ternary Functions
	 */
	@Override
    public void buildLOCATE(LOCATE element) {
		buildTernaryFunction(element);
	}

	@Override
    public void buildSUBSTRING(SUBSTRING element) {
		buildTernaryFunction(element);
	}

    @Override
    public void buildList(LIST element) {
        append("(");
        for (Iterator<Expression> iter = element.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            expression.build(this);
            if (iter.hasNext()) {
                append(", ");
            }
        }
        append(")");
    }
}
