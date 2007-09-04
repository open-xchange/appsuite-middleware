package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.update.tasks.InfostoreRenamePersonalInfostoreFolders;
import com.openexchange.server.DBPoolingException;

import junit.framework.TestCase;

public class RenamePersonalInfostoreFoldersTest extends TestCase {

	private Schema schema = null;
	private UpdateTask updateTask = null;
	private int existing_ctx_id = 0;
	
	private int user_id = -1;
	
	private int parent = FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
	private int module = FolderObject.INFOSTORE;
	private int type = FolderObject.PUBLIC;
	
	private final int start_ids = 2000;
	
	private int idcount = start_ids;
	
	public void setUp() throws Exception {
		Init.initDB();
		Init.loadSystemProperties();
		ContextStorage.init();
		existing_ctx_id = ContextStorage.getInstance().getContextId("defaultcontext");
		
		schema = SchemaStore.getInstance(SchemaStoreImpl.class.getName()).getSchema(existing_ctx_id);
		updateTask = new InfostoreRenamePersonalInfostoreFolders();
		
		user_id = ContextStorage.getInstance().getContext(existing_ctx_id).getMailadmin();
		
	}
	
	public void tearDown() throws Exception {
		exec("DELETE FROM oxfolder_tree WHERE cid = ? and fuid >= ?", existing_ctx_id, start_ids);
		Init.stopDB();
	}
	
	public void createNameCollisions() throws DBPoolingException, SQLException {
		for(int i = 0; i < 3; i++) { createFolder("test test"); }
		createFolder("normal folder");
		for(int i = 0; i < 2; i++) { createFolder("test2 test2"); }
	}
		
	public void createSneakyNameCollision() throws DBPoolingException, SQLException {
		for(int i = 0; i < 3; i++) { createFolder("test test"); }
		createFolder("test test (2)");
	}
	
	public void createMany() throws DBPoolingException, SQLException{
		for(int i = 0; i < 4000; i++) {
			for(int j = 0; j < 3; j++) {
				createFolder("test test "+i);
			}
		}
	}
	
	public void testFixSchema() throws AbstractOXException, SQLException{
		createNameCollisions();
		updateTask.perform(schema, existing_ctx_id);
		assertNoCollisions();
		assertFolderNames("test test", "test test (1)", "test test (2)", "normal folder", "test2 test2", "test2 test2 (1)");
	}
	
	public void testNameCollision() throws SQLException, AbstractOXException{
		createSneakyNameCollision();
		updateTask.perform(schema, existing_ctx_id);
		assertNoCollisions();
		assertFolderNames("test test", "test test (1)", "test test (2)", "test test (3)");
		
	}
	
	public void testRunMultipleTimesNonDestructively() throws AbstractOXException, SQLException{
		createNameCollisions();
		updateTask.perform(schema, existing_ctx_id);
		updateTask.perform(schema, existing_ctx_id);
		updateTask.perform(schema, existing_ctx_id);
		updateTask.perform(schema, existing_ctx_id);
		updateTask.perform(schema, existing_ctx_id);
		updateTask.perform(schema, existing_ctx_id);
		assertNoCollisions();
		assertFolderNames("test test", "test test (1)", "test test (2)", "normal folder", "test2 test2", "test2 test2 (1)");
	}
	
	public void notestManyManyMany() throws AbstractOXException, SQLException{
		createMany();
		long start = System.currentTimeMillis();
		updateTask.perform(schema, existing_ctx_id);
		long stop = System.currentTimeMillis();
		System.out.println(stop-start);
	}
	
	
	public void assertNoCollisions() throws DBPoolingException, SQLException {
		assertEquals(0, countCollisions());
	}
	
	public void assertFolderNames(String...expected) throws DBPoolingException, SQLException {
		List<String> folders = loadFolderNames();
		assertEquals(folders.toString(), expected.length, folders.size());
		
		for(String fname : expected) {
			assertTrue(fname+" not found", folders.remove(fname));
		}
	}
	
	
	private final void createFolder(String fname) throws DBPoolingException, SQLException {
		exec("INSERT INTO oxfolder_tree (fuid, cid, parent, fname, module, type, default_flag,creating_date,changing_date,created_from, changed_from, permission_flag, subfolder_flag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",idcount++, existing_ctx_id, parent, fname, module, type, 1, 0, 0, user_id, user_id,3,0);
	}
	
	private int countCollisions() throws DBPoolingException, SQLException{
		
		Set<String> seen = new HashSet<String>();
		List<String> folderNames = loadFolderNames();
		int collisionCount = 0;
		
		for(String fname : folderNames) {
			if(!seen.add(fname)) {
				collisionCount++;
			}
		}
		return collisionCount;
	}
	
	private List<String> loadFolderNames() throws DBPoolingException, SQLException{
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		List<String> names = new ArrayList<String>();
		try {
			con = Database.get(existing_ctx_id, true);
			stmt = con.prepareStatement("SELECT fname FROM oxfolder_tree WHERE cid = ? and parent = ? and module = ? and fuid >= ?");
			stmt.setInt(1,existing_ctx_id);
			stmt.setInt(2,parent);
			stmt.setInt(3, module);
			stmt.setInt(4, start_ids);
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				names.add(rs.getString(1));
			}
		} finally {
			if(null != rs) {
				rs.close();
			}
			
			if(null != stmt) {
				stmt.close();
			}
			Database.back(existing_ctx_id, true, con);
		}
		return names;
	}
	
	private final void exec(String sql, Object...args) throws DBPoolingException, SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		
		try {
			con = Database.get(existing_ctx_id, true);
			stmt = con.prepareStatement(sql);
			int count = 1;
			for(Object o : args) {
				stmt.setObject(count++,o);
			}
			
			stmt.executeUpdate();
		} finally {
			
			if(null != stmt) {
				stmt.close();
			}
			Database.back(existing_ctx_id, true, con);
		}
	}
	
	
}
