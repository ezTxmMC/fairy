package community.theprojects.fairy.webinterface;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class VueProjectResolver {

    Path resolve(WebConfig cfg) {
        if (cfg.vueDirConfigured != null && !cfg.vueDirConfigured.isBlank()) {
            Path p = Paths.get(cfg.vueDirConfigured).toAbsolutePath().normalize();
            if (Files.exists(p.resolve("package.json"))) return p;
        }
        try {
            Path codePath = Paths.get(FairyWebinterface.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            Path parent = codePath;
            for (int i = 0; i < 5 && parent != null; i++) {
                Path srcResources = parent.resolve("src").resolve("main").resolve("resources");
                if (Files.isDirectory(srcResources)) {
                    Path sub = srcResources.resolve(cfg.vueResourceFolder);
                    if (Files.exists(sub.resolve("package.json"))) return sub.toAbsolutePath().normalize();
                    if (Files.exists(srcResources.resolve("package.json"))) return srcResources.toAbsolutePath().normalize();
                    break;
                }
                parent = parent.getParent();
            }
            if (Files.isDirectory(codePath)) {
                if (Files.exists(codePath.resolve("package.json"))) return codePath;
                Path sub = codePath.resolve(cfg.vueResourceFolder);
                if (Files.exists(sub.resolve("package.json"))) return sub.toAbsolutePath().normalize();
            }
        } catch (Exception ignored) { }
        try {
            URL url = FairyWebinterface.class.getResource("/" + cfg.vueResourceFolder);
            if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                Path p = Paths.get(url.toURI()).toAbsolutePath().normalize();
                if (Files.exists(p.resolve("package.json"))) return p;
            }
            URL rootUrl = FairyWebinterface.class.getResource("/");
            if (rootUrl != null && "file".equalsIgnoreCase(rootUrl.getProtocol())) {
                Path p = Paths.get(rootUrl.toURI()).toAbsolutePath().normalize();
                if (Files.exists(p.resolve("package.json"))) return p;
            }
        } catch (Exception ignored) { }
        return null;
    }
}
