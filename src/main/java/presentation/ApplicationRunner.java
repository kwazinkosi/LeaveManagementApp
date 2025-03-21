package presentation;

import java.util.Map;

import application.cli.CliParser;
import application.console.ConsoleUI;
import application.services.LeaveService;

public class ApplicationRunner {
	
    private final Map<UiType, UserInterface> uiStrategies;
    public ApplicationRunner(LeaveService leaveService) {
    	
        this.uiStrategies = Map.of(
            UiType.CLI, new CliParser(leaveService),
            UiType.CONSOLE, new ConsoleUI(leaveService)
        );
    }

    public void run(String[] args) {
    	
        UiType uiType = detectUiType(args);
        UserInterface ui = uiStrategies.get(uiType);
        ui.launch(args);
    }

    private UiType detectUiType(String[] args) {
        return args.length > 0 ? UiType.CLI : UiType.CONSOLE;
    }

    private enum UiType { CLI, CONSOLE, WEB }
}