/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import javafx.scene.chart.XYChart.Data;

/**
 * {@link DataReducer} implementation based on a modified version of
 * <a href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer-Douglas-Peucker
 * algorithm</a>.
 * <p>
 * Compared to the original algorithm that removes points that don't introduce error bigger than given tolerance, this
 * implementation takes as argument the desired number of points, removing from the curve the points in the order of
 * error size introduced i.e. points whose removal introduces smallest error are removed first. The algorithm stops once
 * the curve has the desired number of points.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public final class DefaultDataReducer<X extends Number, Y extends Number> implements DataReducer<X, Y> {

    private static final int MIN_POINTS_COUNT = 2;

    @Override
    public List<Data<X, Y>> reduce(ChartData<X, Y> data, Range<Double> dataRange, int maxPointsCount) {
        Objects.requireNonNull(data, "List of points must not be null");
        Objects.requireNonNull(dataRange, "The indexRange must not be null");
        if (maxPointsCount < MIN_POINTS_COUNT) {
            throw new IllegalArgumentException("Max number of points must be at least " + MIN_POINTS_COUNT);
        }

        if (data.size() == 0) {
            return Collections.emptyList();
        }

        return extractRemainingPoints(data, findRemainingIndices(data, toIndexRange(data, dataRange), maxPointsCount));
    }

    private Range<Integer> toIndexRange(ChartData<X, Y> data, Range<Double> dataRange) {
        int lowerBoundIndex = DataUtils.binarySearch(data, dataRange.getLowerBound());
        if (lowerBoundIndex < 0) {
            // Insertion index
            lowerBoundIndex = -lowerBoundIndex - 1;
            // Include previous point to draw a line from it
            lowerBoundIndex = Math.max(lowerBoundIndex - 1, 0);
        }

        int upperBoundIndex = DataUtils.binarySearch(data, dataRange.getUpperBound());
        if (upperBoundIndex < 0) {
            // Insertion index
            upperBoundIndex = -upperBoundIndex - 1;
            // Include next point to draw a line to it
            upperBoundIndex = Math.min(data.size() - 1, upperBoundIndex);
        }

        return new Range<>(lowerBoundIndex, upperBoundIndex);
    }

    private BitSet findRemainingIndices(ChartData<X, Y> data, Range<Integer> indexRange, int maxPointsCount) {
        BitSet remainingIndices = new BitSet(data.size());
        int pointsCountInRange = indexRange.getUpperBound() - indexRange.getLowerBound() + 1;
        if (pointsCountInRange <= maxPointsCount) {
            remainingIndices.set(indexRange.getLowerBound(), indexRange.getUpperBound() + 1);
            return remainingIndices;
        }

        remainingIndices.set(indexRange.getLowerBound());
        remainingIndices.set(indexRange.getUpperBound());

        Queue<RamerDouglasPeuckerRange> queue = new PriorityQueue<>(
                RamerDouglasPeuckerRange.FARTHEST_POINT_DISTANCE_INVERSED_COMPARATOR);
        queue.add(computeRange(data, indexRange.getLowerBound(), indexRange.getUpperBound()));

        while (remainingIndices.cardinality() < maxPointsCount) {
            RamerDouglasPeuckerRange range = queue.poll();
            remainingIndices.set(range.farthestPointIndex);
            if (!range.leftSubRangeEmpty()) {
                queue.add(computeRange(data, range.firstIndex, range.farthestPointIndex));
            }
            if (!range.rightSubRangeEmpty()) {
                queue.add(computeRange(data, range.farthestPointIndex, range.lastIndex));
            }
        }

        return remainingIndices;
    }

    private RamerDouglasPeuckerRange computeRange(ChartData<X, Y> data, int fromIndex, int toIndex) {
        double x1 = data.getXAsDouble(fromIndex);
        double y1 = data.getYAsDouble(fromIndex);
        double x2 = data.getXAsDouble(toIndex);
        double y2 = data.getYAsDouble(toIndex);
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        int farthestPointIndex = -1;
        double farthestPointDistance = -1;
        for (int i = fromIndex + 1; i < toIndex; ++i) {
            double px = data.getXAsDouble(i);
            double py = data.getYAsDouble(i);
            double distance = Math.abs(deltaY * px - deltaX * py + (x1 * y2) - (x2 * y1)) / length;
            if (distance > farthestPointDistance) {
                farthestPointIndex = i;
                farthestPointDistance = distance;
            }
        }
        return new RamerDouglasPeuckerRange(fromIndex, toIndex, farthestPointIndex, farthestPointDistance);
    }

    private List<Data<X, Y>> extractRemainingPoints(ChartData<X, Y> data, BitSet remainingIndices) {
        List<Data<X, Y>> remainingPoints = new ArrayList<>(remainingIndices.cardinality());
        for (int i = remainingIndices.nextSetBit(0); i >= 0; i = remainingIndices.nextSetBit(i + 1)) {
            remainingPoints.add(data.get(i));
        }
        return remainingPoints;
    }

    static class RamerDouglasPeuckerRange {
        static final Comparator<RamerDouglasPeuckerRange> FARTHEST_POINT_DISTANCE_INVERSED_COMPARATOR = Collections
                .reverseOrder(Comparator.comparing(RamerDouglasPeuckerRange::getFarthestPointDistance));

        final int firstIndex;
        final int lastIndex;
        // Index of point from this range that is farthest from the line connecting points at firstIndex and lastIndex
        final int farthestPointIndex;
        final double farthestPointDistance;

        RamerDouglasPeuckerRange(int first, int last, int farthestPointIndex, double farthestPointDistance) {
            this.firstIndex = first;
            this.lastIndex = last;
            this.farthestPointIndex = farthestPointIndex;
            this.farthestPointDistance = farthestPointDistance;
        }

        boolean leftSubRangeEmpty() {
            return farthestPointIndex == firstIndex + 1;
        }

        boolean rightSubRangeEmpty() {
            return farthestPointIndex == lastIndex - 1;
        }

        double getFarthestPointDistance() {
            return farthestPointDistance;
        }
    }
}
