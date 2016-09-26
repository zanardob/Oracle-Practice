package util;

public class Field {
    private String name;
    private String value;
    private FieldType type;

    public Field(String name, FieldType type) {
        this.name = name;
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public FieldType getType() {
        return type;
    }

}
