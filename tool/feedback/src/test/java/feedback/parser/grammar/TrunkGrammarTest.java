package feedback.parser.grammar;

import feedback.common.ThreadTestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

final class TrunkGrammarTest extends ThreadTestBase {
    private enum TrunkType {
        LOG_MESSAGE(TrunkGrammarTestUtil.LOG_MESSAGE()),
        HEAD_EXCEPTION_WITHOUT_MESSAGE(TrunkGrammarTestUtil.HEAD_EXCEPTION_WITHOUT_MESSAGE()),
        HEAD_EXCEPTION_WITH_MESSAGE(TrunkGrammarTestUtil.HEAD_EXCEPTION_WITH_MESSAGE()),
        NESTED_EXCEPTION_WITHOUT_MESSAGE(TrunkGrammarTestUtil.NESTED_EXCEPTION_WITHOUT_MESSAGE()),
        NESTED_EXCEPTION_WITH_MESSAGE(TrunkGrammarTestUtil.NESTED_EXCEPTION_WITH_MESSAGE()),
        STACK_TRACE(TrunkGrammarTestUtil.STACK_TRACE());

        final Trunk value;
        TrunkType(final Trunk value) {
            this.value = value;
        }
    }

    private static abstract class TrunkTestHelper {
        final Trunk[] trunks;

        private TrunkTestHelper(final TrunkType[] trunks) {
            this.trunks = Arrays.stream(trunks).map(trunk -> trunk.value).toArray(Trunk[]::new);
        }

        abstract void test();
    }

    private static final class LengthTest extends TrunkTestHelper {
        private final int begin, end, length;

        private LengthTest(final int begin, final int end, final int length, final TrunkType... trunks) {
            super(trunks);
            this.begin = begin;
            this.end = end;
            this.length = length;
        }

        @Override
        void test() {
            TrunkGrammarTestUtil.testLength(trunks, begin, end, length);
        }
    }

    private static final TrunkTestHelper[] lengthCases = new TrunkTestHelper[]{
            new LengthTest(0, 0, 1,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 2,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 1, 1,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 1, 1,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 1, 0,
                    TrunkType.LOG_MESSAGE),
            new LengthTest(0, 3, 0,
                    TrunkType.HEAD_EXCEPTION_WITHOUT_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 1,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 1,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 2,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITHOUT_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 2,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 2,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE),
            new LengthTest(0, 0, 3,
                    TrunkType.HEAD_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITH_MESSAGE,
                    TrunkType.LOG_MESSAGE,
                    TrunkType.STACK_TRACE,
                    TrunkType.STACK_TRACE,
                    TrunkType.NESTED_EXCEPTION_WITH_MESSAGE,
                    TrunkType.STACK_TRACE),
    };

    @Test
    void testTrunkGrammar() {
        for (final TrunkTestHelper test : lengthCases) {
            test.test();
        }
        for (final TrunkGrammarTestUtil.ContentTestHelper test : TrunkGrammarTestUtil.contentCases()) {
            test.test();
        }
    }
}
