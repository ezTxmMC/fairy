package community.theprojects.fairy.node.command;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.console.IPrinter;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.node.config.TemplatesConfig;
import community.theprojects.fairy.node.console.HexColor;
import community.theprojects.fairy.node.group.GroupTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class TemplateCommand implements ICommand {
    private final INode node;
    private final IPrinter printer;
    private final String description;

    public TemplateCommand(String description) {
        this.node = FairyNode.getInstance();
        this.printer = node.getConsole().getPrinter();
        this.description = description;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    sendUsage();
                    return;
                }
                args = Arrays.copyOfRange(args, 1, args.length);
                this.createTemplate(args);
            }
            case "delete" -> {
                args = Arrays.copyOfRange(args, 1, args.length);
                this.deleteTemplate(args);
            }
        }
    }

    private void sendUsage() {
        this.printer.println(HexColor.colorText("========[ ", HexColor.Colors.ORANGE)
                + HexColor.colorText("Usage", HexColor.Colors.YELLOW)
                + HexColor.colorText(" ]========", HexColor.Colors.ORANGE), true);
        this.printer.println(HexColor.colorText("template create <name> <path>", HexColor.Colors.YELLOW), true);
        this.printer.println(HexColor.colorText("template remove <name>", HexColor.Colors.YELLOW), true);
        this.printer.println(HexColor.colorText("========[ ", HexColor.Colors.ORANGE)
                + HexColor.colorText("Usage", HexColor.Colors.YELLOW)
                + HexColor.colorText(" ]========", HexColor.Colors.ORANGE), true);
    }

    private void createTemplate(String[] args) {
        try {
            IConfig templatesConfig = this.node.getTemplatesConfig();
            String name = args[0];
            String path = args[1];
            ((TemplatesConfig) templatesConfig).addTemplate(new GroupTemplate(name, path, new ArrayList<>()));
            Files.createDirectories(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTemplate(String[] args) {
        IConfig templatesConfig = this.node.getTemplatesConfig();
        String name = args[0];
        ((TemplatesConfig) templatesConfig).removeTemplate(name);
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
