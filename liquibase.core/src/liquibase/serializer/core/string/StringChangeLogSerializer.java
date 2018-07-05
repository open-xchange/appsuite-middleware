
package liquibase.serializer.core.string;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

public class StringChangeLogSerializer implements ChangeLogSerializer {

    private static final int INDENT_LENGTH = 4;

    @Override
    public String[] getValidFileExtensions() {
        return new String[] { "txt" };
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return object.getSerializedObjectName() + ":" + serializeObject(object, 1);
    }

    private String serializeObject(LiquibaseSerializable objectToSerialize, int indent) {
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[");

            SortedSet<String> values = new TreeSet<String>();
            for (String field : objectToSerialize.getSerializableFields()) {
                Object value = objectToSerialize.getSerializableFieldValue(field);

                if (value instanceof LiquibaseSerializable) {
                    values.add(indent(indent) + serializeObject((LiquibaseSerializable) value, indent + 1));
                } else {
                    if (value != null) {
                        if (value instanceof Map) {
                            values.add(indent(indent) + field + "=" + serializeObject((Map) value, indent + 1));
                        } else if (value instanceof Collection) {
                            values.add(indent(indent) + field + "=" + serializeObject((Collection) value, indent + 1));
                        } else if (value instanceof Object[]) {
                            values.add(indent(indent) + field + "=" + serializeObject((Object[]) value, indent + 1));
                        } else {
                            String valueString = value.toString();
                            if (value instanceof Double || value instanceof Float) { //java 6 adds additional zeros to the end of doubles and floats
                                if (valueString.contains(".")) {
                                    valueString = valueString.replaceFirst("0*$", "");
                                }
                            }
                            values.add(indent(indent) + field + "=\"" + valueString + "\"");
                        }
                    }
                }
            }

            if (values.size() > 0) {
                buffer.append("\n");
                buffer.append(StringUtils.join(values, "\n"));
                buffer.append("\n");
            }
            buffer.append(indent(indent - 1)).append("]");
            return buffer.toString().replace("\r?\n", "\n"); //standardize all newline chars

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    private String indent(int indent) {
        return StringUtils.repeat(" ", INDENT_LENGTH * indent);
    }

    private String serializeObject(Object[] collection, int indent) {
        if (collection.length == 0) {
            return "[]";
        }

        StringBuilder returnStringBuilder = new StringBuilder("[\n");
        boolean first = true;
        for (Object object : collection) {
            if (first) {
                first = false;
            } else {
                returnStringBuilder.append(",\n");
            }
            if (object instanceof LiquibaseSerializable) {
                returnStringBuilder.append(indent(indent)).append(serializeObject((LiquibaseSerializable) object, indent + 1));
            } else {
                returnStringBuilder.append(indent(indent)).append(object.toString());
            }
        }
        returnStringBuilder.append(indent(indent - 1)).append("]");

        return returnStringBuilder.toString();

    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.size() == 0) {
            return "[]";
        }

        StringBuilder returnStringBuilder = new StringBuilder("[\n");
        for (Object object : collection) {
            if (object instanceof LiquibaseSerializable) {
                returnStringBuilder.append(indent(indent)).append(serializeObject((LiquibaseSerializable) object, indent + 1)).append(",\n");
            } else {
                returnStringBuilder.append(indent(indent)).append(object.toString()).append(",\n");
            }
        }
        String returnString = returnStringBuilder.toString().replaceFirst(",$", "");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    private String serializeObject(Map collection, int indent) {
        if (collection.size() == 0) {
            return "[]";
        }

        StringBuilder returnStringBuilder = new StringBuilder("{\n");
        for (Object key : new TreeSet(collection.keySet())) {
            returnStringBuilder.append(indent(indent)).append(key.toString()).append("=\"").append(collection.get(key)).append("\",\n");
        }
        String returnString = returnStringBuilder.toString().replaceFirst(",$", "");
        returnString += indent(indent - 1) + "}";

        return returnString;

    }

    @Override
    public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {

    }

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }
}
