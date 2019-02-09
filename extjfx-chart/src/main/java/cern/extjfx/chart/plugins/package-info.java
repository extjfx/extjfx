/**
 * Provides the set of chart add-ons with capabilities such as zooming, panning or adding custom nodes to the chart.
 * <h2>CSS Styling</h2>
 * Nodes that are added by custom plugins can by styled using CSS class names as specified in JavaDoc of particular
 * plugin implementations. For example:
 * <pre>
 * {@code 
 * chart.getStylesheets().add("cern/mypackage/my-chart.css");
 * }
 * </pre>
 * 
 * with <code>my-chart.css</code> containing:
 * <pre>
 * {@code 
 *  .chart-crosshair-label {
 *      -fx-background-color: orange;
 *   }
 *   .chart-crosshair-path {
 *      -fx-stroke-width: 1;
 *      -fx-stroke-dash-array: 5 5;
 *   }
 * }
 * </pre>
 * will make the {@code CrosshairIndicator} coordinate's label background orange and lines dashed.
 */
package cern.extjfx.chart.plugins;
