package util;

public class Entity {
    private final String realName;
    private final String viewName;
    private final EntityType entityType;

    public Entity(String name, EntityType entityType) {
        this.realName = name;
        this.entityType = entityType;

        if(entityType == EntityType.VIEW)
            viewName = name + " (view)";
        else if(entityType == EntityType.SNAPSHOT)
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

    public EntityType getEntityType() {
        return entityType;
    }
}
