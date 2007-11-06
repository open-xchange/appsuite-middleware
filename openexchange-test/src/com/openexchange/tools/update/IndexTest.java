package com.openexchange.tools.update;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.server.DBPool;

import junit.framework.TestCase;

public class IndexTest extends TestCase {
	
	private Context ctx = null;
	private Connection con = null;
	
	public void setUp() throws Exception {
		Init.startServer();
		ContextStorage.init();
		final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        ctx = ctxstor.getContext(contextId);
		con = DBPool.pickupWriteable(ctx);
		
		_sql_update("CREATE TABLE test_index (id int, field01 varchar(10), field02 varchar(10), fid int, PRIMARY KEY (id, field01), INDEX named_index (fid), INDEX (fid, field01), INDEX (fid, id))");
	}
	
	public void tearDown() throws Exception {
		_sql_update("DROP TABLE test_index");
		DBPool.closeWriterSilent(ctx, con);
		Init.stopServer();
	}
	
	public void testFindAllIndexes() throws Exception {
		List<Index> indexes = Index.findAllIndexes(con, "test_index");
		System.out.println(indexes);
		assertEquals(4, indexes.size());
		assertNamedIndex(indexes, "PRIMARY", "id", "field01");
		assertNamedIndex(indexes, "named_index", "fid");
		assertIndex(indexes, "fid", "field01");
		assertIndex(indexes, "fid", "id");
		
	}
	
	public void testFindIndexByName() throws Exception {
		Index id = Index.findByName(con, "test_index", "PRIMARY");
		assertNamedIndex(id, "PRIMARY", "id", "field01");
		
		id = Index.findByName(con, "test_index", "named_index");
		assertNamedIndex(id, "named_index", "fid");
		
	}
	
	public void testFindIndexContainingColumns() throws Exception {
		List<Index> indexes = Index.findContainingColumns(con, "test_index", "fid");
		assertEquals(3, indexes.size());
		assertNamedIndex(indexes, "named_index", "fid");
		assertIndex(indexes, "fid", "field01");
		assertIndex(indexes, "fid", "id");
		
		indexes = Index.findContainingColumns(con, "test_index", "fid", "id");
		assertEquals(1, indexes.size());
		assertIndex(indexes, "fid", "id");
	}
	
	public void testFindIndexWithColumns() throws Exception {
		List<Index> indexes = Index.findWithColumns(con, "test_index", "fid");
		assertEquals(1, indexes.size());
		assertNamedIndex(indexes,"named_index","fid");
		
	}
	
	public void testDropIndex() throws Exception {
		Index id = Index.findByName(con, "test_index", "named_index");
		id.drop(con);
		
		try {
			Index.findByName(con, "test_index", "named_index"); 
			fail("Didn't remove index `named_index`");
		} catch (IndexNotFoundException x) {
			assertTrue(true);
		}
	}
	
	public void testCreateIndex() throws Exception {
		Index id = new Index();
		id.setTable("test_index");
		id.setName("new index");
		id.setColumns("fid", "id", "field01");
		id.create(con);
		
		id = Index.findByName(con, "test_index", "new index");
		assertNamedIndex(id, "new index", "fid", "id", "field01");
		
	}
	
	public void _sql_update(String sql) throws Exception {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	public static void assertNamedIndex(List<Index> indexes, String name, String...columns) {
		
		for(Index id : indexes) {
			if(_isNamedIndex(id, name, columns))
				return;
		}
		fail("Index "+name+" with columns ("+ _join(columns)+") not found in list");
	}
	
	public static void assertIndex(List<Index> indexes, String...columns) {
		for(Index id : indexes) {
			if(_isIndex(id, columns))
				return;
		}
		fail("Index with columns ("+ _join(columns)+") not found in list");
	}
	
	public static void assertNamedIndex(Index id, String name, String...columns) {
		assertTrue("Index "+name+" with columns ("+ _join(columns)+") expected but got "+id.toString(), _isNamedIndex(id, name, columns));
	}
	
	public static void assertIndex(Index id, String...columns) {
		assertTrue("Index with columns ("+ _join(columns)+") expected but got "+id.toString(), _isIndex(id, columns));
	}

	public static String _join(String...strings) {
		StringBuffer b = new StringBuffer();
		for(String s : strings) { b.append(s).append(","); }
		b.setLength(b.length()-1);
		return b.toString();
	}
	
	
	public static boolean _isNamedIndex(Index id, String name, String...columns ) {
		
		if(!id.getName().equals(name)) {
			return false;
		}
		
		return _isIndex(id,columns);
	}
	
	public static boolean _isIndex(Index id, String...columns) {
		List<String> cols = id.getColumns();
		
		if(cols.size() != columns.length) {
			return false;
		}
		
		for(int i = 0; i < columns.length; i++) {
			if(!columns[i].equals(cols.get(i))) {
				return false;
			}
		}
		return true;
		
	}
	
}
