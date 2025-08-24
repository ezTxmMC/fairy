package community.theprojects.fairy.api.group;

import community.theprojects.fairy.api.util.JavaVersion;

public interface IGroup {

    String getId();
    String getName();
    String getDescription();
    int getMinimumMemory();
    int getMaximumMemory();
    int getMaxPlayers();
    boolean hasStaticServices();
    IType getType();
    JavaVersion getJavaVersion();
    IGroupTemplate getTemplate();

}
