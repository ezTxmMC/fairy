package community.theprojects.fairy.node.database.model;

import community.theprojects.fairy.api.service.ServiceStatus;
import community.theprojects.fairy.node.database.SQLConnection;
import community.theprojects.fairy.node.database.Database;

import java.nio.file.Path;
import java.util.UUID;

@SQLConnection(host = "localhost", port = 3306, database = "fairycloud", username = "root", password = "")
public class ServiceModel {
    private UUID id;
    private String name;
    private String description;
    private String group;
    private Path path;
    private Process process;
    private ServiceStatus status;
    
    public ServiceModel(UUID id, String name, String description, String group, Path path, Process process, ServiceStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.group = group;
        this.path = path;
        this.process = process;
        this.status = status;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public void setPath(Path path) {
        this.path = path;
    }
    
    public void setProcess(Process process) {
        this.process = process;
    }
    
    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getGroup() {
        return this.group;
    }
    
    public Path getPath() {
        return this.path;
    }
    
    public Process getProcess() {
        return this.process;
    }
    
    public ServiceStatus getStatus() {
        return this.status;
    }

    public static ServiceModel of(UUID id) {
        try (var connection = new Database(ServiceModel.class)) {
            var statement = connection.prepareStatement("SELECT * FROM ServiceModel WHERE id = ?", id);
            var result = statement.executeQuery();
            if (result.next()) {
                return new ServiceModel(
                        (UUID) result.getObject("id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("group"),
                        result.getString("path") != null ? Path.of(result.getString("path")) : null,
                        null,
                        result.getString("status") != null ? ServiceStatus.valueOf(result.getString("status")) : null
                );
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
