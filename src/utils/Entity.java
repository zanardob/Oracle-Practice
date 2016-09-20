package utils;

enum Type {
    TABLE,
    VIEW,
    SNAPSHOT
}

public class Entity {
    private String realName;
    private String viewName;
    private Type type;

    public Entity(String name, Type type) {
        this.realName = name;
        this.type = type;

        if(type == Type.VIEW)
            viewName = name + " (view)";
        else if(type == Type.SNAPSHOT)
            viewName = name + " (snapshot)";
        else
            viewName = name;
    }

    public String getRealName() {
        return realName;
    }

    public String getViewName() {
        return viewName;
    }

    public Type getType() {
        return type;
    }
}
