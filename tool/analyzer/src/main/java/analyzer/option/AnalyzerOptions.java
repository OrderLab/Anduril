package analyzer.option;

import analyzer.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Parsed command line arguments for the analyzer (main code snippets are from AutoWatchdog project).
 */
public class AnalyzerOptions {
    private boolean is_help = false;
    private boolean is_soot_help = false;
    private boolean keep_debug = false;
    private boolean is_whole_program = true;
    private boolean gen_executable = false;
    private boolean no_output = false;
    private boolean output_jar = true;
    private boolean list_analysis = true;

    private String class_path;
    private List<String> input_list;
    private String main_class;

    private String flaky_case;
    private String failure_log_diff_locations_path;

    //a distributed system like HDFS can have multiple main classes as entries,
    // which would be needed in long running scanning phase
    private List<String> secondary_main_classes;

    private String output_dir;
    private String[] analyses;
    private String[] classes;
    private Map<String, List<String>> phase_options;
    private String[] args;
    // settings that override the values from the config file
    private Properties override_props;

    private static AnalyzerOptions instance = new AnalyzerOptions();
    public static AnalyzerOptions getInstance() {
        return instance;
    }

    private AnalyzerOptions() {

    }

    public String getFlakyCase() {
        return flaky_case;
    }

    void setFlakyCase(String flaky_case) {
        this.flaky_case = flaky_case;
    }

    public String getDiffPath() {
        return failure_log_diff_locations_path;
    }

    void setDiffPath(String failure_log_diff_locations_path) {
        this.failure_log_diff_locations_path = failure_log_diff_locations_path;
    }

    public boolean isHelp() {
        return is_help;
    }

    void setIsHelp(boolean is_help) {
        this.is_help = is_help;
    }

    public boolean isSootHelp() {
        return is_soot_help;
    }

    void setIsSootHelp(boolean is_soot_help) {
        this.is_soot_help = is_soot_help;
    }

    public boolean keepDebug() {
        return keep_debug;
    }

    void setKeepDebug(boolean keep_debug) {
        this.keep_debug = keep_debug;
    }

    public boolean isWholeProgram() {
        return is_whole_program;
    }

    void setIsWholeProgram(boolean is_whole_program) {
        this.is_whole_program = is_whole_program;
    }

    public boolean genExecutable() {
        return gen_executable;
    }

    void setGenExecutable(boolean gen_executable) {
        this.gen_executable = gen_executable;
    }

    public boolean noOutput() {
        return no_output;
    }
    void setNoOutput(boolean no_output) {
        this.no_output = no_output;
    }

    public boolean isOutputJar() {
        return output_jar;
    }

    void setOutputJar(boolean output_jar) {
        this.output_jar = output_jar;
    }

    public boolean listAnalysis() {
        return list_analysis;
    }

    void setListAnalysis(boolean list_analysis) {
        this.list_analysis = list_analysis;
    }

    public String getClassPath() {
        return class_path;
    }

    void setClassPath(String class_path) {
        this.class_path = class_path;
    }

    /**
     * Get the list of input directories or jars
     * @return
     */
    public List<String> getInputList() {
        return input_list;
    }

    void setInputList(List<String> input_list) {
        this.input_list = input_list;
    }

    public boolean isInputListEmpty() {
        return input_list == null || input_list.isEmpty();
    }

    /**
     * Get the main class to start analysis
     * @return
     */
    public String getMainClass() {
        return main_class;
    }

    void setMainClass(String main_class) {
        this.main_class = main_class;
    }

    /**
     * Get the list of secondary main classes
     * @return
     */
    public List<String> getSecondaryMainClassList() {
        return secondary_main_classes;
    }

    void setSecondaryMainClassList(List<String> secondary_main_classes) {
        this.secondary_main_classes = secondary_main_classes;
    }

    public boolean isSecondaryMainClassListEmpty() {
        return secondary_main_classes == null || secondary_main_classes.isEmpty();
    }

    /**
     * Get the output directory
     * @return
     */
    public String getOutputDir() {
        return output_dir;
    }

    void setOutputDir(String output_dir) {
        this.output_dir = output_dir;
    }

    /**
     * Get the list of analyses to execute
     * @return
     */
    public String[] getAnalyses() {
        return analyses;
    }

    void setAnalyses(String[] analyses) {
        this.analyses = analyses;
    }

    /**
     * Get the list of class names to be analyzed
     * @return
     */
    public String[] getClasses() {
        return classes;
    }

    public void setClasses(String[] classes) {
        this.classes = classes;
    }

    /**
     * Get the list of options passed to one or more Soot phases
     * @return
     */
    public Map<String, List<String>> getPhaseOptions() {
        return phase_options;
    }

    public void setPhaseOptions(Map<String, List<String>> phase_options) {
        this.phase_options = phase_options;
    }

    /**
     * Get the properties that are specified through the command line. They
     * will override the settings in the config file.
     * @return
     */
    public Properties getOverrideProperties() {
        return override_props;
    }

    public void setOverrideProperties(Properties override_props) {
        this.override_props = override_props;
    }

    public String[] getArgs() {
        return args;
    }

    void setArgs(String[] args) {
        this.args = args;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- keep_debug? ").append(keep_debug).append("\n");
        sb.append("- whole_program? ").append(is_whole_program).append("\n");
        sb.append("- gen_executable? ").append(gen_executable).append("\n");
        sb.append("- no_output? ").append(no_output).append("\n");
        sb.append("- output_jar? ").append(output_jar).append("\n");
        sb.append("- list_analysis? ").append(list_analysis).append("\n");
        sb.append("- class_path: ").append(class_path).append("\n");
        sb.append("- input_list: ").append(StringUtils.join(",", input_list))
                .append("\n");
        sb.append("- main_class: ").append(main_class).append("\n");
        sb.append("- output_dir: ").append(output_dir).append("\n");
        sb.append("- analyses: ").append(StringUtils.join(",", analyses)).append("\n");
        sb.append("- classes: ").append(StringUtils.join(",", classes)).append("\n");
        sb.append("- phase_options: ");
        if (phase_options != null) {
            for (Map.Entry<String, List<String>> entry : phase_options.entrySet()) {
                sb.append(entry.getKey()).append(" ").append(StringUtils.join(",",
                        entry.getValue())).append("; ");
            }
            sb.append("\n");
        } else {
            sb.append("null\n");
        }

        sb.append("- ARGS: ").append(StringUtils.join(" ", args)).append("\n");
        return sb.toString();
    }
}
