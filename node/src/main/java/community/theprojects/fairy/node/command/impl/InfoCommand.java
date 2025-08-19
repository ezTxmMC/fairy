package community.theprojects.fairy.node.command.impl;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.node.console.Printer;

public class InfoCommand implements ICommand {
    private final INode node;
    private final Printer printer;
    private final String name;
    private final String description;

    public InfoCommand(String name, String description) {
        this.node = FairyNode.getInstance();
        this.printer = ((FairyNode) node).getConsole().getPrinter();
        this.name = name;
        this.description = description;
    }

    @Override
    public void execute(String[] args) {
        this.printer.println("Fairy - Node: " + this.node.getName());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
