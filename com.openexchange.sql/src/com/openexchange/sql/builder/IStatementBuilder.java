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
import java.sql.SQLException;
import java.util.List;
import com.openexchange.sql.grammar.ABS;
import com.openexchange.sql.grammar.ALL;
import com.openexchange.sql.grammar.AND;
import com.openexchange.sql.grammar.ANY;
import com.openexchange.sql.grammar.AVG;
import com.openexchange.sql.grammar.Assignment;
import com.openexchange.sql.grammar.BETWEEN;
import com.openexchange.sql.grammar.BitAND;
import com.openexchange.sql.grammar.BitLSHIFT;
import com.openexchange.sql.grammar.BitOR;
import com.openexchange.sql.grammar.BitRSHIFT;
import com.openexchange.sql.grammar.BitXOR;
import com.openexchange.sql.grammar.CONCAT;
import com.openexchange.sql.grammar.COUNT;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.DISTINCT;
import com.openexchange.sql.grammar.DIVIDE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.EXISTS;
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
import com.openexchange.sql.grammar.PLUS;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.SMALLER;
import com.openexchange.sql.grammar.SMALLEROREQUAL;
import com.openexchange.sql.grammar.SQRT;
import com.openexchange.sql.grammar.SUBSTRING;
import com.openexchange.sql.grammar.SUM;
import com.openexchange.sql.grammar.TIMES;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.sql.grammar.UnaryMINUS;
import com.openexchange.sql.grammar.UnaryPLUS;
import com.openexchange.sql.grammar.WHERE;

public interface IStatementBuilder {

	String buildCommand(Command element);

	PreparedStatement prepareStatement(Connection con, Command element, List<? extends Object> values) throws SQLException;

	void buildABS(ABS element);

	void buildALL(ALL element);

	void buildAND(AND and);

	void buildANY(ANY element);

	void buildAssignment(Assignment list);

	void buildAVG(AVG element);

	void buildBETWEEN(BETWEEN element);

	void buildColumn(Column element);

	void buildConstant(Constant element);

	void buildCONCAT(CONCAT element);

	void buildCOUNT(COUNT element);

	void buildDELETE(DELETE delete);

	void buildDISTINCT(DISTINCT element);

	void buildDIVIDE(DIVIDE element);

	void buildEQUALS(EQUALS element);

	void buildEXISTS(EXISTS element);

	void buildFROM(FROM from);

	void buildGenericFunction(GenericFunction element);

	void buildGREATER(GREATER element);

	void buildGREATEROREQUAL(GREATEROREQUAL element);

	void buildGROUPBY(GROUPBY element);

	void buildHAVING(HAVING element);

	void buildIN(IN element);

	void buildINSERT(INSERT insert);

	void buildINTO(INTO element);

	void buildISNULL(ISNULL element);

	void buildLeftOuterJoin(LeftOuterJoin join);

	void buildLIKE(LIKE element);

	void buildLENGTH(LENGTH element);

	void buildLOCATE(LOCATE element);

	void buildMAX(MAX element);

	void buildMIN(MIN element);

	void buildMINUS(MINUS element);

	void buildNOT(NOT element);

	void buildNOTEQUALS(NOTEQUALS element);

	void buildNOTEXISTS(NOTEXISTS element);

	void buildNOTIN(NOTIN element);

	void buildNOTLIKE(NOTLIKE element);

	void buildNOTNULL(NOTNULL element);

	void buildON(ON on);

	void buildOR(OR and);

	void buildORDERBY(ORDERBY element);

	void buildPLUS(PLUS element);

	void buildSELECT(SELECT select);

	void buildSMALLER(SMALLER element);

	void buildSMALLEROREQUAL(SMALLEROREQUAL element);

	void buildSQRT(SQRT element);

	void buildSUBSTRING(SUBSTRING element);

	void buildSUM(SUM element);

	void buildTable(Table table);

	void buildJoin(Join join);

	void buildTIMES(TIMES element);

	void buildUnaryMINUS(UnaryMINUS element);

	void buildUnaryPLUS(UnaryPLUS element);

	void buildUPDATE(UPDATE insert);

	void buildWHERE(WHERE where);

    void buildList(LIST element);

    void buildBitAND(BitAND bitAND);

    void buildBitLSHIFT(BitLSHIFT bitLSHIFT);

    void buildBitOR(BitOR bitOR);

    void buildBitRSHIFT(BitRSHIFT bitRSHIFT);

    void buildBitXOR(BitXOR bitXOR);

    void buildINVERT(INVERT invert);
}
