package runtime.config;

public final class Config {
    // common for experiment and baseline
    public double probability;

    public boolean logInject;
    public String exceptionName;
    public boolean recordOnthefly;
    public boolean distributedMode;
    public boolean disableAgent;
    public int pid;
    public int trialTimeout;
    public int targetId;
    public int times;
    public boolean fixPointInjectionMode;

    private void setCommonDefaultValues() {
        exceptionName = System.getProperty("flakyAgent.fault", "#");

        distributedMode = Boolean.getBoolean("flakyAgent.distributedMode");
        disableAgent = Boolean.getBoolean("flakyAgent.disableAgent");
        logInject = Boolean.getBoolean("flakyAgent.logInject");
        recordOnthefly = Boolean.getBoolean("flakyAgent.recordOnthefly");
        fixPointInjectionMode = Boolean.getBoolean("flakyAgent.fixPointInjectionMode");

        pid = Integer.getInteger("flakyAgent.pid", -1);
        trialTimeout = Integer.getInteger("flakyAgent.trialTimeout", -1);
        targetId = Integer.getInteger("flakyAgent.injectionId", -1);
        times = Integer.getInteger("flakyAgent.injectionTimes", 0);
    }

    // baseline only
    public String baselinePolicy;

    private void setBaselineOnlyDefaultValues() {
        probability = Double.parseDouble(System.getProperty("baseline.probability", "0"));

        baselinePolicy = System.getProperty("baseline.policy", "");
    }

    // experiment only
    public boolean avoidBlockMode;   // { TraceAgent.avoidBlockMode == false } ==> avoid the blocks encountered
    public boolean allowFeedback;
    public String injectionPointsPath;
    public String traceRecordFileName;
    public String timePriorityTable;
    public boolean isTimeFeedback;
    public boolean isProbabilityFeedback;
    public int injectionOccurrenceLimit;
    public int slidingWindowSize;

    private void setExperimentOnlyDefaultValues() {
        probability = Double.parseDouble(System.getProperty("flakyAgent.probability", "0.01"));

        traceRecordFileName = System.getProperty("flakyAgent.traceFile");
        timePriorityTable = System.getProperty("flakyAgent.timePriorityTable", "#");
        injectionPointsPath = System.getProperty("flakyAgent.injectionPointsPath", "#");

        avoidBlockMode = Boolean.getBoolean("flakyAgent.avoidBlockMode");
        allowFeedback = Boolean.getBoolean("flakyAgent.feedback");
        isTimeFeedback = Boolean.getBoolean("flakyAgent.timeFeedback");
        isProbabilityFeedback = Boolean.getBoolean("flakyAgent.probabilityFeedback");

        injectionOccurrenceLimit = Integer.getInteger("flakyAgent.injectionOccurrenceLimit", 3);
        slidingWindowSize = Integer.getInteger("flakyAgent.slidingWindow", 10);
    }

    private Config() { }

    public static Config getDefaultBaselineConfig() {
        final Config config = new Config();
        config.setCommonDefaultValues();
        config.setBaselineOnlyDefaultValues();
        return config;
    }

    public static Config getDefaultExperimentConfig() {
        final Config config = new Config();
        config.setCommonDefaultValues();
        config.setExperimentOnlyDefaultValues();
        return config;
    }
}
