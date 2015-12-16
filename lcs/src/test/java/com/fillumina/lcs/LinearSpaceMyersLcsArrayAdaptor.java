package com.fillumina.lcs;

import java.util.List;

/**
 * Adaptor that makes {@link AbstractLinearSpaceMyersLcs} callable using
 * the {@link Lcs} interface.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class LinearSpaceMyersLcsArrayAdaptor implements LcsSizeEvaluator {
    private LinearSpaceMyersLcs<?> linearSpaceMyersLcs;

    @Override
    public int getLcsSize() {
        return linearSpaceMyersLcs.getLcsLength();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<? extends T> lcs(List<? extends T> a, List<? extends T> b) {
        linearSpaceMyersLcs = LinearSpaceMyersLcs.lcs(a.toArray(), b.toArray());
        return (List<? extends T>) linearSpaceMyersLcs.extractLcsList();
    }
}
