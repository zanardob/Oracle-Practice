package utils;

public class ForeignKeyConstraint {
    private String tableName;
    private String columnName;
    private String referredTableName;
    private String referredColumnName;

    public ForeignKeyConstraint(String tableName, String columnName, String referredTableName, String referredColumnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.referredTableName = referredTableName;
        this.referredColumnName = referredColumnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getReferredTableName() {
        return referredTableName;
    }

    public String getReferredColumnName() {
        return referredColumnName;
    }
}
