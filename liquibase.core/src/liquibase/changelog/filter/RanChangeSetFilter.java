package liquibase.changelog.filter;

import java.util.List;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

public abstract class RanChangeSetFilter implements ChangeSetFilter {
    public List<RanChangeSet> ranChangeSets;

    public RanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equalsIgnoreCase(changeSet.getId())
                    && ranChangeSet.getAuthor().equalsIgnoreCase(changeSet.getAuthor())
                    && ranChangeSet.getChangeLog().equalsIgnoreCase(changeSet.getFilePath())) {
                return ranChangeSet;
            }
        }
        return null;

    }
}
