package utils;

public class Entity {
    private String realName;
    private String viewName;
    private EntityType entityType;

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
