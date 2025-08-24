package community.theprojects.fairy.node;

import community.theprojects.fairy.api.INode;

public class FairyLauncher {

    public static void main(String[] args) {
        FairyNode fairyNode = (FairyNode) initNode(args);
        fairyNode.init();
        fairyNode.start();
    }

    private static INode initNode(String[] args) {
        if (args.length > 1) {
            StringBuilder descriptionBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                descriptionBuilder.append(args[i]);
                if (i != args.length - 1) {
                    descriptionBuilder.append(" ");
                }
            }
            return new FairyNode(args[0], descriptionBuilder.toString());
        }
        return new FairyNode("node-1", "");
    }
}
