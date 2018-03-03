package liquibase.changelog.filter;

import java.util.List;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

public class ActuallyExecutedChangeSetFilter extends RanChangeSetFilter {

    public ActuallyExecutedChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        super(ranChangeSets);
    }

    @Override
    public boolean accepts(ChangeSet changeSet) {
        RanChangeSet ranChangeSet = getRanChangeSet(changeSet);
        return ranChangeSet != null && (ranChangeSet.getExecType() == null || ranChangeSet.getExecType().equals(ChangeSet.ExecType.EXECUTED) || ranChangeSet.getExecType().equals(ChangeSet.ExecType.RERAN));
    }
}
