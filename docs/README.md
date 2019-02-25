[![Build Status](https://travis-ci.com/extjfx/extjfx.svg?branch=master)](https://travis-ci.com/extjfx/extjfx)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# ExtJFX

ExtJFX is a small library developed at CERN containing features needed by our JavaFX applications that are not supported by the standard JavaFX toolkit. 
The library consists of 3 modules: 
- [extjfx-chart](#extjfx-chart): zooming, panning, data annotations, value/range indicators, chart decorations, overlaying different types of charts, etc.
- [extjfx-fxml](#extjfx-fxml): `FxmlView` class that simplifies loading FXML files using conventional names
- [extjfx-test](#extjfx-test): `FxJUnit4Runner` to execute GUI tests
- [extjfx-samples](#extjfx-samples): Executable jar with chart samples

#Build Artifacts 
* [JavaDoc](https://extjfx.github.io/extjfx/apidocs/)
* [Latest Release](https://github.com/extjfx/extjfx/releases/latest)

## extjfx-chart
#### XYChartPane
The central class of the `cern.extjfx.chart` package is [XYChartPane](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/XYChartPane.html). It is a container that can hold one or more instances of [XYChart](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/XYChart.html) (e.g. [LineChart](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/LineChart.html), [AreaChart](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/AreaChart.html), [BarChart](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/BarChart.html)). `XYChartPane` brings support for overlaying different chart types on top of each other (as on the figure below) and possibility to add chart plugins (instances of [XYChartPlugin](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/XYChartPlugin.html)) which can be either interacting components e.g. [Zoomer](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/Zoomer.html) or [Panner](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/Panner.html), or passive graphical elements drawn on the chart such as labels or data indicators.

![An example of overlay charts: AreaChart, LineChart and ScatterChart with independent axes for each chart](pics/mixed-chart-types.png?raw=true "Different types of charts overlaid")

<details><summary>The corresponding source code (expand)</summary>

```
public class MixedChartSample extends Application {
    private static final List<String> DAYS = new ArrayList<>(
            Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Mixed Chart Sample");

        BarChart<String, Number> barChart = new BarChart<>(createXAxis(), createYAxis());
        barChart.getStyleClass().add("chart1");
        barChart.setAnimated(false);
        barChart.getYAxis().setLabel("Data 1");
        barChart.getYAxis().setSide(Side.LEFT);
        barChart.getData().add(new Series<>("Data 1", createTestData(3)));

        LineChart<String, Number> lineChart = new LineChart<>(createXAxis(), createYAxis());
        lineChart.getStyleClass().add("chart2");
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.getYAxis().setLabel("Data 2");
        lineChart.getYAxis().setSide(Side.RIGHT);
        lineChart.getData().add(new Series<>("Data 2", createTestData(10)));
        
        ScatterChart<String, Number> scatterChart = new ScatterChart<>(createXAxis(), createYAxis());
        scatterChart.getStyleClass().add("chart3");
        scatterChart.setAnimated(false);
        scatterChart.getYAxis().setLabel("Data 3");
        scatterChart.getYAxis().setSide(Side.RIGHT);
        scatterChart.getData().add(new Series<>("Data 3", createTestData(20)));

        XYChartPane<String, Number> chartPane = new XYChartPane<>(barChart);
        chartPane.setTitle("Mixed chart types");
        chartPane.setCommonYAxis(false);
        chartPane.getOverlayCharts().addAll(lineChart, scatterChart);
        chartPane.getPlugins().addAll(new CrosshairIndicator<>(), new DataPointTooltip<>());
        chartPane.getStylesheets().add("mixed-chart-sample.css");

        BorderPane borderPane = new BorderPane(chartPane);
        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private NumericAxis createYAxis() {
        NumericAxis yAxis = new NumericAxis();
        yAxis.setAnimated(false);
        yAxis.setForceZeroInRange(false);
        yAxis.setAutoRangePadding(0.1);
        yAxis.setAutoRangeRounding(true);
        return yAxis;
    }

    private CategoryAxis createXAxis() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);
        return xAxis;
    }

    private ObservableList<Data<String, Number>> createTestData(double refVal) {
        Random rnd = new Random();
        List<Data<String, Number>> data = new ArrayList<>();
        for (int i = 0; i < DAYS.size(); i++) {
            data.add(new Data<>(DAYS.get(i), refVal - Math.abs(3 - i) + rnd.nextDouble()));
        }
        return FXCollections.observableArrayList(data);
    }    
    
    public static void main(String[] args) {
        launch(args);
    }
}
```
</details>

<details><summary>The corresponding CSS (expand)</summary>

```
.chart1 .chart-bar { -fx-bar-fill: #22bad9; }
.chart1 .axis:left { -fx-tick-label-fill: #22bad9; }
.chart1 .axis:left .axis-label { -fx-text-fill: #22bad9; }

.chart2 .axis:right { -fx-tick-label-fill: #c62b00; }
.chart2 .axis:right .axis-label { -fx-text-fill: #c62b00; }
.chart2 .chart-series-line { -fx-stroke: #c62b00; }
.chart2 .chart-line-symbol { -fx-background-color: #c62b00, white; }

.chart3 .axis:right { -fx-tick-label-fill: green; }
.chart3 .axis:right .axis-label { -fx-text-fill: green; }
.chart3 .chart-symbol { 
    -fx-background-color: green;
    -fx-background-radius: 0;
    -fx-padding: 7px 5px 7px 5px;
    -fx-shape: "M5,0 L10,9 L5,18 L0,9 Z";
}
```
</details>

The `XYChartPane` allows having a single (shared) Y axis or distinct axes, one per overlaid chart. 

Note that in order to draw charts properly on top of each other some properties of the overlaid charts are overridden - see JavaDoc of [XYChartPane](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/XYChartPane.html) for details.

#### Chart Plugins 

Chart plugins are add-ons to the standard charts that can be added to the XYChartPane to either interact with chart content or to decorate it.
Currently the package provides the following plugins:
- [ChartOverlay](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/ChartOverlay.html) allows adding any node on top of the chart area. 
- [CrosshairIndicator](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/CrosshairIndicator.html) a cross (horizontal and vertical line) following mouse cursor and displaying current coordinates
- [DataPointTooltip](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/DataPointTooltip.html) a tooltip label displaying coordinates of the data point hovered by the mouse cursor
- [Zoomer](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/Zoomer.html) zooms the plot area to the dragged rectangle
- [Panner](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/Panner.html) allows dragging the visible data window with mouse cursor
- [XValueIndicator](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/XValueIndicator.html) and [YValueIndicator](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/YValueIndicator.html) a vertical or horizontal line (accordingly) indicating specified X or Y value, with optional text label that can be used to describe the indicated value
- [XRangeIndicator](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/XRangeIndicator.html) and [YRangeIndicator](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/plugins/YRangeIndicator.html) a rectangle indicating vertical or horizontal range (accordingly) of X or Y values, with optional text label that can be used to describe the indicated range

The following example presents all plugins on a single chart pane.

<details><summary>Source code (expand)</summary>

```
public class PluginsSample extends Application {

  @Override
    public void start(Stage stage) {
        stage.setTitle("Plugins Sample");
        
       NumericAxis xAxis = new NumericAxis();
        xAxis.setLabel("X Values");
         
        NumericAxis yAxis = new NumericAxis();
        yAxis.setAutoRangePadding(0.1);
        yAxis.setLabel("Y Values");
         
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getData().add(new Series<>("Test Data", createTestData()));
         
        XYChartPane<Number, Number> chartPane = new XYChartPane<>(chart);
        XValueIndicator<Number> internalStop = new XValueIndicator<>(75, "Internal Stop");
        internalStop.setLabelPosition(0.95);
        chartPane.getPlugins().add(internalStop);
         
        YValueIndicator<Number> yMin = new YValueIndicator<>(-7.5, "MIN");
        yMin.setLabelPosition(0.1);
        YValueIndicator<Number> yMax = new YValueIndicator<>(7.5, "MAX");
        yMax.setLabelPosition(0.1);
        chartPane.getPlugins().addAll(yMin, yMax);
        XRangeIndicator<Number> xRange = new XRangeIndicator<>(40, 60, "X Range");
        xRange.setLabelVerticalPosition(0.95);
        chartPane.getPlugins().add(xRange);
         
        YRangeIndicator<Number> thresholds = new YRangeIndicator<>(-5, 5, "Thresholds");
        thresholds.setLabelHorizontalAnchor(HPos.RIGHT);
        thresholds.setLabelHorizontalPosition(0.95);
        thresholds.setLabelVerticalAnchor(VPos.TOP);
        thresholds.setLabelVerticalPosition(0.95);
        chartPane.getPlugins().add(thresholds);
         
        Label label = new Label("Label added to the chart pane\n using ChartOverlay");
        label.setStyle("-fx-background-color: rgba(255, 127, 80, 0.5)");
        AnchorPane.setLeftAnchor(label, 5.0);
        AnchorPane.setTopAnchor(label, 5.0);
        chartPane.getPlugins().add(new ChartOverlay<>(OverlayArea.PLOT_AREA, new AnchorPane(label)));
        chartPane.getPlugins().addAll(new Zoomer(), new Panner(), new CrosshairIndicator<>(), new DataPointTooltip<>());
        chartPane.getStylesheets().add(getClass().getResource("plugins-sample.css").toExternalForm());
  
        Scene scene = new Scene(chartPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private ObservableList<Data<Number, Number>> createTestData() {
        Random rnd = new Random(System.currentTimeMillis());
        List<Data<Number, Number>> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(new Data<>(i, (rnd.nextBoolean() ? 1 : -1) * 10 * rnd.nextDouble()));
        }
        return FXCollections.observableArrayList(data);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
 }
```
</details>

<details><summary>The associated CSS file (expand)</summary>

```
.x-value-indicator-label { 
	-fx-background-color: pink; 
}
.x-value-indicator-line  {
    -fx-stroke: pink;
    -fx-stroke-width: 2;
}
.x-range-indicator-rect {
    -fx-fill: rgba(173, 255, 47, 0.5);
}
 
.y-range-indicator-label {
    -fx-background-color: orange;
}
.y-range-indicator-rect {
    -fx-stroke: orange;
    -fx-fill: #416ef468;
}
.y-value-indicator-label {
    -fx-background-color: red;
}
.y-value-indicator-line {
    -fx-stroke: red;
}
```
</details>

The resulting chart:

![Plugins Example](pics/chart-plugins.png?raw=true "Chart Plugins")

#### NumericAxis and LogarithmicAxis

Note that in the examples above we used [cern.extjfx.chart.NumericAxis](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/NumericAxis.html) rather than [javafx.scene.chart.NumberAxis](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/NumberAxis.html) which does not support proper recalculation of tick units with auto-range being switched off (necessary behavior for `Zoomer` and `Panner` to work properly).

In addition to the [NumericAxis](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/NumericAxis.html) the package contains also [LogarithmicAxis](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/LogarithmicAxis.html) with a configurable logarithm base (by default 10):

```
NumericAxis xAxis = new NumericAxis();
LogarithmicAxis yAxis = new LogarithmicAxis();
LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
lineChart.setTitle("Test data");
...
```

![Logarithmic Axis](pics/log-axis.png?raw=true "Logarithmic Axis")

#### HeatMapChart
[HeatMapChart](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/HeatMapChart.html) is a specialized chart that uses colors to represent data values. 
The following figure presents a particle beam image rendered using `HeatMapChart`.

![Beam Image](pics/beam-image.png?raw=true "HeatMapChart")

<details><summary>Source code (expand)</summary>

```
NumericAxis xAxis = new NumericAxis();
xAxis.setAnimated(false);
xAxis.setAutoRangeRounding(false);
xAxis.setLabel("X Position");
 
NumericAxis yAxis = new NumericAxis();
yAxis.setAnimated(false);
yAxis.setAutoRangeRounding(false);
yAxis.setLabel("Y Position");
 
HeatMapChart<Number, Number> chart = new HeatMapChart<>(xAxis, yAxis);
chart.setTitle("Beam Image");
 
// readImage() creates a DefaultData class containing X, Y and Z values
chart.setData(readImage());
chart.setLegendVisible(true);
chart.setLegendSide(Side.RIGHT);
```
</details>

By default the `HeatMapChart` uses a *rainbow* colors gradient but this can be changed using [colorGradient](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/HeatMapChart.html#colorGradientProperty--) property (see JavaDoc for details). 

The chart can be also used in combination with JavaFX [CategoryAxis](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/CategoryAxis.html):

![HeatMapChart with CategoryAxis](pics/heatmap-category.png?raw=true "HeatMapChart with CategoryAxis")

<details><summary>Source code (expand)</summary>

```
@Override
public void start(Stage primaryStage) {
    primaryStage.setTitle("HeatMapChart Category Sample");
    
    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel("Week Days");
    CategoryAxis yAxis = new CategoryAxis();
    yAxis.setLabel("Teams");
    
    HeatMapChart<String, String> chart = new HeatMapChart<>(xAxis, yAxis);
    chart.setTitle("Avg #coffees per Person");
    chart.setColorGradient(ColorGradient.BLUE_RED);
    chart.setData(createData());
    chart.setLegendVisible(true);
    chart.setLegendSide(Side.RIGHT);
     
    Scene scene = new Scene(chart, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
}
 
private static Data<String, String> createData() {
    String[] team = {"A", "B", "C", "D", "E"};
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
     
    Random rnd = new Random();
    double[][] coffees = new double[days.length][team.length];
    for (int i = 0; i < days.length; i++) {
        for (int j = 0; j < team.length; j++) {
            coffees[i][j] = 3 * rnd.nextDouble();
        }
    }
    return new DefaultData<>(days, team, coffees);
}
```
</details>

#### Dealing With Large Data Sets

The JavaFX charting package performs well with series containing up to a few thousands data points, with rendering time below one second (on a decent desktop computer). 
However, drawing series containing tens of thousands points takes several seconds, blocking the FX thread and making the application unresponsive.

To overcome the performance issues, the extjfx-chart package provides [DataReducingObservableList](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/data/DataReducingObservableList.html) that performs data reduction to the specified number of points within the given X data range. i.e. it reduces only the part of initial data set that is currently visible on the chart.
This means that while performing a zoom-in, one can see "more details" in the interesting region. 

By default [DataReducingObservableList](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/data/DataReducingObservableList.html) uses  [DefaultDataReducer](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/data/DefaultDataReducer.html) that is an implementation of Ramer-Douglas-Peucker algorithm - sufficiently fast and giving desired results in majority of cases. If zooming-in is not needed, the `DefaultDataReducer` can be used directly to filter the data, before it is passed to the chart. As the alternative, you can also use [LinearDataReducer](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/chart/data/LinearDataReducer.html).

<details><summary>Example source code (expand)</summary>

```
NumericAxis xAxis = new NumericAxis();
xAxis.setAnimated(false);
NumericAxis yAxis = new NumericAxis();
yAxis.setAnimated(false);
LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
lineChart.setTitle("Test data");
 
DataReducingObservableList<Number, Number> reducedData = new DataReducingObservableList<>(xAxis);
lineChart.getData().add(new Series<>("Random data", reducedData));
 
ArrayData<Number, Number> sourceData =  ArrayData.of(RandomDataGenerator.generateArrayData(0, 1, MAX_NUMBER_OF_POINTS, 0)); 
reducedData.setData(sourceData);
```
</details>

## extjfx-fxml

The package contains the [FxmlView](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/fxml/FxmlView.html) class that for a that for a given controller (class or instance) loads corresponding FXML file and applies associated CSS file (if present). The FXML and CSS files are searched in the same package as controller's class and are expected to have the same conventional name i.e. for `MainController`, they should be called `Main.fxml` and `Main.css` respectively.

The class supports also loading corresponding resource bundle file (if present) that is expected to follow the same
naming convention e.g.`Main_en_US.properties`. The package structure would look in the following way:
```
com.mycompany.myapp.MainController.java
com.mycompany.myapp.Main.fxml
com.mycompany.myapp.Main.css
com.mycompany.myapp.Main_en_US.properties
```

Example usage:
```
FxmlView mainView = new FxmlView(MainController.class);
Scene scene = new Scene(mainView.getRootNode(), 400, 400);
// ...
MainController mainController = mainView.getController();
mainController.doSomething();
```

Controllers are instantiated using configured controller factory that by default is
initialized to [DefaultControllerFactory.createController(Class)](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/fxml/DefaultControllerFactory.html#createController-java.lang.Class-) which supports basic Dependency Injection (see [JavaDoc](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/fxml/DefaultControllerFactory.html) for details).

The `FxmlView` class was inspired by Adam Bien's `FXMLView` from the [afterburner.fx](http://afterburner.adam-bien.com) framework, 
with the difference that it doesn't require a separate view class per FXML.

## extjfx-test

Contains [FxJUnit4Runner](https://extjfx.github.io/extjfx/apidocs/cern/extjfx/test/FxJUnit4Runner.html) - a JUnit runner to execute JavaFX tests:
```
@RunWith(FxJUnit4Runner.class)
public class MyControlTest {

    @Test
    public void testInJUnitThread() {
        // Do testing
    }

    @Test
    @RunInFxThread
    public void testInJavaFXThread() {
        // Do testing
    }
}
```
## extjfx-samples

Sample application with chart examples: zooming, panning, decorations, HeatMapChart, etc. 
Download the [executable jar](https://github.com/extjfx/extjfx/releases/download/v0.0.1/extjfx-samples-0.0.1.jar) with samples and run:

```
java -jar extjfx-samples-[version].jar
```

