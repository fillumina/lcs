package com.fillumina.lcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @see
 * <a href="http://www.codeproject.com/Articles/42279/Investigating-Myers-diff-algorithm-Part-of">
 * Investigating Myers' diff algorithm: Part 1 of 2
 * </a>
 * @see
 * <a href="http://www.codeproject.com/Articles/42280/Investigating-Myers-Diff-Algorithm-Part-of">
 * Investigating Myers' diff algorithm: Part 2 of 2
 * </a>
 * @see <a href="https://neil.fraser.name/software/diff_match_patch/myers.pdf">
 * An O(ND) Difference Algorithm and Its Variations, Myers 1986
 * </a>
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ReverseMyersLcs<T> implements Lcs<T> {

    @Override
    public List<T> lcs(List<T> a, List<T> b) {
        OneBasedVector<T> obvA = new OneBasedVector<>(a);
        OneBasedVector<T> obvB = new OneBasedVector<>(b);
        List<Snake> snakes = lcsMyers(obvA, obvB);
        return extractLcs(snakes, obvA);
    }

    private List<Snake> lcsMyers(OneBasedVector<T> a, OneBasedVector<T> b) {
        final int n = a.size();
        final int m = b.size();
        final int dmax = n + m;
        final int delta = n - m;

        BidirectionalArray vv = new BidirectionalArray(dmax);
        BidirectionalVector v = new BidirectionalVector(dmax);

        v.set(delta-1, n);

        int next, prev, x, y;
        for (int d = 0; d <= dmax; d++) {
            for (int k = -d+delta; k <= d+delta; k += 2) {
                next = v.get(k + 1); // left
                prev = v.get(k - 1); // up
                if (k == d+delta || (k != -d+delta && prev < next)) {
                    x = prev;   // up
                } else {
                    x = next - 1;   // left
                }
                y = x - k;
                while (x > 0 && y > 0 && x <= n && y <= m &&
                        a.get(x).equals(b.get(y))) {
                    x--;
                    y--;
                }
                v.set(k, x);
                if (x <= 0 && y <= 0) {
                    vv.copy(d, v);

//                    int lcs = (dmax - d) / 2;
//                    System.out.println("LCS=" + lcs);
//                    System.out.println("D=" + d);
//                    System.out.println("K=" + k);
//                    System.out.println("DELTA=" + delta);
//                    System.out.println("x=" + x);
//                    System.out.println("y=" + y);
//                    System.out.println(vv.toString());

                    return calculateSolution(d, vv, x, y, delta, n, m);
                }
            }
            vv.copy(d, v);
        }
        return Collections.<Snake>emptyList();
    }

    private List<Snake> calculateSolution(int lastD,
            BidirectionalArray vs, int xx, int yy, int delta, int n, int m) {
        List<Snake> snakes = new ArrayList<>();

        int xStart = xx;
        int yStart = yy;

        int d, next, prev, xMid, yMid, xEnd, yEnd;
        for (d = lastD; d >= 0 && xStart < n && yStart < m; d--) {
            int k = xStart - yStart;

            next = vs.get(d - 1, k + 1);
            prev = vs.get(d - 1, k - 1);
            if (k == d+delta || (k != -d+delta && prev != 0 && prev < next)) {
                xEnd = prev;
                yEnd = prev - k + 1;
                xMid = xEnd;
            } else {
                xEnd = next;
                yEnd = next - k - 1;
                xMid = xEnd - 1;
            }
            yMid = xMid - k;

            snakes.add(new Snake(xStart, yStart, xMid, yMid, xEnd, yEnd));

            xStart = xEnd;
            yStart = yEnd;
        }
        return snakes;
    }

    /** @return the common subsequence elements. */
    private List<T> extractLcs(List<Snake> snakes, OneBasedVector<T> a) {
        List<T> list = new ArrayList<>();
        for (Snake snake : snakes) {
            System.out.println(snake);
            for (int x=snake.xStart + 1; x<=snake.xMid; x++) {
                list.add(a.get(x));
            }
        }
        return list;
    }

    /**
     * Records each snake in the solution. A snake is a sequence of
     * equal elements starting from mid to end and preceeded by a vertical
     * or horizontal edge going from start to mid.
     */
    static class Snake {

        public final int xStart, yStart, xMid, yMid, xEnd, yEnd;

        public Snake(int xStart, int yStart, int xMid, int yMid, int xEnd,
                int yEnd) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.xMid = xMid;
            this.yMid = yMid;
            this.xEnd = xEnd;
            this.yEnd = yEnd;
        }

        @Override
        public String toString() {
            return "Snake{" + "xStart=" + xStart + ", yStart=" + yStart +
                    ", xMid=" + xMid + ", yMid=" + yMid + ", xEnd=" + xEnd +
                    ", yEnd=" + yEnd + '}';
        }
    }

    /** A vector that allows for negative indexes. */
    static class BidirectionalVector {

        private final int[] array;
        private final int halfSize;

        public BidirectionalVector(int[] array) {
            this.halfSize = array.length >> 1;
            this.array = array;
        }

        /**
         * @param size specify the positive size (the total size will be
         *             {@code size * 2 + 1}.
         */
        public BidirectionalVector(int size) {
            this.halfSize = size;
            this.array = new int[(halfSize << 1) + 1];
        }

        public int get(int x) {
            int index = halfSize + x;
            if (index < 0 || index >= array.length) {
                return 0;
            }
            return array[index];
        }

        public void set(int x, int value) {
            int index = halfSize + x;
            if (index < 0 || index >= array.length) {
                return;
            }
            array[index] = value;
        }

        @Override
        public String toString() {
            return ArraysUtil.printVector(array);
        }
    }

    /** An array that allows for negative indexes. */
    static class BidirectionalArray {

        private final int[][] array;
        private final int halfSize;

        public BidirectionalArray(int size) {
            this.halfSize = size;
            this.array = new int[halfSize][(halfSize << 1) + 1];
        }

        public int get(int x, int y) {
            if (x < 0 || x >= array.length) {
                return 0;
            }
            int indexY = halfSize + y;
            if (indexY < 0 || indexY >= array[x].length) {
                return 0;
            }
            return array[x][indexY];
        }

        public void set(int x, int y, int value) {
            if (x < 0 || x >= array.length) {
                return;
            }
            int indexY = halfSize + y;
            if (indexY < 0 || indexY >= array[x].length) {
                return;
            }
            array[x][indexY] = value;
        }

        public BidirectionalVector getVector(int x) {
            return new BidirectionalVector(array[x]);
        }

        public void copy(int line, BidirectionalVector v) {
            System.arraycopy(v.array, 0, array[line], 0, v.array.length);
        }

        @Override
        public String toString() {
            return ArraysUtil.printArray(array);
        }
    }

    /** A vector that starts from index 1 instead of 0. */
    static class OneBasedVector<T> {

        private final List<T> list;
        private final int size;

        public OneBasedVector(List<T> list) {
            this.list = list;
            this.size = list.size();
        }

        public T get(int x) {
            return list.get(x - 1);
        }

        public void set(int x, T value) {
            list.set(x - 1, value);
        }

        public int size() {
            return size;
        }
    }
}
