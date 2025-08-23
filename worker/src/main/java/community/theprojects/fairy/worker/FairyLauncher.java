package community.theprojects.fairy.worker;

import community.theprojects.fairy.api.IWorker;

public class FairyLauncher {

    public static void main(String[] args) {
        FairyWorker fairyWorker = (FairyWorker) initWorker(args);
        fairyWorker.init();
        fairyWorker.start();
    }

    private static IWorker initWorker(String[] args) {
        if (args.length > 1) {
            StringBuilder descriptionBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                descriptionBuilder.append(args[i]);
                if (i != args.length - 1) {
                    descriptionBuilder.append(" ");
                }
            }
            return new FairyWorker(args[0], descriptionBuilder.toString());
        }
        return new FairyWorker("worker-1", "Main worker.");
    }
}
