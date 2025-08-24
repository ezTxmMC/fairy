package community.theprojects.fairy.webinterface;

class WebConfig {
    final String vueDirConfigured;
    final String vueResourceFolder;
    final String vueOutDir;
    final String packageManager;
    final String runningDirOverride;

    WebConfig() {
        this.vueDirConfigured = getStringPropertyOrEnv("vue.dir", "VUE_DIR", null);
        this.vueResourceFolder = getStringPropertyOrEnv("vue.resource", "VUE_RESOURCE", "vue");
        this.vueOutDir = getStringPropertyOrEnv("vue.out", "VUE_OUT", "dist");
        this.packageManager = getStringPropertyOrEnv("vue.pm", "VUE_PM", "").trim();
        this.runningDirOverride = getStringPropertyOrEnv("vue.running.dir", "VUE_RUNNING_DIR", null);
    }

    static String getStringPropertyOrEnv(String prop, String env, String def) {
        String val = System.getProperty(prop);
        if (val == null || val.isBlank()) val = System.getenv(env);
        return (val == null || val.isBlank()) ? def : val;
    }
}
