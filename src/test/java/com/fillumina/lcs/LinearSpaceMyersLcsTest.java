package com.fillumina.lcs;

import com.fillumina.lcs.LinearSpaceMyersLcs.Match;
import com.fillumina.lcs.testutil.AbstractLcsTest;
import com.fillumina.lcs.testutil.CharacterLcsTestHelper;
import com.fillumina.lcs.testutil.RandomSequenceGenerator;
import com.fillumina.lcs.util.ListUtils;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class LinearSpaceMyersLcsTest extends AbstractLcsTest {

    private LinearSpaceMyersLcsAdaptor<Character> algo =
            new LinearSpaceMyersLcsAdaptor<>();

    public static void main(String[] args) {
        new LinearSpaceMyersLcsTest().randomLcs(60, 10);
    }

    @Override
    protected Lcs<?> getLcsAlgorithm() {
        return new LinearSpaceMyersLcsAdaptor<>();
    }

    @Test(timeout = 100L)
    public void shouldLcsHUMAN() {
        countLcs("HUMAN", "CHIMPANZEE", 4);
    }

    @Test(timeout = 100L)
    public void shouldLcsABCABBA() {
        countLcs("ABCABBA", "CBABAC", 4);
    }

    @Test(timeout = 100L)
    public void shouldLcsPYTHON() {
        countLcs("PYTHON", "PONY", 3);
    }

    @Test(timeout = 100L)
    public void shouldLcsSPRINGTIME() {
        countLcs("SPRINGTIME", "PIONEER", 4);
    }

    @Test(timeout = 100L)
    public void shouldLcsHORSEBACK() {
        countLcs("HORSEBACK", "SNOWFLAKE", 3);
    }

    @Test(timeout = 100L)
    public void shouldLcsMAELSTROM() {
        countLcs("MAELSTROM", "BECALM", 3);
    }

    @Test(timeout = 100L)
    public void shouldLcsHEROICALLY() {
        countLcs("HEROICALLY", "SCHOLARLY", 5);
    }

    @Test(timeout = 100L)
    public void shouldLcs0ForAnEmptyString() {
        countLcs("ABCDEF", "GHIJKLMN", 0);
    }

    @Test(timeout = 100L)
    public void shouldLcs1ForTheOnlyMatchAtBeginning() {
        countLcs("ABCDEF", "A", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcs1ForTheOnlyMatchAtEnd() {
        countLcs("ABCDEF", "F", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcs1ForTheOnlyMatchAtMiddle() {
        countLcs("ABCDEF", "C", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcsEmptyResultForEmptyList() {
        countLcs("ABCDEF", "", 0);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheOnlyMatchAtBeginningReversed() {
        countLcs("A", "ABCDEF", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheOnlyMatchAtEndReversed() {
        countLcs("F", "ABCDEF", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheOnlyMatchAtMiddleReversed() {
        countLcs("C", "ABCDEF", 1);
    }

    @Test(timeout = 100L)
    public void shouldLcsEmptyResultForEmptyListReversed() {
        countLcs("", "ABCDEF", 0);
    }

    @Test(timeout = 100L)
    public void shouldLcsEmptyListForBothEmptyList() {
        countLcs("", "", 0);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheLeftDiagonal() {
        countLcs("123AAAAAAA", "123BBBBBBB", 3);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheRightDiagonal() {
        countLcs("AAAAAAA123", "BBBBBBB123", 3);
    }

    @Test(timeout = 100L)
    public void shouldLcsTheBothEndsDiagonals() {
        countLcs("123AAAAAAA123", "123BBBBBBB123", 6);
    }

    private void countLcs(String a, String b, int expectedLcs) {
        final List<Character> listA = CharacterLcsTestHelper.toList(a);
        final List<Character> listB = CharacterLcsTestHelper.toList(b);
        Match m = algo.lcsMatch(listA, listB);
        assertEquals(ListUtils.toString(m), expectedLcs, m.getSequenceSize());
    }

    @Test(timeout = 1_000L)
    public void shouldPassVeryLongTest() {
        RandomSequenceGenerator generator =
//                new RandomSequenceGenerator(60,10, 26641683514364L);
                new RandomSequenceGenerator(6000,5000);

        System.out.println(generator.toString());

        @SuppressWarnings("unchecked")
        List<Integer> lcsList = ((Lcs)getLcsAlgorithm())
                .lcs(generator.getA(), generator.getB());

        assertEquals(generator.getLcs(), lcsList);
    }
}
