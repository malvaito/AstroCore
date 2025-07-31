package dev.malvaito.tpa;

import dev.malvaito.AstroCore;

public class TPA {

    private final AstroCore plugin;
    private final TPAManager tpaManager;
    private final TPACommand tpaCommand;

    public TPA(AstroCore plugin) {
        this.plugin = plugin;
        this.tpaManager = new TPAManager(plugin);
        this.tpaCommand = new TPACommand(plugin, tpaManager);
    }

    public TPAManager getTpaManager() {
        return tpaManager;
    }

    public TPACommand getTpaCommand() {
        return tpaCommand;
    }
}
