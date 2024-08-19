package reporter.check;

import javax.json.JsonObject;

class InjectionLocationMatcher {

    public static final class InjectionLocation {
        public final String injectionClass;
        public final String method;
        public final String invocation;
        public final String exception;

        InjectionLocation(final String injectionClass, final String method,
                          final String invocation, final String exception) {
            this.injectionClass = injectionClass;
            this.method = method;
            this.invocation = invocation;
            this.exception = exception;
        }

        public boolean match (JsonObject injection) {
            return (injectionClass == null || injectionClass.equals(injection.getJsonObject("location").getString("class")))
                    && (method == null || method.equals(injection.getJsonObject("location").getString("method")))
                    && (invocation == null || invocation.equals(injection.getString("invocation")))
                    && (exception == null || exception.equals(injection.getString("exception")));
        }
    }

    private InjectionLocation[] targetPoints;

    InjectionLocationMatcher(JsonObject spec) {
        //hard-coded
        String injectionClass = null;
        String method = null;
        String invocation = null;
        String exception = null;
        //switch("hdfs-12248") {
        switch(spec.getString("case")) {
            case "hdfs-4233":
                targetPoints = new InjectionLocation[1];
                injectionClass = "org.apache.hadoop.hdfs.server.namenode.EditLogFileOutputStream";
                invocation = "void <init>(java.io.File,java.lang.String)";
                exception = "java.io.FileNotFoundException";
                targetPoints[0] = new InjectionLocation(injectionClass,method,invocation,exception);
                break;

            case "hdfs-12248":
                targetPoints = new InjectionLocation[1];
                injectionClass = "org.apache.hadoop.hdfs.server.namenode.TransferFsImage";
                method = "org.apache.hadoop.hdfs.server.namenode.TransferFsImage$TransferResult uploadImageFromStorage(java.net.URL,org.apache.hadoop.conf.Configuration,org.apache.hadoop.hdfs.server.namenode.NNStorage,org.apache.hadoop.hdfs.server.namenode.NNStorage$NameNodeFile,long,org.apache.hadoop.hdfs.util.Canceler)";
                invocation = "void <init>(java.net.URL,java.lang.String)";
                exception = "java.net.MalformedURLException";
                targetPoints[0] = new InjectionLocation(injectionClass,method,invocation,exception);
                break;

            default:
                // no target points or case number is wrong ?
        }
    }

    public boolean match(JsonObject injection) {
        for (InjectionLocation targetPoint : targetPoints) {
            if (targetPoint.match(injection)) {
                return true;
            }
        }
        return false;
    }
}
