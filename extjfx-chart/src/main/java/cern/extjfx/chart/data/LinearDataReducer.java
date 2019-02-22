/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.XYChart.Data;

/**
 * Implementation of the DataReducer that reduces a large data set to the number of visible pixels
 * <ul>
 * <li>The algorithm goes through the data along the X axis from xMin to xMax. It typically merges the value of several
 * original points into two pixel value, by picking the minimum and maximum Y value from the original data.</li>
 * <li>The original data can have "holes", i.e. no data points between two X values. In this case, the algorithm
 * interpolates data in between.</li>
 * <li>If the size of the data set is smaller than the number of pixels, it is not reduced. The resulting "reduced" data
 * therefore contains the same number of points as the original, which may be fewer than the pixels.</li>
 * </ul>
 * 
 * @author Stephane Bart Pedersen
 * @author Vito Baggiolini
 */
public class LinearDataReducer<X extends Number, Y extends Number> implements DataReducer<Number, Number>{

    @Override
    public List<Data<Number, Number>> reduce(ChartData<Number, Number> data, 
                                             Range<Double> dataRange,
                                             int maxPointsCount) {
        return reduce(toXpoints(data), toYpoints(data), dataRange.getLowerBound(), dataRange.getUpperBound(), maxPointsCount);
    }
        
    private int getIndexStartFromXmin(final double [] xIn, final double xMin) {
        int indStart = -1;
        for (int i = 0; i < xIn.length; i++) {  
            
            if (xIn[i] >= xMin && indStart == -1) {
                indStart = i;
                i = xIn.length;
            }
        }
        return indStart;
    }
    
    private int getIndexEndBeforeXmax(final double [] xIn, final double xMax) {
        int indEnd = -1;
        for (int i = xIn.length - 1; i >= 0; i--) { 
            if (xIn[i] <= xMax && indEnd == -1) {
                indEnd = i;
                i = 0;
            }
        }
        return indEnd;
    }
    
    private int getPixelXIndexFromValueArray(final double width_screen, 
                                                   final double x_value, 
                                                   final double min_x_value_on_screen, 
                                                   final double max_x_value_on_screen) {        
        return (int)(width_screen * (x_value - min_x_value_on_screen) / (max_x_value_on_screen - min_x_value_on_screen));
    }
        
    /**
     * Algorithm that actually does the work.
     * 
     * @param xIn x-axis input data (original), must have the same length as {@code yIn}
     * @param yIn y-axis input data (original), must have the same length as {@code xIn}
     * @param xMin lower bound of data to be reduced, given in original x units. By default this is the first value of
     *            xIn, i.e. {@code xIn[0]}
     * @param xMax upper bound of data to be reduced, given in original x units. By default this is last value of xIn,
     *            i.e. {@code xIn[xIn.length-1]}
     * @param width_scene in pixels = number of points the reduced data shall have
     * @return list containing reduced data, with maximum {@code width_scene} elements
     */
    public List<Data<Number, Number>> reduce(double [] xIn, 
                                             double [] yIn, 
                                             final double xMin, 
                                             final double xMax, 
                                             final int width_scene) {
        
        //output data
        List<Double> xOut = new ArrayList<>();
        List<Double> yOut = new ArrayList<>();
                    
        //No compression if width null or greater than the array size
        if (width_scene == 0 || xIn.length < (2 * width_scene)) {
            xOut = new ArrayList<>();
            yOut = new ArrayList<>();        
            
            for (int i = 0; i < xIn.length; i++) {  
                xOut.add(xIn[i]);
                yOut.add(yIn[i]);
            }
            return copyToResultData(xOut, yOut, width_scene);
        }
        
        int indStart = getIndexStartFromXmin(xIn, xMin);
        int indEnd   = getIndexEndBeforeXmax(xIn, xMax);
        
        if (indStart == -1) {           
            return copyToResultData(xOut, yOut, width_scene);
        }
        
        List<Double> xOutTmp = new ArrayList<>();
        List<Double> yOutTmp = new ArrayList<>();      
        
        int xstart = indStart;

        //Select data (compressed) per pixel 0-w
        for (int i = 0; i < width_scene; i++) {
            List<Double> v_xMergedToPixel = new ArrayList<>();
            List<Double> v_yMergedToPixel = new ArrayList<>();
          
            for (int k = xstart; k < indEnd; k++) {
                int xscreen = getPixelXIndexFromValueArray(width_scene, xIn[k], xMin, xMax);
                
                if (xscreen == i) {
                    v_xMergedToPixel.add(xIn[k]);
                    v_yMergedToPixel.add(yIn[k]);
                } else {
                    xstart = k;
                    k = indEnd;
                }
            }
            
            //No points found then add MAX_VALUE to be processed afterward
            if (v_xMergedToPixel.size() == 0) {
                xOutTmp.add((xMin + i * (xMax - xMin) / width_scene));
                yOutTmp.add(Double.MAX_VALUE);
            }
            //Only 1 points added
            else if (v_xMergedToPixel.size() == 1) {
                
                xOutTmp.add(v_xMergedToPixel.get(0));
                yOutTmp.add(v_yMergedToPixel.get(0));
            }
            //Only 2 points added
            else if (v_xMergedToPixel.size() == 2) {
                
                xOutTmp.add(v_xMergedToPixel.get(0));
                yOutTmp.add(v_yMergedToPixel.get(0));
                xOutTmp.add(v_xMergedToPixel.get(1));
                yOutTmp.add(v_yMergedToPixel.get(1));
            }
            //More than 2 points then just add the min and max
            else if (v_xMergedToPixel.size() > 2) {
                
                double yminloc = Double.MAX_VALUE;
                double ymaxloc = -Double.MAX_VALUE;
                
                int imin = -1;
                int imax = -1;
                for (int j = 0; j < v_xMergedToPixel.size(); j++) {
                    if (yminloc > v_yMergedToPixel.get(j)) {
                        yminloc = v_yMergedToPixel.get(j);
                        imin = j;
                    }
                    
                    if (ymaxloc < v_yMergedToPixel.get(j)) {
                        ymaxloc = v_yMergedToPixel.get(j);
                        imax = j;
                    }
                }
                
                if (imin <= imax) {
                    
                    if (v_yMergedToPixel.get(imin) != v_yMergedToPixel.get(0)) {
                        xOutTmp.add(v_xMergedToPixel.get(0));
                        yOutTmp.add(v_yMergedToPixel.get(0));
                    }
 
                    xOutTmp.add(v_xMergedToPixel.get(imin));
                    yOutTmp.add(v_yMergedToPixel.get(imin));
                    xOutTmp.add(v_xMergedToPixel.get(imax));
                    yOutTmp.add(v_yMergedToPixel.get(imax));
                    
                    if (v_yMergedToPixel.get(imax) != v_yMergedToPixel.get(v_yMergedToPixel.size() - 1)) {
                        
                        xOutTmp.add(v_xMergedToPixel.get(v_xMergedToPixel.size() - 1));
                        yOutTmp.add(v_yMergedToPixel.get(v_yMergedToPixel.size() - 1));
                    }
                }
                else {
                    
                    if (v_yMergedToPixel.get(imax) != v_yMergedToPixel.get(0)) {
                        xOutTmp.add(v_xMergedToPixel.get(0));
                        yOutTmp.add(v_yMergedToPixel.get(0));
                    }
                    
                    xOutTmp.add(v_xMergedToPixel.get(imax));
                    yOutTmp.add(v_yMergedToPixel.get(imax));
                    xOutTmp.add(v_xMergedToPixel.get(imin));
                    yOutTmp.add(v_yMergedToPixel.get(imin));
                    
                    if (v_yMergedToPixel.get(imin) != v_yMergedToPixel.get(v_yMergedToPixel.size() - 1)) {
                        xOutTmp.add(v_xMergedToPixel.get(v_xMergedToPixel.size() - 1));
                        yOutTmp.add(v_yMergedToPixel.get(v_yMergedToPixel.size() - 1));
                    }
                }
            }  
        }
        
        //First point to be added if nothing found (example : zoom)
        if (yOutTmp.get(0) == Double.MAX_VALUE) {
            if (indStart != 0) {
                //New points added = interpolation
                double valy = yIn[indStart - 1] + 
                        (yIn[indStart] - yIn[indStart - 1]) * 
                            (xOutTmp.get(0) - xIn[indStart - 1]) / 
                                (xIn[indStart] - xIn[indStart - 1]);
                xOutTmp.set(0, valy);
           }
        }
        
        //Check whether we have pixel with no points (MAX_VALUE)
        for (int i = 1; i < xOutTmp.size(); i++) {
            indEnd = -1;
            if (yOutTmp.get(i) == Double.MAX_VALUE) {
                for (int j = i; j < xOutTmp.size(); j++) {
                    if (yOutTmp.get(j) != Double.MAX_VALUE) {
                        indEnd = j;
                        j = xOutTmp.size();
                    }
                }
                
                if (indEnd != -1) {
                } else {
                    //No points at the end then remove (no need)
                    for (int j = i; j < xOutTmp.size(); j++) {
                        xOutTmp.remove(xOutTmp.size() - 1);
                        yOutTmp.remove(yOutTmp.size() - 1);
                    }
                    i = xOutTmp.size();
                }
            }
        }     
        
        //Remove unused points
        for (int i = 0; i < xOutTmp.size(); i++) {
            if (yOutTmp.get(i) != Double.MAX_VALUE) {
                xOut.add(xOutTmp.get(i));
                yOut.add(yOutTmp.get(i));
            }
        }
        return copyToResultData(xOut, yOut, width_scene);
    }               
  
    /**
     * @param maxPointsCount
     * @return
     */
    private static List<Data<Number, Number>> copyToResultData(List<Double> xOut, 
                                                               List<Double> yOut, 
                                                               int maxPointsCount) {
        
        List<Data<Number, Number>> result = new ArrayList<>(maxPointsCount);
        int outDataSize = Math.max(xOut.size(), yOut.size());  
        for (int i = 0; i < outDataSize; i++) {
            result.add(new Data<>(xOut.get(i), yOut.get(i)));
        }
        return result;
    }

    private static double [] toAxisPoints(ChartData<Number, Number> chData, boolean isX) {
        double [] res =  new double[chData.size()];
        for (int i = 0; i  < res.length; i++) {
            res[i] = isX ? chData.getXAsDouble(i) : chData.getYAsDouble(i);
        }
        return res;
    }

    private static double [] toXpoints(ChartData<Number, Number> chData) {
        return toAxisPoints(chData, true);
    }               

    private static double [] toYpoints(ChartData<Number, Number> chData) {
        return toAxisPoints(chData, false);
    }               
}
