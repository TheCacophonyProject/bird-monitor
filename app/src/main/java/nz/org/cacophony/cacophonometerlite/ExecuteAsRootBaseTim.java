package nz.org.cacophony.cacophonometerlite;

import java.util.ArrayList;

/**
 * Created by Student on 10/20/2017.
 */

public class ExecuteAsRootBaseTim extends ExecuteAsRootBase {
    ArrayList<String> commands = new ArrayList<String>();
    @Override
    protected ArrayList<String> getCommandsToExecute() {
//        ArrayList<String> commands = new ArrayList<String>();
//      //  commands.add("ls");
//        commands.add("ls");
        return commands;
    }

    void addCommand(String command){
        commands.add(command);
    }
}
