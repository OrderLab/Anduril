package analyzer.phase;

/**
 * Information about a custom phase
 */
public class PhaseInfo {
    private String pack;
    private String name;
    private String full_name;
    private String help;
    private boolean whole_program;
    private boolean need_call_graph;

    public PhaseInfo(String pack, String name, String help, boolean whole_program, boolean need_call_graph) {
        this.pack = pack;
        this.name = name;
        this.full_name = pack + "." + name;
        this.help = help;
        // enable whole program option is the pack is w*
        this.whole_program = whole_program || pack.equals("wjtp") || pack.equals("wjop") || pack.equals("wjap");
        // call graph option is only applicable for whole program analysis phase
        this.need_call_graph = this.whole_program && need_call_graph;
    }

    public String getPack() {
        return pack;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return full_name;
    }

    public String getHelp() {
        return help;
    }

    public boolean isWholeProgram() {
        return whole_program;
    }

    public boolean needCallGraph() {
        return need_call_graph;
    }
}
