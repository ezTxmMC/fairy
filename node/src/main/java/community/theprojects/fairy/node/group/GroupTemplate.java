package community.theprojects.fairy.node.group;

import community.theprojects.fairy.api.group.IGroupTemplate;

import java.util.List;

public record GroupTemplate(String name, String path, List<String> groups) implements IGroupTemplate {
}
