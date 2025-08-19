package community.theprojects.fairy.node;

public class FairyLauncher {

    public static void main(String[] args) {
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            descriptionBuilder.append(args[i]);
            if (i != args.length - 1) {
                descriptionBuilder.append(" ");
            }
        }
        FairyNode fairyNode = new FairyNode(args[0], descriptionBuilder.toString());
        fairyNode.init();
        fairyNode.start();
    }
}
