package com.obdlibrary.obd;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;

import java.util.ArrayList;

/**
 * TODO put description
 */
public final class ObdConfig {

    public static ArrayList<ObdCommand> getCommands() {
        ArrayList<ObdCommand> cmds = new ArrayList<>();
        cmds.add(new RPMCommand());
        cmds.add(new SpeedCommand());
        return cmds;
    }
}
