package com.fillumina.lcs.hirschberg;

import com.fillumina.lcs.testutil.AbstractLcsTest;
import java.util.List;
import com.fillumina.lcs.LcsList;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class HirschbergLinearSpaceAlgorithmLcsTest
        extends AbstractLcsTest {

    @Override
    public LcsList getLcsAlgorithm() {
        return new HirschbergLinearSpaceAlgorithmLcs() {

            @Override
            public <T> List<? extends T> lcs(List<? extends T> xs,
                    List<? extends T> ys) {
                count(xs, ys);
                return super.lcs(xs, ys);
            }
        };
    }
}
