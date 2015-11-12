package com.fillumina.lcs.myers;

import com.fillumina.lcs.LinearSpaceMyersLcsWrapper;
import com.fillumina.lcs.testutil.AbstractLcsTest;
import com.fillumina.lcs.ListLcs;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BaselineOptimizedLinearSpaceMyersLcsTest extends AbstractLcsTest {

    private LinearSpaceMyersLcsWrapper<Character> algo = new LinearSpaceMyersLcsWrapper<>();

    public static void main(String[] args) {
        new BaselineOptimizedLinearSpaceMyersLcsTest().randomLcs(60, 10);
    }

    @Override
    protected ListLcs<?> getLcsAlgorithm() {
        return new LinearSpaceMyersLcsWrapper<>();
    }
}
