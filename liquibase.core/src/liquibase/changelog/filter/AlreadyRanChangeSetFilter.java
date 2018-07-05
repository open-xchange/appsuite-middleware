package liquibase.changelog.filter;

import java.util.List;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

public class AlreadyRanChangeSetFilter extends RanChangeSetFilter {

    public AlreadyRanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        super(ranChangeSets);
    }

    @Override
    public boolean accepts(ChangeSet changeSet) {
        return getRanChangeSet(changeSet) != null;
    }

}
