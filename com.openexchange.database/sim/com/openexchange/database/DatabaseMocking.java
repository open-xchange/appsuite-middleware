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

package com.openexchange.database;


import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mysql.jdbc.ResultSetMetaData;

/**
 * {@link DatabaseMocking}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DatabaseMocking {
    
    private static final ConcurrentHashMap<Connection, QueryStubBuilder> queryStubBuilders = new ConcurrentHashMap<Connection, QueryStubBuilder>();
    private static final ConcurrentHashMap<Connection, QueryCollector> queryCollectors = new ConcurrentHashMap<Connection, QueryCollector>();
    
    
    public static java.sql.Connection connection() {
        Connection connection = mock(java.sql.Connection.class);
        
        QueryCollector collector = new QueryCollector();
        
        queryStubBuilders.put(connection, new QueryStubBuilder(connection, collector));
        queryCollectors.put(connection, collector);
        
        return connection;
    }
        
    public static StatementVerfificationBuilder verifyConnection(Connection connectionMock) {
        QueryCollector collector = queryCollectors.get(connectionMock);
        if (collector == null) {
            Assert.fail("Could not find query collector for connection " + connectionMock);
        }
        
        return new StatementVerfificationBuilder(collector);
    }
    
    public static QueryStubBuilder whenConnection(java.sql.Connection connectionMock) {
        return queryStubBuilders.get(connectionMock);
    }
    
    private static class QueryCollector {
        private ConcurrentHashMap<String, List<SetParameterAnswer>> queries = new ConcurrentHashMap<String, List<SetParameterAnswer>>();
        
        public void registerQuery(String query, SetParameterAnswer paramCollector) {
            List<SetParameterAnswer> list = queries.get(query);
            if (list == null) {
                list = new CopyOnWriteArrayList<SetParameterAnswer>();
                List<SetParameterAnswer> meantime = queries.putIfAbsent(query, list);
                if (meantime != null) {
                    list = meantime;
                }
            }
            
            list.add(paramCollector);
        }

        public boolean hasMatching(String query, List<Object> parameters) {
            List<SetParameterAnswer> paramSets = queries.get(query);
            if (paramSets == null || paramSets.isEmpty()) {
                return false;
            }

            for (SetParameterAnswer paramSet : paramSets) {
                boolean success = true;
                for(int i = 0, size = parameters.size(); i < size; i++) {
                    Object expected = parameters.get(i);
                    Object actual = paramSet.getParameter(i + 1);
                    
                    if (!equals(expected, actual)) {
                        success = false;
                    }
                }
                if (success) {
                    return true;
                }
            }
            
            return false;
        }

        private boolean equals(Object expected, Object actual) {
        	if (expected == actual) {
        		return true;
        	}
        	if (expected == null) {
        		return false;
        	}
        	if (actual == null) {
        		return false;
        	}
        	if (expected instanceof byte[]) {
        		if (!(actual instanceof byte[])) {
        			return false;
        		}
        		return Arrays.equals((byte[]) expected, (byte[]) actual); 
        	}
        	return expected.equals(actual);
		}

		public String dump() {
            StringBuilder b = new StringBuilder();
            for(Map.Entry<String, List<SetParameterAnswer>> entry: queries.entrySet()) {
                b.append(entry.getKey()).append(":\n");
                for(SetParameterAnswer paramSet: entry.getValue()) {
                    b.append("  ").append(paramSet.dump());
                }
                b.append("\n");
            }
            return b.toString();
        }
        
    }
    
    private static class QueryStub {
        public String query;
        public List<Object> parameters;
        public ArrayList<List<Object>> rows;
        public List<String> cols = new ArrayList<String>();
        public int numberOfUpdatedRows;
        public boolean fail;
        
        
        public QueryStub(String query) {
            this.query = query;
            this.rows = new ArrayList<List<Object>>();
            this.parameters = new ArrayList<Object>();
        }
    }
    
    private static class SetParameterAnswer implements Answer<Void> {
        private Map<Integer, Object> parameters = new HashMap<Integer, Object>();
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Object[] arguments = invocation.getArguments();
            parameters.put((Integer) arguments[0], arguments[1]);
            return null;
        }

        public String dump() {
            return parameters.toString();
        }

        public void intercept(PreparedStatement stmt) throws SQLException {
            doAnswer(this).when(stmt).setFloat(anyInt(), anyFloat());
            doAnswer(this).when(stmt).setDouble(anyInt(), anyDouble());
            doAnswer(this).when(stmt).setByte(anyInt(), anyByte());
            doAnswer(this).when(stmt).setShort(anyInt(), anyShort());
            doAnswer(this).when(stmt).setInt(anyInt(), anyInt());
            doAnswer(this).when(stmt).setLong(anyInt(), anyLong());
            doAnswer(this).when(stmt).setBoolean(anyInt(), anyBoolean());
            doAnswer(this).when(stmt).setString(anyInt(), anyString());
            doAnswer(this).when(stmt).setObject(anyInt(), anyObject());
            doAnswer(this).when(stmt).setBytes(anyInt(), any(byte[].class));
        }

        public Object getParameter(int i) {
            return parameters.get(i);
        }
        
    }
    
    
    private static class ResultSetAnswers {
        private QueryStub query;

        private int index = -1;
        
        public ResultSetAnswers(QueryStub query) {
            this.query = query;
            
        }
        
        public Answer<Boolean> next() {
            return new Answer<Boolean>() {

                @Override
                public Boolean answer(InvocationOnMock invocation) throws Throwable {
                    index++;
                    return index < query.rows.size();
                }
            };
        }
        
        public <T> Answer<T> getNamed(Class<T> type) {
            return new Answer<T>() {

                @Override
                public T answer(InvocationOnMock invocation) throws Throwable {
                    String name = (String) invocation.getArguments()[0];
                    int rowIndex = query.cols.indexOf(name);
                    if (rowIndex == -1) {
                        Assert.fail("Don't know about column " + name);
                    }
                    return (T) query.rows.get(index).get(rowIndex);
                }
            };
        }
        
        public <T> Answer<T> getIndexed(Class<T> type) {
            return new Answer<T>() {

                @Override
                public T answer(InvocationOnMock invocation) throws Throwable {
                    int rowIndex = (Integer) invocation.getArguments()[0];
                    return (T) query.rows.get(index).get(rowIndex - 1);
                }
            };
        }
        
    }
    
    
    public static class QueryStubBuilder {
        
        private Connection connection;
        private QueryStub query;
        
        private List<QueryStub> queries = new ArrayList<QueryStub>();
        
        public QueryStubBuilder(Connection connectionMock, final QueryCollector collector) {
            super();
            this.connection = connectionMock;

                        
            try {
                when(connection.prepareStatement(anyString())).thenAnswer(new Answer<PreparedStatement>() {

                    @Override
                    public PreparedStatement answer(InvocationOnMock invocation) throws Throwable {
                        final SetParameterAnswer paramCollector = new SetParameterAnswer();
                        final String query = (String) invocation.getArguments()[0];
                            
                        PreparedStatement stmt = mock(PreparedStatement.class);
                        paramCollector.intercept(stmt);
                        

                        when(stmt.executeQuery()).then(new Answer<ResultSet>() {

                            @Override
                            public ResultSet answer(InvocationOnMock invocation) throws Throwable {
                                // Find query that matches the query and parameters and return a mocked result set
                                QueryStub results = null;
                                for(QueryStub qStub: queries) {
                                    if (qStub.query.equals(query)) {
                                        boolean success = true;
                                        for (int i = 0, size = qStub.parameters.size(); i < size; i++) {
                                            Object param = qStub.parameters.get(i);
                                            Object setParam = paramCollector.getParameter(i+1);
                                            if (param instanceof byte[] && setParam instanceof byte[]) {
                                            	if (!Arrays.equals((byte[])param, (byte[])setParam)) {
                                            		success = false;
                                            		break;
                                            	}
                                            } else if (!param.equals(setParam)) {
                                                success = false;
                                                break;
                                            }
                                        }
                                        if (success) {
                                            results = qStub;                                
                                        }
                                    }
                                }
                                
                                
                                if (results == null) {
                                    Assert.fail("Could not find appropriate rows");
                                }
                                
                                if (results.fail) {
                                    throw new SQLException("Kabooom!");
                                }
                                
                                ResultSet rs = mock(ResultSet.class);
                                
                                ResultSetAnswers answers = new ResultSetAnswers(results);
                                
                                doAnswer(answers.next()).when(rs).next();
                                
                                doAnswer(answers.getNamed(float.class)).when(rs).getFloat(anyString());
                                doAnswer(answers.getNamed(double.class)).when(rs).getDouble(anyString());
                                doAnswer(answers.getNamed(byte.class)).when(rs).getByte(anyString());
                                doAnswer(answers.getNamed(short.class)).when(rs).getShort(anyString());
                                doAnswer(answers.getNamed(int.class)).when(rs).getInt(anyString());
                                doAnswer(answers.getNamed(long.class)).when(rs).getLong(anyString());
                                doAnswer(answers.getNamed(boolean.class)).when(rs).getBoolean(anyString());
                                doAnswer(answers.getNamed(String.class)).when(rs).getString(anyString());
                                doAnswer(answers.getNamed(Object.class)).when(rs).getObject(anyString());
                                
                                doAnswer(answers.getIndexed(float.class)).when(rs).getFloat(anyInt());
                                doAnswer(answers.getIndexed(double.class)).when(rs).getDouble(anyInt());
                                doAnswer(answers.getIndexed(byte.class)).when(rs).getByte(anyInt());
                                doAnswer(answers.getIndexed(short.class)).when(rs).getShort(anyInt());
                                doAnswer(answers.getIndexed(int.class)).when(rs).getInt(anyInt());
                                doAnswer(answers.getIndexed(long.class)).when(rs).getLong(anyInt());
                                doAnswer(answers.getIndexed(boolean.class)).when(rs).getBoolean(anyInt());
                                doAnswer(answers.getIndexed(String.class)).when(rs).getString(anyInt());
                                doAnswer(answers.getIndexed(Object.class)).when(rs).getObject(anyInt());
                                
                                ResultSetMetaData metaData = mock(ResultSetMetaData.class);
                                when(metaData.getColumnCount()).thenReturn(results.cols.size());
                                
                                final QueryStub query = results;
                                doAnswer(new Answer<String>() {

                                    @Override
                                    public String answer(InvocationOnMock invocation) throws Throwable {
                                        int index = (Integer) invocation.getArguments()[0];
                                        return query.cols.get(index - 1);
                                    }
                                }).when(metaData).getColumnName(anyInt());
                                
                                when(rs.getMetaData()).thenReturn(metaData);
                                
                                return rs;
                            }
                            
                        });

                        when(stmt.executeUpdate()).thenAnswer(new Answer<Integer>() {

                            @Override
                            public Integer answer(InvocationOnMock invocation) throws Throwable {
                                QueryStub results = null;
                                for(QueryStub qStub: queries) {
                                    if (qStub.query.equals(query)) {
                                        boolean success = true;
                                        for (int i = 0, size = qStub.parameters.size(); i < size; i++) {
                                            Object param = qStub.parameters.get(i);
                                            Object setParam = paramCollector.getParameter(i+1);
                                            if (!param.equals(setParam)) {
                                                success = false;
                                                break;
                                            }
                                        }
                                        if (success) {
                                            results = qStub;                                
                                        }
                                    }
                                }
                                
                                
                                if (results == null) {
                                    return 0;
                                }
                                
                                if (results.fail) {
                                    throw new SQLException("Kabooom!");
                                }

                                return results.numberOfUpdatedRows;
                            }
                        });
                        collector.registerQuery(query, paramCollector);
                        return stmt;
                    }
                    
                });
            } catch (SQLException e) {
                // Doesn't happen
            }
        }
        
        public QueryStubBuilder isQueried(final String query) {
            this.query = new QueryStub(query);
            this.queries.add(this.query);
            return this;
        }
        
        
        public QueryStubBuilder withParameter(Object parameter) {
            query.parameters.add(parameter);
            return this;
        }
        
        public QueryStubBuilder andParameter(Object parameter) {
            return withParameter(parameter);
        }

        public QueryStubBuilder thenReturnColumns(String...columns) {
            List<String> cols = new ArrayList<String>();
            for (String col : columns) {
                cols.addAll(Arrays.asList(col.split("\\s+")));
            }
            query.cols = cols;
            return this;
        }

        public QueryStubBuilder withRow(Object...rowDefinition) {
            return andRow(rowDefinition);
        }
        
        public QueryStubBuilder andRow(Object...rowDefinition) {
            query.rows.add(Arrays.asList(rowDefinition));
            return this;
        }

        public void thenReturnModifiedRows(int i) {
            query.numberOfUpdatedRows = i;
        }

        public void thenFail() {
            query.fail = true;
        }

        public void andNoRows() {
            // Syntactic Sugar to better express intent
        }
    }

    public static class StatementVerfificationBuilder {
        private QueryCollector queries;
        
        private String query;
        private List<Object> parameters = new ArrayList<Object>();
        
        
        public StatementVerfificationBuilder(QueryCollector queries) {
            this.queries = queries;
        }

        public StatementVerfificationBuilder receivedQuery(String query) {
            this.query = query;
            assertMatching();
            return this;
        }

        public StatementVerfificationBuilder withParameter(Object param) {
            return andParameter(param);
        }
        
        public StatementVerfificationBuilder andParameter(Object param) {
            parameters.add(param);
            assertMatching();
            return this;
        }
        
        private void assertMatching() {
            if (!queries.hasMatching(query, parameters)) {
                StringBuilder failureNotice = new StringBuilder("Did not receive a matching query: " + query + " with parameters: " + parameters);
                failureNotice.append("\nGot:\n\n");
                failureNotice.append(queries.dump());
                
                Assert.fail(failureNotice.toString());
            }
        }
    }
}
