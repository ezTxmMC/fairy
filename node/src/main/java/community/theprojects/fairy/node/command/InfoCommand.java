package community.theprojects.fairy.node.command;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.api.console.IPrinter;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.node.console.HexColor;
import community.theprojects.fairy.node.console.Printer;

public class InfoCommand implements ICommand {
    private final INode node;
    private final IPrinter printer;
    private final String description;

    public InfoCommand(String description) {
        this.node = FairyNode.getInstance();
        this.printer = node.getConsole().getPrinter();
        this.description = description;
    }

    @Override
    public void execute(String[] args) {
        this.printer.println(HexColor.colorText("Fairy - Node: " + this.node.getName(), HexColor.Colors.YELLOW), true);
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
