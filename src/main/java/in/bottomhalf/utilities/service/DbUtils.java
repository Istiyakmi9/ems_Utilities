package in.bottomhalf.utilities.service;

import in.bottomhalf.utilities.annotations.Column;
import in.bottomhalf.utilities.annotations.Id;
import in.bottomhalf.utilities.annotations.Table;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
public class DbUtils {
    public <T> String save(T instance) throws Exception {
        String tableName = getTableName(instance);
        String primayKey = getPrimaryKey(instance);

        HashMap<String, Field> fields = getFields(instance);
        StringBuilder columnsName = new StringBuilder();
        columnsName.append("INSERT INTO ").append(tableName).append("(");

        StringBuilder columnsValue = new StringBuilder();
        columnsValue.append("VALUES(");

        StringBuilder updateStatement = new StringBuilder();

        try {
            String delimiter = "";
            for (Map.Entry<String, Field> field : fields.entrySet()) {
                field.getValue().setAccessible(true);
                Object value = field.getValue().get(instance);
                Class<?> type = field.getValue().getType();

                columnsName.append(delimiter + field.getKey());

                if (type == String.class || type == Date.class) {
                    columnsValue.append(delimiter + "'" + value.toString() + "'");
                    if(!field.getKey().equals(primayKey)) {
                        updateStatement.append(delimiter + field.getKey() + " = ").append("'" + value.toString() + "'");
                    }
                } else {
                    columnsValue.append(delimiter + value.toString());
                    if(!field.getKey().equals(primayKey)) {
                        updateStatement.append(delimiter + field.getKey() + " = ").append(value.toString());
                    }
                }

                delimiter = ",";
            }

            columnsName.append(") ");
            columnsValue.append(") ON DUPLICATE KEY UPDATE ");
        } catch (IllegalAccessException ex) {
            throw new Exception(ex.getMessage());
        }

        return columnsName.toString() + columnsValue.toString() + updateStatement.toString();
    }

    public <T> String get(Class<T> type) throws Exception {
        var instance = type.getDeclaredConstructor().newInstance();
        String tableName = getTableName(instance);
        if (tableName == null) {
            throw new Exception("Table name not define on given modal");
        }

        return createSelectStatement(instance, tableName);
    }

    public <T> String getById(Class<T> type) throws Exception {
        var instance = type.getDeclaredConstructor().newInstance();
        String tableName = getTableName(instance);
        if (tableName == null) {
            throw new Exception("Table name not define on given modal");
        }

        return createSelectStatement(instance, tableName);
    }

    public <T> String getPrimaryKey(T instance) throws Exception {
        String primaryKey = null;
        var columnId = instance.getClass().getAnnotationsByType(Id.class);
        var columnName = instance.getClass().getAnnotationsByType(Column.class);
        if (columnName.length > 0 && columnId.length > 0) {
            primaryKey = Arrays.stream(columnName).findFirst().get().name();

            if (primaryKey == null) {
                throw new Exception("Table does not have any primary key");
            }
        }

        return primaryKey;
    }

    private <T> String getTableName(T instance) throws Exception {
        String tableName = null;
        var annotations = instance.getClass().getAnnotationsByType(Table.class);
        if (annotations.length > 0) {
            tableName = Arrays.stream(annotations).findFirst().get().name();

            if (tableName == null) {
                throw new Exception("Table name not define on given modal");
            }
        }

        return tableName;
    }

    private <T> String createSelectStatement(T instance, String tableName) throws Exception {
        HashMap<String, Field> fields = getFields(instance);
        StringBuilder columnsName = new StringBuilder();
        columnsName.append("SELECT ");

        try {
            String delimiter = "";
            for (Map.Entry<String, Field> field : fields.entrySet()) {
                columnsName.append(delimiter + field.getKey());
                delimiter = ",";
            }

            columnsName.append(" FROM " + tableName);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        return columnsName.toString();
    }

    private <T> HashMap<String, Field> getFields(T instance) {
        Optional<Column> annotation;
        HashMap<String, Field> fieldsCollection = new HashMap<>();
        Field[] fields = instance.getClass().getDeclaredFields();

        for (Field field : fields) {
            annotation = Arrays.stream(field.getAnnotationsByType(Column.class)).findFirst();
            annotation.ifPresent(column -> fieldsCollection.put(column.name(), field));
        }

        return fieldsCollection;
    }
}
