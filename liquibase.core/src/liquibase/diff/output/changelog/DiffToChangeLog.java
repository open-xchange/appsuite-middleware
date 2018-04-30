package liquibase.diff.output.changelog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectComparator;
import liquibase.util.StringUtils;

public class DiffToChangeLog {

    private String idRoot = String.valueOf(new Date().getTime());
    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetAuthor;
    private final DiffResult diffResult;
    private final DiffOutputControl diffOutputControl;


    private static Set<Class<?>> loggedOrderFor = new HashSet<Class<?>>();

    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        this.diffResult = diffResult;
        this.diffOutputControl = diffOutputControl;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void print(String changeLogFile) throws IOException {
        ChangeLogSerializer changeLogSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(changeLogFile);
        this.print(changeLogFile, changeLogSerializer);
    }

    public void print(PrintStream out) throws IOException {
        this.print(out, new XMLChangeLogSerializer());
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer) throws IOException {
        File file = new File(changeLogFile);
        if (!file.exists()) {
            LogFactory.getLogger().info(file + " does not exist, creating");
            FileOutputStream stream = new FileOutputStream(file);
            print(new PrintStream(stream), changeLogSerializer);
            stream.close();
        } else {
            LogFactory.getLogger().info(file + " exists, appending");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            print(new PrintStream(out), changeLogSerializer);

            String xml = new String(out.toByteArray());
            xml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");
            xml = xml.replaceFirst("</databaseChangeLog>", "");
            xml = xml.trim();
            if ("".equals(xml)) {
                LogFactory.getLogger().info("No changes found, nothing to do");
                return;
            }

            String lineSeparator = System.getProperty("line.separator");
            long offset = 0;
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            try {
                for (String line; (line = fileReader.readLine()) != null;) {
                    int index = line.indexOf("</databaseChangeLog>");
                    if (index >= 0) {
                        offset += index;
                    } else {
                        offset += line.getBytes().length;
                        offset += lineSeparator.getBytes().length;
                    }
                }
                fileReader.close();

                fileReader = new BufferedReader(new FileReader(file));
                fileReader.skip(offset);
            } finally {
                fileReader.close();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            try {
                randomAccessFile.seek(offset);
                randomAccessFile.writeBytes("    ");
                randomAccessFile.write(xml.getBytes());
                randomAccessFile.writeBytes(lineSeparator);
                randomAccessFile.writeBytes("</databaseChangeLog>" + lineSeparator);
            } finally {
                randomAccessFile.close();
            }
        }
    }

    /**
     * Prints changeLog that would bring the target database to be the same as
     * the reference database
     */
    public void print(PrintStream out, ChangeLogSerializer changeLogSerializer) throws IOException {
        List<ChangeSet> changeSets = generateChangeSets();
        changeLogSerializer.write(changeSets, out);
        out.flush();
    }

    public List<ChangeSet> generateChangeSets() {
        final ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
        DatabaseObjectComparator comparator = new DatabaseObjectComparator();

        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        List<Class<? extends DatabaseObject>> types = getOrderedOutputTypes(MissingObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (DatabaseObject object : diffResult.getMissingObjects(type, comparator)) {
                if (object == null) {
                    continue;
                }
                Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(object)) {
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (DatabaseObject object : diffResult.getUnexpectedObjects(type, comparator)) {
                Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getComparisonSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getComparisonSnapshot().getDatabase().isSystemObject(object)) {
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = getOrderedOutputTypes(ChangedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects(type, comparator).entrySet()) {
                Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(entry.getKey()) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(entry.getKey())) {
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }
        return changeSets;
    }

    protected List<Class<? extends DatabaseObject>> getOrderedOutputTypes(Class<? extends ChangeGenerator> generatorType) {

        Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();
        DependencyGraph graph = new DependencyGraph();
        for (Class<? extends DatabaseObject> type : diffResult.getReferenceSnapshot().getSnapshotControl().getTypesToInclude()) {
            graph.addType(type);
        }
        List<Class<? extends DatabaseObject>> types = graph.sort(comparisonDatabase, generatorType);

        if (!loggedOrderFor.contains(generatorType)) {
            StringBuilder logBuilder = new StringBuilder(generatorType.getSimpleName());
            logBuilder.append(" type order: ");
            for (Class<? extends DatabaseObject> type : types) {
                logBuilder.append("    ").append(type.getName());
            }
            LogFactory.getLogger().debug(logBuilder.toString());
            loggedOrderFor.add(generatorType);
        }

        return types;
    }

    private void addToChangeSets(Change[] changes, List<ChangeSet> changeSets, ObjectQuotingStrategy quotingStrategy) {
        if (changes != null) {
            for (Change change : changes) {
                changeSets.add(generateChangeSet(change, quotingStrategy));
            }
        }
    }

    protected ChangeSet generateChangeSet(Change change, ObjectQuotingStrategy quotingStrategy) {
        ChangeSet changeSet = new ChangeSet(generateId(), getChangeSetAuthor(), false, false,
                null, changeSetContext, null, quotingStrategy, null);
        changeSet.addChange(change);

        return changeSet;
    }

    protected String getChangeSetAuthor() {
        if (changeSetAuthor != null) {
            return changeSetAuthor;
        }
        String author = System.getProperty("user.name");
        if (StringUtils.trimToNull(author) == null) {
            return "diff-generated";
        } else {
            return author + " (generated)";
        }
    }

    public void setChangeSetAuthor(String changeSetAuthor) {
        this.changeSetAuthor = changeSetAuthor;
    }

    public void setIdRoot(String idRoot) {
        this.idRoot = idRoot;
    }

    protected String generateId() {
        return idRoot + "-" + changeNumber++;
    }

    private static class DependencyGraph {

        private final Map<Class<? extends DatabaseObject>, Node> allNodes = new HashMap<Class<? extends DatabaseObject>, Node>();

        /**
         * Initializes a new {@link DependencyGraph}.
         */
        public DependencyGraph() {
            super();
        }

        void addType(Class<? extends DatabaseObject> type) {
            allNodes.put(type, new Node(type));
        }

        public List<Class<? extends DatabaseObject>> sort(Database database, Class<? extends ChangeGenerator> generatorType) {
            ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
            for (Class<? extends DatabaseObject> type : allNodes.keySet()) {
                for (Class<? extends DatabaseObject> afterType : changeGeneratorFactory.runBeforeTypes(type, database, generatorType)) {
                    getNode(type).addEdge(getNode(afterType));
                }

                for (Class<? extends DatabaseObject> beforeType : changeGeneratorFactory.runAfterTypes(type, database, generatorType)) {
                    getNode(beforeType).addEdge(getNode(type));
                }
            }


            ArrayList<Node> returnNodes = new ArrayList<Node>();

            SortedSet<Node> nodesWithNoIncomingEdges = new TreeSet<Node>(new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return o1.type.getName().compareTo(o2.type.getName());
                }
            });
            for (Node n : allNodes.values()) {
                if (n.inEdges.size() == 0) {
                    nodesWithNoIncomingEdges.add(n);
                }
            }

            while (!nodesWithNoIncomingEdges.isEmpty()) {
                Node node = nodesWithNoIncomingEdges.iterator().next();
                nodesWithNoIncomingEdges.remove(node);

                returnNodes.add(node);

                for (Iterator<Edge> it = node.outEdges.iterator(); it.hasNext(); ) {
                    //remove edge e from the graph
                    Edge edge = it.next();
                    Node nodePointedTo = edge.to;
                    it.remove();//Remove edge from node
                    nodePointedTo.inEdges.remove(edge);//Remove edge from nodePointedTo

                    //if nodePointedTo has no other incoming edges then insert nodePointedTo into nodesWithNoIncomingEdges
                    if (nodePointedTo.inEdges.isEmpty()) {
                        nodesWithNoIncomingEdges.add(nodePointedTo);
                    }
                }
            }
            //Check to see if all edges are removed
            for (Node n : allNodes.values()) {
                if (!n.inEdges.isEmpty()) {
                    StringBuilder messageBuilder = new StringBuilder("Could not resolve ");
                    messageBuilder.append(generatorType.getSimpleName()).append(" dependencies due to dependency cycle. Dependencies: \n");
                    for (Node node : allNodes.values()) {
                        SortedSet<String> fromTypes = new TreeSet<String>();
                        SortedSet<String> toTypes = new TreeSet<String>();
                        for (Edge edge : node.inEdges) {
                            fromTypes.add(edge.from.type.getSimpleName());
                        }
                        for (Edge edge : node.outEdges) {
                            toTypes.add(edge.to.type.getSimpleName());
                        }
                        String from = StringUtils.join(fromTypes, ",");
                        String to = StringUtils.join(toTypes, ",");
                        messageBuilder.append("    [").append(from).append("] -> ");
                        messageBuilder.append(node.type.getSimpleName()).append(" -> [").append(to).append("]\n");
                    }

                    throw new UnexpectedLiquibaseException(messageBuilder.toString());
                }
            }
            List<Class<? extends DatabaseObject>> returnList = new ArrayList<Class<? extends DatabaseObject>>();
            for (Node node : returnNodes) {
                returnList.add(node.type);
            }
            return returnList;
        }


        private Node getNode(Class<? extends DatabaseObject> type) {
            Node node = allNodes.get(type);
            if (node == null) {
                node = new Node(type);
            }
            return node;
        }


        static class Node {
            public final Class<? extends DatabaseObject> type;
            public final HashSet<Edge> inEdges;
            public final HashSet<Edge> outEdges;

            public Node(Class<? extends DatabaseObject> type) {
                this.type = type;
                inEdges = new HashSet<Edge>();
                outEdges = new HashSet<Edge>();
            }

            public Node addEdge(Node node) {
                Edge e = new Edge(this, node);
                outEdges.add(e);
                node.inEdges.add(e);
                return this;
            }

            @Override
            public String toString() {
                return type.getName();
            }
        }

        static class Edge {
            public final Node from;
            public final Node to;

            public Edge(Node from, Node to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((from == null) ? 0 : from.hashCode());
                result = prime * result + ((to == null) ? 0 : to.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Edge)) {
                    return false;
                }
                Edge e = (Edge) obj;
                return e.from == from && e.to == to;
            }

        }
    }
}
