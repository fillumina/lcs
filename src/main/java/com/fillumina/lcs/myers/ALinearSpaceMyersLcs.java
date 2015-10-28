package com.fillumina.lcs.myers;

import com.fillumina.lcs.Lcs;
import com.fillumina.lcs.util.BidirectionalVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The indexes are passed along the calls so to avoid using sublists.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ALinearSpaceMyersLcs<T> implements Lcs<T> {

    @Override
    public List<T> lcs(final List<T> a, final List<T> b) {
        final int n = a.size();
        final int m = b.size();
        Match snakes = lcsTails(a, 0, n, b, 0, m);
        return extractLcs(snakes, a);
    }

    public Match lcsTails(final List<T> a, final int a0, final int n,
            final List<T> b, final int b0, final int m) {
        final int min = Math.min(n, m);
        int d;
        for (d=0; d<min && a.get(a0+d).equals(b.get(b0+d)); d++);
        if (d != 0) {
            return Match.chain(new Match(a0, b0, d),
                    lcsTails(a, a0+d, n-d, b, b0+d, m-d));
        }
        int u;
        for (u=0; u<min && a.get(a0+n-u-1).equals(b.get(b0+m-u-1)); u++);
        if (u != 0) {
            return Match.chain(lcs(a, a0, n-u, b, b0, m-u, createEndpoint()),
                    new Match(a0+n-u, b0+m-u, u));
        }
        return lcs(a, a0, n, b, b0, m, createEndpoint());
    }

    public Match lcs(final List<T> a, final int a0, final int n,
            final List<T> b, final int b0, final int m,
            final int[] endpoint) {

        if (n == 0) {
            if (m != 0) {
                return vertical(endpoint, a0, b0, m);
            }
            return Match.NULL;
        }

        if (m == 0 /* && n != 0 */) {
            return horizontal(endpoint, a0, b0, n);
        }

        if (n == 1) {
            if (m == 1) {
                if (a.get(a0).equals(b.get(b0))) {
                    return diagonal(endpoint, a0, b0, 1);
                }
                return relativeBoundaries(endpoint, a0, b0, 1, 1);
            }

            relativeBoundaries(endpoint, a0, b0, 1, m);
            T t = a.get(a0);
            for (int i = b0; i < b0+m; i++) {
                if (t.equals(b.get(i))) {
                    return new Match(a0, i, 1);
                }
            }
            return Match.NULL;
        }

        if (m == 1) {
            relativeBoundaries(endpoint, a0, b0, n, 1);
            T t = b.get(b0);
            for (int i = a0; i < a0+n; i++) {
                if (t.equals(a.get(i))) {
                    return new Match(i, b0, 1);
                }
            }
            return Match.NULL;
        }

        final int[] middleSnakeEndpoint = createEndpoint();
        final Match diagonal = findMiddleSnake(a, a0, n, b, b0, m, middleSnakeEndpoint);
        final boolean fromStart = touchingStart(a0, b0, middleSnakeEndpoint);
        final boolean toEnd = touchingEnd(a0+n, b0+m, middleSnakeEndpoint);

        if (fromStart && toEnd) {
            relativeBoundaries(endpoint, a0, a0, n, m);
            return diagonal;
        }

        int[] beforeEndpoint = null, afterEndpoint = null;

        Match before = fromStart ?
                null :
                lcs(a, a0, xStart(middleSnakeEndpoint) - a0,
                        b, b0, yStart(middleSnakeEndpoint) - b0,
                        beforeEndpoint = createEndpoint());

        Match after = toEnd ?
                null :
                lcs(a, xEnd(middleSnakeEndpoint),
                        a0 + n - xEnd(middleSnakeEndpoint),
                        b, yEnd(middleSnakeEndpoint),
                        b0 + m - yEnd(middleSnakeEndpoint),
                        afterEndpoint = createEndpoint());

        if (fromStart) {
            absoluteBoundaries(endpoint,
                    xStart(middleSnakeEndpoint), yStart(middleSnakeEndpoint),
                    xEnd(afterEndpoint), yEnd(afterEndpoint));
            return Match.chain(diagonal, after);

        } else if (toEnd) {
            absoluteBoundaries(endpoint,
                    xStart(beforeEndpoint), yStart(beforeEndpoint),
                    xEnd(middleSnakeEndpoint), yEnd(middleSnakeEndpoint));
            return Match.chain(before, diagonal);
        }

        absoluteBoundaries(endpoint,
                xStart(beforeEndpoint), yStart(beforeEndpoint),
                xEnd(afterEndpoint), yEnd(afterEndpoint));
        return Match.chain(before, diagonal, after);
    }

    Match findMiddleSnake(List<T> a, int a0, int n,
            List<T> b, int b0, int m,
            int[] endpoint) {
        final int max = (n + m + 1) / 2;
        final int delta = n - m;
        final boolean oddDelta = (delta & 1) == 1;

        final int[] snake = new int[3];

        // TODO find a way to allocate those only once and reuse them indexed
        final BidirectionalVector vf = new BidirectionalVector(max + 1);
        final BidirectionalVector vb = new BidirectionalVector(max + 1);

        vb.set(delta - 1, n);

        int kk, xf, xr, start, end;
        for (int d = 0; d <= max; d++) {
            start = delta - (d - 1);
            end = delta + (d - 1);
            for (int k = -d; k <= d; k += 2) {
                xf = findFurthestReachingDPath(
                        d, k, a, a0, n, b, b0, m, vf, snake);
                if (oddDelta && isIn(k, start, end)) {
                    if (xf > 0 && vb.get(k) <= xf) {
                        return findLastSnake(k, snake, a0, b0, vf, endpoint);
                    }
                }
            }

            for (int k = -d; k <= d; k += 2) {
                kk = k + delta;
                xr = findFurthestReachingDPathReverse(
                        d, kk, a, a0, n, b, b0, m, delta, vb, snake);
                if (!oddDelta && isIn(k, -d, +d)) {
                    if (xr >= 0 && xr <= vf.get(kk)) {
                        return findLastSnakeReverse(kk, a0, b0, vb,
                                endpoint, snake);
                    }
                }
            }
        }
        return Match.NULL;
    }

    private int findFurthestReachingDPath(int d, int k,
            List<T> a, int a0, int n,
            List<T> b, int b0, int m,
            BidirectionalVector v,
            int[] snake) {
        int x, y;

        int next = v.get(k + 1);
        int prev = v.get(k - 1);
        if (k == -d || (k != d && prev < next)) {
            x = next;       // down
            setKShift(snake, 1);
        } else {
            x = prev + 1;   // right
            setKShift(snake, -1);
        }
        setXMid(snake, x);
        y = x - k;
        while (x >= 0 && y >= 0 && x < n && y < m &&
                a.get(a0+x).equals(b.get(b0+y))) {
            x++;
            y++;
        }
        v.set(k, x);
        setXEnd(snake, x);
        return x;
    }

    private Match findLastSnake(int k, int[] snake, int a0, int b0,
            BidirectionalVector v,
            int[] endpoint) {
        final int kShift = getKShift(snake);
        final int xStart = v.get(k + kShift);
        final int yStart = xStart - k - kShift;
        final int xMid = getXMid(snake);
        final int yMid = xMid - k;
        final int xEnd = getXEnd(snake);
        final int yEnd = xEnd - k;

        absoluteBoundaries(endpoint, a0+xStart, b0+yStart, a0+xEnd, b0+yEnd);
        return Match.create(a0+xMid, b0+yMid, xEnd-xMid);
    }

    private int findFurthestReachingDPathReverse(int d, int k,
            List<T> a, int a0, int n,
            List<T> b, int b0, int m,
            int delta, BidirectionalVector v,
            int[] snake) {
        int x, y;

        final int next = v.get(k + 1); // left
        final int prev = v.get(k - 1); // up
        if (k == d + delta || (k != -d + delta && prev < next)) {
            x = prev;   // up
            setKShift(snake, -1);
        } else {
            x = next - 1;   // left
            setKShift(snake, +1);
        }
        setXMid(snake, x);
        y = x - k;
        while (x > 0 && y > 0 && x <= n && y <= m &&
                a.get(a0+x-1).equals(b.get(b0+y-1))) {
            x--;
            y--;
        }
        setXEnd(snake, x);
        if (x >= 0) {
            v.set(k, x);
        }
        return x;
    }

    private Match findLastSnakeReverse(int k, int a0, int b0,
            BidirectionalVector v,
            int[] endpoint, int[] snake) {

        final int kShift = getKShift(snake);
        final int xStart = v.get(k + kShift);
        final int yStart = xStart - k - kShift;
        final int xMid = getXMid(snake);
//        final int yMid = xMid - k;
        final int xEnd = getXEnd(snake);
        final int yEnd = xEnd - k;

        final int a0XEnd = a0 + xEnd;
        final int b0YEnd = b0 + yEnd;

        absoluteBoundaries(endpoint, a0XEnd, b0YEnd, a0+xStart, b0+yStart);
        return Match.create(a0XEnd, b0YEnd, xMid-xEnd);
    }

    private static boolean isIn(int value, int startInterval, int endInterval) {
        if (startInterval < endInterval) {
            if (value < startInterval) {
                return false;
            }
            if (value > endInterval) {
                return false;
            }
        } else {
            if (value > startInterval) {
                return false;
            }
            if (value < endInterval) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the common subsequence elements.
     */
    private List<T> extractLcs(Match snakes, List<T> a) {
        System.out.println("SOLUTION:");
        List<T> list = new ArrayList<>();
        for (Match segment : snakes) {
            System.out.println(segment.toString());
            segment.addEquals(list, a);
        }
        return list;
    }

    public static class Match implements Iterable<Match>, Serializable {
        private static final long serialVersionUID = 1L;
        public static final Match NULL = new Match(-1, -1, -1);
        private final int x, y, steps;
        private Match next;
        private Match last;

        private static Match create(int x, int y, int steps) {
            if (steps <= 0) {
                return Match.NULL;
            }
            return new Match(x, y, steps);
        }

        private Match(int x, int y, int steps) {
            this.x = x;
            this.y = y;
            this.steps = steps;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getSteps() {
            return steps;
        }

        private <T> void addEquals(List<T> list, List<T> a) {
            for (int i = x; i < x + steps; i++) {
                list.add(a.get(i));
            }
        }

        private static Match chain(Match... segments) {
            switch (segments.length) {
                case 0: return NULL;
                case 1: return segments[1];
            }
            Match head = NULL;
            Match current = null;
            for (Match s : segments) {
                if (s != null && s != NULL) {
                    if (head == NULL) {
                        current = head = s;
                    } else {
                        current.next = s;
                    }
                    while (current.last != null) {
                        current = current.last;
                    }
                    while (current.next != null) {
                        current = current.next;
                    }
                }
            }
            if (current != head) {
                head.last = current;
            }
            return head;
        }

        @Override
        public Iterator<Match> iterator() {
            return new Iterator<Match>() {
                private Match current = Match.this;

                @Override
                public boolean hasNext() {
                    return current != null && current != NULL;
                }

                @Override
                public Match next() {
                    Match tmp = current;
                    current = current.next;
                    return tmp;
                }
            };
        }

        static String toString(Match s) {
            Match current = s;
            StringBuilder buf = new StringBuilder("[");
            buf.append(s.toString());
            while (current.next != null && current.next != NULL) {
                current = current.next;
                buf.append(", ").append(current.toString());
            }
            buf.append("]");
            return buf.toString();
        }

        @Override
        public String toString() {
            if (this == NULL) {
                return "Match{NULL}";
            }
            return getClass().getSimpleName() +
                    "{xStart=" + x + ", yStart=" + y + ", steps=" + steps + '}';
        }
    }

    // methods to manage endpoints and snake in a slightly more meaningfully way
    // (an object would have been a performance overkill)

    private static int xStart(int[] endpoint) {
        return endpoint[0];
    }

    private static int yStart(int[] endpoint) {
        return endpoint[1];
    }

    private static int xEnd(int[] endpoint) {
        return endpoint[2];
    }

    private static int yEnd(int[] endpoint) {
        return endpoint[3];
    }

    private static int[] createEndpoint() {
        return new int[4];
    }

    private static Match relativeBoundaries(int[] endpoint,
            int xStart, int yStart, int xStep, int yStep) {
        endpoint[0] = xStart;
        endpoint[1] = yStart;
        endpoint[2] = xStart + xStep;
        endpoint[3] = yStart + yStep;
        return Match.NULL;
    }

    private static Match absoluteBoundaries(int[] endpoint,
            int xStart, int yStart, int xEnd, int yEnd) {
        endpoint[0] = xStart;
        endpoint[1] = yStart;
        endpoint[2] = xEnd;
        endpoint[3] = yEnd;
        return Match.NULL;
    }

    private static Match diagonal(int[] endpoint,
            int xStart, int yStart, int step) {
        endpoint[0] = xStart;
        endpoint[1] = yStart;
        endpoint[2] = xStart + step;
        endpoint[3] = yStart + step;
        return new Match(xStart, yStart, step);
    }

    private static Match horizontal(int[] endpoint,
            int xStart, int yStart, int step) {
        endpoint[0] = xStart;
        endpoint[1] = yStart;
        endpoint[2] = xStart + step;
        endpoint[3] = yStart;
        return Match.NULL;
    }

    private static Match vertical(int[] endpoint,
            int xStart, int yStart, int step) {
        endpoint[0] = xStart;
        endpoint[1] = yStart;
        endpoint[2] = xStart;
        endpoint[3] = yStart + step;
        return Match.NULL;
    }

    private static boolean touchingStart(int x, int y, int[] endpoint) {
        return x == xStart(endpoint) && y == yStart(endpoint);
    }

    private static boolean touchingEnd(int x, int y, int[] endpoint) {
        return xEnd(endpoint) >= x && yEnd(endpoint) >= y;
    }

    private void setKShift(int[] snake, int k) {
        snake[0] = k;
    }

    private void setXMid(int[] snake, int xMid) {
        snake[1] = xMid;
    }

    private void setXEnd(int[] snake, int xEnd) {
        snake[2] = xEnd;
    }

    private int getKShift(int[] snake) {
        return snake[0];
    }

    private int getXMid(int[] snake) {
        return snake[1];
    }

    private int getXEnd(int[] snake) {
        return snake[2];
    }
}
