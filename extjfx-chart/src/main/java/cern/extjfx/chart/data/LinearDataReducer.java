/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.XYChart.Data;

/**
 * Implementation of the DataReducer that reduces a large data set to the number of visible pixels
 * <p>
 * <ul>
 * <li>The algorithm goes through the data along the X axis from xMin to xMax. It typically merges the value of several
 * original points into two pixel value, by picking the minimum and maximum Y value from the original data.</li>
 * <li>The original data can have "holes", i.e. no data points between two X values. In this case, the algorithm
 * interpolates data in between.</li>
 * <li>If the size of the data set is smaller than the number of pixels, it is not reduced. The resulting "reduced" data
 * therefore contains the same number of points as the original, which may be fewer than the pixels.</li>
 * </ul>
 * 
 * @author Stephane Bart Pedersen, Vito Baggiolini
 * @param <X>
 * @param <Y>
 */
public class LinearDataReducer<X extends Number, Y extends Number> implements DataReducer<Number, Number>{

    @Override
    public List<Data<Number, Number>> reduce(ChartData<Number, Number> data, 
                                             Range<Double> dataRange,
                                             int maxPointsCount) {
        return reduce(toXpoints(data), toYpoints(data), dataRange.getLowerBound(), dataRange.getUpperBound(), maxPointsCount);
    }
        
    private int get_index_start_from_xmin(final double [] xIn, final double xMin) {
        
        int ind_start = -1;
        
        for (int i = 0; i < xIn.length; i++) {  
            
            if (xIn[i] >= xMin && ind_start == -1) {
                
                ind_start = i;
                i = xIn.length;
                
            }
            
        }
        
        return ind_start;
        
    }
    
    private int get_index_end_before_xmax(final double [] xIn, final double xMax) {
        
        int ind_end = -1;
        
        for (int i = xIn.length - 1; i >= 0; i--) { 
            
            if (xIn[i] <= xMax && ind_end == -1) {
                
                ind_end = i;
                i = 0;
                
            }
            
        }
        
        return ind_end;
        
    }
    
    private int get_pixel_x_index_from_value_array(final double width_screen, 
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
        
        int ind_start = get_index_start_from_xmin(xIn, xMin);
        int ind_end   = get_index_end_before_xmax(xIn, xMax);
        
        if (ind_start == -1) {           
            return copyToResultData(xOut, yOut, width_scene);
        }
        
        List<Double> xOut_tmp = new ArrayList<>();
        List<Double> yOut_tmp = new ArrayList<>();      
        
        int xstart = ind_start;

        //Select data (compressed) per pixel 0-w
        for (int i = 0; i < width_scene; i++) {
            
            List<Double> v_xMergedToPixel = new ArrayList<>();
            List<Double> v_yMergedToPixel = new ArrayList<>();
          
            for (int k = xstart; k < ind_end; k++) {
                
                int xscreen = get_pixel_x_index_from_value_array(width_scene, xIn[k], xMin, xMax);
                
                if (xscreen == i) {
                    
                    v_xMergedToPixel.add(xIn[k]);
                    v_yMergedToPixel.add(yIn[k]);
                    
                }
                else {
                    
                    xstart = k;
                    k = ind_end;
                    
                }
                
            }
            
            //No points found then add MAX_VALUE to be processed afterward
            if (v_xMergedToPixel.size() == 0) {
                
                xOut_tmp.add((xMin + i * (xMax - xMin) / width_scene));
                yOut_tmp.add(Double.MAX_VALUE);
                
            }
            //Only 1 points added
            else if (v_xMergedToPixel.size() == 1) {
                
                xOut_tmp.add(v_xMergedToPixel.get(0));
                yOut_tmp.add(v_yMergedToPixel.get(0));
                
            }
            //Only 2 points added
            else if (v_xMergedToPixel.size() == 2) {
                
                xOut_tmp.add(v_xMergedToPixel.get(0));
                yOut_tmp.add(v_yMergedToPixel.get(0));
                xOut_tmp.add(v_xMergedToPixel.get(1));
                yOut_tmp.add(v_yMergedToPixel.get(1));
                
                
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
                        
                        xOut_tmp.add(v_xMergedToPixel.get(0));
                        yOut_tmp.add(v_yMergedToPixel.get(0));
                        
                    }
 
                    xOut_tmp.add(v_xMergedToPixel.get(imin));
                    yOut_tmp.add(v_yMergedToPixel.get(imin));
                    xOut_tmp.add(v_xMergedToPixel.get(imax));
                    yOut_tmp.add(v_yMergedToPixel.get(imax));
                    
                    if (v_yMergedToPixel.get(imax) != v_yMergedToPixel.get(v_yMergedToPixel.size() - 1)) {
                        
                        xOut_tmp.add(v_xMergedToPixel.get(v_xMergedToPixel.size() - 1));
                        yOut_tmp.add(v_yMergedToPixel.get(v_yMergedToPixel.size() - 1));
                        
                    }

                }
                else {
                    
                    if (v_yMergedToPixel.get(imax) != v_yMergedToPixel.get(0)) {
                        
                        xOut_tmp.add(v_xMergedToPixel.get(0));
                        yOut_tmp.add(v_yMergedToPixel.get(0));
                        
                    }
                    
                    xOut_tmp.add(v_xMergedToPixel.get(imax));
                    yOut_tmp.add(v_yMergedToPixel.get(imax));
                    xOut_tmp.add(v_xMergedToPixel.get(imin));
                    yOut_tmp.add(v_yMergedToPixel.get(imin));
                    
                    if (v_yMergedToPixel.get(imin) != v_yMergedToPixel.get(v_yMergedToPixel.size() - 1)) {
                        
                        xOut_tmp.add(v_xMergedToPixel.get(v_xMergedToPixel.size() - 1));
                        yOut_tmp.add(v_yMergedToPixel.get(v_yMergedToPixel.size() - 1));
                        
                    }
                    
                }
                
            }  
            
        }
        
        //First point to be added if nothing found (example : zoom)
        if (yOut_tmp.get(0) == Double.MAX_VALUE) {
            
            if (ind_start != 0) {
                
                //New points added = interpolation
                double valy = yIn[ind_start - 1] + 
                        (yIn[ind_start] - yIn[ind_start - 1]) * 
                            (xOut_tmp.get(0) - xIn[ind_start - 1]) / 
                                (xIn[ind_start] - xIn[ind_start - 1]);
                xOut_tmp.set(0, valy);
                
           }
            
        }
        
        //Check whether we have pixel with no points (MAX_VALUE)
        for (int i = 1; i < xOut_tmp.size(); i++) {
            
            ind_end = -1;
            
            if (yOut_tmp.get(i) == Double.MAX_VALUE) {
                
                for (int j = i; j < xOut_tmp.size(); j++) {
                    
                    if (yOut_tmp.get(j) != Double.MAX_VALUE) {
                        
                        ind_end = j;
                        j = xOut_tmp.size();
                        
                    }
                    
                }
                
                if (ind_end != -1) {}
                else {
                    
                    //No points at the end then remove (no need)
                    for (int j = i; j < xOut_tmp.size(); j++) {
                        
                        xOut_tmp.remove(xOut_tmp.size() - 1);
                        yOut_tmp.remove(yOut_tmp.size() - 1);
                        
                    }
                    
                    i = xOut_tmp.size();
                    
                }
                
            }
            
        }     
        
        //Remove unused points
        for (int i = 0; i < xOut_tmp.size(); i++) {
            
            if (yOut_tmp.get(i) != Double.MAX_VALUE) {
                
                xOut.add(xOut_tmp.get(i));
                yOut.add(yOut_tmp.get(i));
                
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
