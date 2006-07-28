package com.openexchange.groupware.reminder;


import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.iterator.SearchIterator;
import java.util.Date;
import junit.framework.TestCase;

public class ReminderTest extends TestCase {

	protected static String userName = "offspring";
	
	protected static String password = "netline";
	
	private static boolean isInit = false;
	
	private SessiondConnector sc = null;
	
	private ReminderSQLInterface reminderSql = null;
	
	private SessionObject sessionObj = null;
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		isInit = true;
	}
	
	public void setUp() throws Exception {
		super.setUp();
		init();
		
		sc = SessiondConnector.getInstance();
		sessionObj = sc.addSession(userName, password, "localhost");
		reminderSql = new ReminderHandler(sessionObj);
	}
	
	public void testInsert() throws Exception {
		ReminderObject reminderObj = createReminderObject(12345, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
	}
	
	public void testUpdate() throws Exception {
		ReminderObject reminderObj = createReminderObject(56789, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		reminderObj = createReminderObject(56789, Types.TASK);
		reminderSql.updateReminder(reminderObj);
	}
	
	public void testDelete() throws Exception {
		ReminderObject reminderObj = createReminderObject(56789, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		reminderObj = createReminderObject(56789, Types.TASK);
		reminderSql.deleteReminder(56789, sessionObj.getUserObject().getId());
	}
	
	public void testLoad() throws Exception {
		Date alarm = new Date(System.currentTimeMillis()-3600000);
		ReminderObject reminderObj = createReminderObject(11111, Types.CONTACT);
		reminderObj.setDate(alarm);
		reminderSql.insertReminder(reminderObj);
		
		reminderObj = reminderSql.loadReminder(sessionObj.getUserObject().getId(), 11111);
		
		assertNotNull("is reminder object not null", reminderObj);
		assertEquals("targetId", 11111, reminderObj.getTargetId());
		assertEquals("module", 11111, reminderObj.getTargetId());
		assertEquals("date", alarm.getTime(), reminderObj.getDate().getTime());
		assertEquals("userId", sessionObj.getUserObject().getId(), reminderObj.getUser());
	}
	
	public void testListReminderByTargetId() throws Exception {
		ReminderObject reminderObj = createReminderObject(22222, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listReminder(22222);
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	public void testListReminderBetweenByUserId() throws Exception {
		ReminderObject reminderObj = createReminderObject(33333, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listReminder(sessionObj.getUserObject().getId(), new Date(0), new Date());
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	public void testListLastModifiedReminderUserId() throws Exception {
		ReminderObject reminderObj = createReminderObject(44444, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listReminder(sessionObj.getUserObject().getId(), new Date());
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	protected ReminderObject createReminderObject(int targetId, int module) {
		ReminderObject reminderObj = new ReminderObject();
		reminderObj.setUser(sessionObj.getUserObject().getId());
		reminderObj.setModule(module);
		reminderObj.setTargetId(targetId);
		reminderObj.setDate(new Date());
		
		return reminderObj;
	}
}

