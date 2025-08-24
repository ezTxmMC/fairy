package community.theprojects.fairy.node.config;

import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.group.IGroupTemplate;
import community.theprojects.fairy.util.json.JsonFileHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TemplatesConfig implements IConfig {
    private final List<JSONObject> templates;

    public TemplatesConfig() {
        this.templates = new ArrayList<>();
    }

    public void addTemplate(IGroupTemplate template) {
        try {
            JSONObject templateJson = new JSONObject();
            templateJson.put("name", template.name()).put("path", template.path()).put("groups", template.groups());
            this.templates.add(templateJson);
            JsonFileHandler.writeToFile(this, "storage/templates.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeTemplate(String name) {
        try {
            AtomicInteger index = new AtomicInteger(0);
            AtomicInteger removeIndex = new AtomicInteger(0);
            this.getTemplates().forEach(template -> {
                if (template.getString("name").equalsIgnoreCase(name)) {
                    removeIndex.set(index.get());
                }
                index.set(index.getAndIncrement());
            });
            this.templates.remove(removeIndex.get());
            JsonFileHandler.writeToFile(this, "storage/templates.json", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeGroupFromTemplate(String name) {
        try {
            this.getTemplates().forEach(template -> {
                if (template.getString("name").equalsIgnoreCase(name)) {
                    int i = -1;
                    for (Object object : template.getJSONArray("groups")) {
                        i++;
                        if (!(object instanceof String groupName)) {
                            continue;
                        }
                        if (!name.equalsIgnoreCase(groupName)) {
                            continue;
                        }
                        break;
                    }
                    template.getJSONArray("groups").remove(i);
                }
            });
            JsonFileHandler.writeToFile(this, "storage/templates.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTemplateExisting(String name) {
        AtomicBoolean exists = new AtomicBoolean(false);
        this.getTemplates().forEach(template -> {
            if (template.getString("name").equalsIgnoreCase(name)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

    public boolean isGroupHavingTemplate(String groupName) {
        AtomicBoolean having = new AtomicBoolean(false);
        this.getTemplates().forEach(template -> {
            if (template.getJSONArray("groups").toList().contains(groupName)) {
                having.set(true);
            }
        });
        return having.get();
    }

    public String getTemplateByGroupName(String groupName) {
        AtomicReference<String> templateName = new AtomicReference<>();
        this.getTemplates().forEach(template -> {
            if (template.getJSONArray("groups").toList().contains(groupName)) {
                templateName.set(template.getString("name"));
            }
        });
        return templateName.get();
    }

    public List<JSONObject> getTemplates() {
        return templates;
    }

    @Override
    public String toString() {
        return "TemplatesConfig{templates=" + templates + "}";
    }
}
