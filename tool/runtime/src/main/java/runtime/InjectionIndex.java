package runtime;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

public final class InjectionIndex {
    public final int pid, id, occurrence, block;
    public final String exceptionName;

    public InjectionIndex(final int pid, final int id, final String exceptionName, final int occurrence, final int block) {
        this.pid = pid;
        this.id = id;
        this.exceptionName = exceptionName;
        this.occurrence = occurrence;
        this.block = block;
    }

    public JsonObjectBuilder dump() {
        return Json.createObjectBuilder()
                .add("pid", pid)
                .add("id", id)
                .add("exception", exceptionName)
                .add("occurrence", occurrence)
                .add("block", block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InjectionIndex that = (InjectionIndex) o;
        return pid == that.pid && id == that.id && occurrence == that.occurrence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, id, occurrence);
    }
}
