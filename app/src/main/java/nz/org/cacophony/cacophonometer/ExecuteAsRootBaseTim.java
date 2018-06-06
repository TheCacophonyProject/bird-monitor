package nz.org.cacophony.cacophonometer;

import java.util.ArrayList;

/**
 * The app is designed to run on rooted phones so that it can toggle airplane/flight mode on and off
 * (to save power).  To be able to do this it needs to be able to run root commands - this class
 * extends ExecuteAsRootBase to allow specific commands to be run e.g. enable flight mode to run.
 * It may have been possible to modify ExecuteAsRootBase, but extended it interfered with that
 * code less which I hoped would reduce the chance of breaking it :-)
 */
@SuppressWarnings("Convert2Diamond")
public class ExecuteAsRootBaseTim extends ExecuteAsRootBase {
    private final ArrayList<String> commands = new ArrayList<String>();
    @Override
    protected ArrayList<String> getCommandsToExecute() {

        return commands;
    }

    void addCommand(String command){
        commands.add(command);
    }
}
