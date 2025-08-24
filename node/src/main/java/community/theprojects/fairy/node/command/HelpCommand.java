package community.theprojects.fairy.node.command;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.api.console.IPrinter;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.node.console.HexColor;

public class HelpCommand implements ICommand {
    private final INode node;
    private final IPrinter printer;
    private final String description;

    public HelpCommand(String description) {
        this.node = FairyNode.getInstance();
        this.printer = node.getConsole().getPrinter();
        this.description = description;
    }

    @Override
    public void execute(String[] args) {
        this.printer.println(HexColor.colorText("========[ ", HexColor.Colors.ORANGE)
                + HexColor.colorText("Help", HexColor.Colors.YELLOW)
                + HexColor.colorText(" ]========", HexColor.Colors.ORANGE), true);
        node.getCommandHandler().getCommands().forEach((name, command) -> {
            this.printer.println(HexColor.colorText(name + " - " + command.getDescription(), HexColor.Colors.YELLOW), true);
        });
        this.printer.println(HexColor.colorText("========[ ", HexColor.Colors.ORANGE)
                + HexColor.colorText("Help", HexColor.Colors.YELLOW)
                + HexColor.colorText(" ]========", HexColor.Colors.ORANGE), true);
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
