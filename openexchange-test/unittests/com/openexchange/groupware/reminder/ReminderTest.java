package com.openexchange.groupware.reminder;


import java.util.Date;
import java.util.Properties;
import com.openexchange.api2.ReminderService;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.tools.iterator.SearchIterator;
import junit.framework.TestCase;

public class ReminderTest extends TestCase {

	private ReminderService reminderSql = null;

	protected Properties reminderProps = null;

	private static boolean isInit = false;

	private final int userId = -1;

	private Context context = null;

	public void init() throws Exception {
		if (isInit) {
			return ;
		}

		Init.startServer();

		isInit = true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		init();

		final TestConfig config = new TestConfig();
		final int contextId = ContextStorage.getInstance().getContextId(config.getContextName());
		context = ContextStorage.getInstance().getContext(contextId);

		reminderSql = new ReminderHandler(context);
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
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);
	}

	public void testUpdate() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		reminderObj = createReminderObject(targetId, Types.TASK);
		reminderSql.updateReminder(reminderObj);
	}

	public void testDelete() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		reminderSql.deleteReminder(targetId, userId, reminderObj.getModule());
	}

	public void testDeleteAllReminders() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		reminderSql.deleteReminder(targetId, Types.TASK);
	}

	public void testLoad() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final Date alarm = new Date(System.currentTimeMillis()-3600000);
		ReminderObject reminderObj = createReminderObject(targetId, Types.CONTACT);
		reminderObj.setDate(alarm);
		final int objectId = reminderSql.insertReminder(reminderObj);
		assertTrue("object_id is not > 0", objectId > 0);

		reminderObj = reminderSql.loadReminder(targetId, userId, Types.CONTACT);

		assertNotNull("is reminder object not null", reminderObj);
		assertEquals("targetId", targetId, reminderObj.getTargetId());
		assertEquals("module", Types.CONTACT, reminderObj.getModule());
		assertNotNull("date", reminderObj.getDate());
		assertEquals("userId", userId, reminderObj.getUser());
	}

	public void testListReminderByTargetId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		int counter = 0;
		final SearchIterator it = reminderSql.listReminder(Types.APPOINTMENT, targetId);
		while (it.hasNext()) {
			final ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

	public void testListReminderBetweenByUserId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		int counter = 0;
		final SearchIterator it = reminderSql.listModifiedReminder(userId, new Date());
		while (it.hasNext()) {
			final ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

	public void testListLastModifiedReminderUserId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj);

		int counter = 0;
		final SearchIterator it = reminderSql.listModifiedReminder(userId, new Date(0));
		while (it.hasNext()) {
			final ReminderObject r = (ReminderObject)it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

	public void testExistsReminder() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date());
		reminderSql.insertReminder(reminderObj);
		assertTrue("reminder does not exists!", reminderSql.existsReminder(targetId, userId, Types.APPOINTMENT));
		assertFalse("find invalid reminder!", reminderSql.existsReminder(targetId+1, userId, Types.APPOINTMENT));
	}

	protected ReminderObject createReminderObject(final int targetId, final int module) {
		final ReminderObject reminderObj = new ReminderObject();
		reminderObj.setUser(userId);
		reminderObj.setModule(module);
		reminderObj.setTargetId(targetId);
		reminderObj.setDate(new Date());

		return reminderObj;
	}
}

