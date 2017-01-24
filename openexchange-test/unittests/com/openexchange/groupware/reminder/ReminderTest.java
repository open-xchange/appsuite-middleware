
package com.openexchange.groupware.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.tools.iterator.SearchIterator;

public class ReminderTest {

	private ReminderSQLInterface reminderSql = null;

    protected Properties reminderProps = null;

    private static boolean isInit = false;

    private final int userId = -1;

    private Context context = null;

    public void init() throws Exception {
        if (isInit) {
            return;
        }

        Init.startServer();

        isInit = true;
    }

    @Before
    public void setUp() throws Exception {
        init();

		final TestConfig config = new TestConfig();
		final int contextId = ContextStorage.getInstance().getContextId(config.getContextName());
		context = ContextStorage.getInstance().getContext(contextId);
		reminderSql = ReminderHandler.getInstance();
	}

    /**
     * {@inheritDoc}
     */
    @After
    public void tearDown() throws Exception {
        if (isInit) {
            Init.stopServer();
        }
    }

    @Test
    public void testInsert() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);
	}

    @Test
	public void testUpdate() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		reminderObj = createReminderObject(targetId, Types.TASK);
		reminderSql.updateReminder(reminderObj, context);
	}

    @Test
	public void testDelete() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		reminderSql.deleteReminder(targetId, userId, reminderObj.getModule(), context);
	}

    @Test
	public void testDeleteAllReminders() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.TASK);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		reminderSql.deleteReminder(targetId, Types.TASK, context);
	}

    @Test
	public void testLoad() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final Date alarm = new Date(System.currentTimeMillis()-3600000);
		ReminderObject reminderObj = createReminderObject(targetId, Types.CONTACT);
		reminderObj.setDate(alarm);
		final int objectId = reminderSql.insertReminder(reminderObj, context);
		assertTrue("object_id is not > 0", objectId > 0);

		reminderObj = reminderSql.loadReminder(targetId, userId, Types.CONTACT, context);

		assertNotNull("is reminder object not null", reminderObj);
		assertEquals("targetId", targetId, reminderObj.getTargetId());
		assertEquals("module", Types.CONTACT, reminderObj.getModule());
		assertNotNull("date", reminderObj.getDate());
		assertEquals("userId", userId, reminderObj.getUser());
	}

    @Test
	public void testListReminderByTargetId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		int counter = 0;
		final SearchIterator<ReminderObject> it = reminderSql.listReminder(Types.APPOINTMENT, targetId, context);
		while (it.hasNext()) {
			final ReminderObject r = it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

    @Test
	public void testListReminderBetweenByUserId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		int counter = 0;
		final SearchIterator<ReminderObject> it = reminderSql.listModifiedReminder(userId, new Date(), context);
		while (it.hasNext()) {
			final ReminderObject r = it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

    @Test
	public void testListLastModifiedReminderUserId() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date(System.currentTimeMillis()-3600000));
		reminderSql.insertReminder(reminderObj, context);

		int counter = 0;
		final SearchIterator<ReminderObject> it = reminderSql.listModifiedReminder(userId, new Date(0), context);
		while (it.hasNext()) {
			final ReminderObject r = it.next();
			assertNotNull("check reminder objects in iterator", r);
			counter++;
		}

		assertTrue("result > 0", counter >= 1);
	}

    @Test
	public void testExistsReminder() throws Exception {
		final int targetId = IDGenerator.getId(context, Types.REMINDER);
		final ReminderObject reminderObj = createReminderObject(targetId, Types.APPOINTMENT);
		reminderObj.setDate(new Date());
		reminderSql.insertReminder(reminderObj, context);
		assertTrue("reminder does not exists!", reminderSql.existsReminder(targetId, userId, Types.APPOINTMENT, context));
		assertFalse("find invalid reminder!", reminderSql.existsReminder(targetId+1, userId, Types.APPOINTMENT, context));
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
