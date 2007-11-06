package com.openexchange.groupware.reminder;


import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.iterator.SearchIterator;
import java.util.Date;
import java.util.Properties;
import junit.framework.TestCase;

public class ReminderTest extends TestCase {

	private ReminderSQLInterface reminderSql = null;
	
	private static SessiondConnector sc = null;
	
	private static SessionObject sessionObj = null;
	
	protected Properties reminderProps = null;
	
	private static boolean isInit = false;
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}
		
		Init.startServer();

		sc = SessiondConnector.getInstance();
		sessionObj = sc.addSession(Init.getAJAXProperty("login"), Init.getAJAXProperty("password"), "localhost");

		isInit = true;
	}
	
	public void setUp() throws Exception {
		super.setUp();
		init();
		
		reminderSql = new ReminderHandler(sessionObj);
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        if (isInit) {
            Init.stopServer();
        }
        super.tearDown();
    }

    public void testInsert() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
	}
	
	public void testUpdate() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		reminderObj = createReminderObject(targetId, Types.TASK);
		reminderSql.updateReminder(reminderObj);
	}
	
	public void testDelete() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		reminderSql.deleteReminder(targetId, sessionObj.getUserObject().getId(), reminderObj.getModule());
	}
	
	public void testDeleteAllReminders() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		reminderSql.deleteReminder(targetId, Types.TASK);
	}

	public void testLoad() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		Date alarm = new Date(System.currentTimeMillis()-3600000);
		ReminderObject reminderObj = createReminderObject(targetId, Types.CONTACT);
		reminderObj.setDate(alarm);
		int objectId = reminderSql.insertReminder(reminderObj);
		assertTrue("object_id is not > 0", objectId > 0);
		
		reminderObj = reminderSql.loadReminder(targetId, sessionObj.getUserObject().getId(), Types.CONTACT);
		
		assertNotNull("is reminder object not null", reminderObj);
		assertEquals("targetId", targetId, Integer.parseInt(reminderObj.getTargetId()));
		assertEquals("module", Types.CONTACT, reminderObj.getModule());
		assertNotNull("date", reminderObj.getDate());
		assertEquals("userId", sessionObj.getUserObject().getId(), reminderObj.getUser());
	}
	
	public void testListReminderByTargetId() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listReminder(targetId);
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	public void testListReminderBetweenByUserId() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listModifiedReminder(sessionObj.getUserObject().getId(), new Date());
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	public void testListLastModifiedReminderUserId() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
		
		int counter = 0;
		SearchIterator it = reminderSql.listModifiedReminder(sessionObj.getUserObject().getId(), new Date(0));
		while (it.hasNext()) {
			ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}
		
		assertTrue("result > 0", counter >= 1);
	}
	
	public void testExistsReminder() throws Exception {
		int targetId = IDGenerator.getId(sessionObj.getContext(), Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date());
		reminderSql.insertReminder(reminderObj);
		assertTrue("reminder does not exists!", reminderSql.existsReminder(targetId, sessionObj.getUserObject().getId(), Types.APPOINTMENT));
		assertFalse("find invalid reminder!", reminderSql.existsReminder(targetId+1, sessionObj.getUserObject().getId(), Types.APPOINTMENT));
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

