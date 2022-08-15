package reporter.check;

import javax.json.JsonObject;

class InjectionLocationMatcher {

    public static final class InjectionLocation {
        public final String injectionClass;
        public final String invocation;
        public final String exception;

        InjectionLocation(final String injectionClass,
                          final String invocation, final String exception) {
            this.injectionClass = injectionClass;
            this.invocation = invocation;
            this.exception = exception;
        }

        public boolean match (JsonObject loc) {
            return injectionClass.equals(loc.getString("class"))
                    && invocation.equals(loc.getString("invocation"))
                    && exception.equals(loc.getString("exception"));
        }
    }

    private InjectionLocation[] targetPoints;

    InjectionLocationMatcher(JsonObject spec) {
        //hard-coded
        switch(spec.getString("case")) {
            case "hdfs-4233":
                targetPoints = new InjectionLocation[1];
                final String injectionClass = "org.apache.hadoop.hdfs.server.namenode.EditLogFileOutputStream";
                final String invocation = "void <init>(java.io.File,java.lang.String)";
                final String exception = "java.io.FileNotFoundException";
                targetPoints[0] = new InjectionLocation(injectionClass,invocation,exception);
                break;

            default:
                // no target points or case number is wrong ?
        }
    }

    public boolean match(JsonObject loc) {
        for (InjectionLocation targetPoint : targetPoints) {
            if (targetPoint.match(loc)) {
                return true;
            }
        }
        return false;
    }
}
