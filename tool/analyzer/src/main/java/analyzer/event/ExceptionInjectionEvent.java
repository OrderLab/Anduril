package analyzer.event;

import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.List;
import java.util.Objects;

public abstract class ExceptionInjectionEvent extends ProgramEvent {
    public final SootClass exceptionType;
    public ExceptionInjectionEvent(final SootClass exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionInjectionEvent that = (ExceptionInjectionEvent) o;
        return Objects.equals(exceptionType, that.exceptionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exceptionType);
    }
}
