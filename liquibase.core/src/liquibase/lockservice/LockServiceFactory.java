package liquibase.lockservice;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

	private static volatile LockServiceFactory instance;

	public static LockServiceFactory getInstance() {
	    LockServiceFactory tmp = instance;
	    if (null == tmp) {
            synchronized (LockServiceFactory.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new LockServiceFactory();
                    instance = tmp;
                }
            }
        }
		return tmp;
	}

	// -------------------------------------------------------------------------------------------------------------------------------------

	private final List<LockService> registry = new CopyOnWriteArrayList<LockService>();

	private LockServiceFactory() {
		Class<? extends LockService>[] classes;
		try {
			classes = ServiceLocator.getInstance().findClasses(LockService.class);

			for (Class<? extends LockService> clazz : classes) {
				register(clazz.getConstructor().newInstance());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public void register(LockService lockService) {
		registry.add(0, lockService);
	}

	public LockService getLockService(Database database) {
        SortedSet<LockService> foundServices = new TreeSet<LockService>(new Comparator<LockService>() {

            @Override
            public int compare(LockService o1, LockService o2) {
                return -1 * Integer.compare(o1.getPriority(), o2.getPriority());
			}
        });

        for (LockService lockService : registry) {
            if (lockService.supports(database)) {
                foundServices.add(lockService);
			}
        }

        if (foundServices.size() == 0) {
            throw new UnexpectedLiquibaseException("Cannot find LockService for " + database.getShortName());
        }

        try {
            LockService lockService = foundServices.iterator().next().getClass().newInstance();
            lockService.setDatabase(database);
            return lockService;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
	}

	public void resetAll() {
		for (LockService lockService : registry) {
			lockService.reset();
		}
		instance = null;
	}

}
