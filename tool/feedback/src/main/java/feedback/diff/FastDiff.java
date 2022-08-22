package feedback.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

final class FastDiff<T> {
    private enum CHOICE {
        GOOD_ONLY,
        BAD_ONLY,
        COMMON,
    }

    final ArrayList<T> badOnly = new ArrayList<>();
    final int common;

    FastDiff(final T[] good, final T[] bad) {
        int[] opt = new int[bad.length + 1], update = new int[bad.length + 1];
        Arrays.fill(opt, 0);
        final CHOICE[] choices = new CHOICE[(good.length + 1) * (bad.length + 1)];
        for (int i = 0; i <= bad.length; i++) {
            choices[i] = CHOICE.BAD_ONLY;
        }
        int current = bad.length + 1;
        for (final T e : good) {
            update[0] = opt[0];
            choices[current] = CHOICE.GOOD_ONLY;
            int opt_i = opt[0] + 1;
            int previous = update[0];
            for (int i = 0; i < bad.length; i++) {
                int opt_i_1 = opt[i + 1];
                int best = opt_i_1;
                CHOICE choice = CHOICE.GOOD_ONLY;
                if (e.equals(bad[i]) && best < opt_i) {
                    best = opt_i;
                    choice = CHOICE.COMMON;
                }
                if (best < previous) {
                    best = previous;
                    choice = CHOICE.BAD_ONLY;
                }
                update[i + 1] = best;
                choices[current + i + 1] = choice;
                previous = best;
                opt_i = opt_i_1 + 1;
            }
            current += bad.length + 1;
            final int[] tmp = update;
            update = opt;
            opt = tmp;
        }
        int i = good.length, j = bad.length;
        int common = 0;
        while (i != 0 || j != 0) {
            switch (choices[i * (bad.length + 1) + j]) {
                case COMMON: i--; j--; common++; break;
                case GOOD_ONLY: i--; break;
                case BAD_ONLY: j--; this.badOnly.add(bad[j]); break;
            }
        }
        Collections.reverse(this.badOnly);
        this.common = common;
    }
}
