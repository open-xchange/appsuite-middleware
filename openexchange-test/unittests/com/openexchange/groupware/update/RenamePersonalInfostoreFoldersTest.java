package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.tasks.InfostoreRenamePersonalInfostoreFolders;

public class RenamePersonalInfostoreFoldersTest extends UpdateTest {

    private UpdateTask updateTask = null;

    private final int parent = FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
	private final int module = FolderObject.INFOSTORE;
	private final int type = FolderObject.PUBLIC;

	private final int start_ids = 2000;

	private int idcount = start_ids;

    @Override
	public void setUp() throws Exception {
        super.setUp();
        updateTask = new InfostoreRenamePersonalInfostoreFolders();

    }

    @Override
	public void tearDown() throws Exception {
        exec("DELETE FROM oxfolder_tree WHERE cid = ? and fuid >= ?", existing_ctx_id, start_ids);
        super.tearDown();
    }

    public void createNameCollisions() throws OXException, SQLException {
		for(int i = 0; i < 3; i++) { createFolder("test test"); }
		createFolder("normal folder");
		for(int i = 0; i < 2; i++) { createFolder("test2 test2"); }
	}

	public void createSneakyNameCollision() throws OXException, SQLException {
		for(int i = 0; i < 3; i++) { createFolder("test test"); }
		createFolder("test test (2)");
	}

	public void createMany() throws OXException, SQLException{
		for(int i = 0; i < 4000; i++) {
			for(int j = 0; j < 3; j++) {
				createFolder("test test "+i);
			}
		}
	}

	public void testFixSchema() throws OXException, SQLException{
		createNameCollisions();
		updateTask.perform(schema, existing_ctx_id);
		assertNoCollisions();
		assertFolderNames("test test", "test test (1)", "test test (2)", "normal folder", "test2 test2", "test2 test2 (1)");
	}

	public void testNameCollision() throws SQLException, OXException{
		createSneakyNameCollision();
		updateTask.perform(schema, existing_ctx_id);
		assertNoCollisions();
		assertFolderNames("test test", "test test (1)", "test test (2)", "test test (3)");

	}

	public void testRunMultipleTimesNonDestructively() throws OXException, SQLException{
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

	public void notestManyManyMany() throws OXException, SQLException{
		createMany();
		updateTask.perform(schema, existing_ctx_id);
	}


	public void assertNoCollisions() throws OXException, SQLException {
		assertEquals(0, countCollisions());
	}

	public void assertFolderNames(final String...expected) throws OXException, SQLException {
		final List<String> folders = loadFolderNames();
		assertEquals(folders.toString(), expected.length, folders.size());

		for(final String fname : expected) {
			assertTrue(fname+" not found", folders.remove(fname));
		}
	}


	private final void createFolder(final String fname) throws OXException, SQLException {
		exec("INSERT INTO oxfolder_tree (fuid, cid, parent, fname, module, type, default_flag,creating_date,changing_date,created_from, changed_from, permission_flag, subfolder_flag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",idcount++, existing_ctx_id, parent, fname, module, type, 1, 0, 0, user_id, user_id,3,0);
	}

	private int countCollisions() throws OXException, SQLException{

		final Set<String> seen = new HashSet<String>();
		final List<String> folderNames = loadFolderNames();
		int collisionCount = 0;

		for(final String fname : folderNames) {
			if(!seen.add(fname)) {
				collisionCount++;
			}
		}
		return collisionCount;
	}

	private List<String> loadFolderNames() throws OXException, SQLException{
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		final List<String> names = new ArrayList<String>();
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
			Database.backAfterReading(existing_ctx_id, con);
		}
		return names;
	}


}
