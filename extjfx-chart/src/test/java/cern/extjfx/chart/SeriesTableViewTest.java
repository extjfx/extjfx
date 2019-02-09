/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.SeriesTableView;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.converter.NumberStringConverter;

@RunWith(FxJUnit4Runner.class)
public class SeriesTableViewTest {

    private static final Series<Number, Number> SERIES1 = createSeries1();

    private static Series<Number, Number> createSeries1() {
        Series<Number, Number> series = new Series<>();
        series.setName("Series1");
        series.getData().add(new Data<>(1.111, 2.222));
        return series;
    }
    
    private static final Series<Number, Number> SERIES2 = createSeries2();

    private static Series<Number, Number> createSeries2() {
        Series<Number, Number> series = new Series<>();
        series.setName("Series2");
        series.getData().add(new Data<>(3.333, 4.444));
        series.getData().add(new Data<>(5.555, 6.666));
        return series;
    }

    @Test
    public void testEmptyView() {
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>());
        assertNotNull(view.getData());
        assertEquals("#", getTableView(view).getColumns().get(0).getText());
    }
    
    @Test
    public void testSingleSeriesColumns() {
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(SERIES1));
        
        TableView<?> tableView = getTableView(view);
        assertEquals(2, tableView.getColumns().size());
        assertEquals(SERIES1.getName(), getSeriesColumn(view, 1).getText());
        assertEquals("X", getXColumn(view, 1).getText());
        assertEquals("Y", getYColumn(view, 1).getText());
    }
    
    @Test
    public void testSingleSeriesRows() {
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(SERIES1));
        
        TableView<?> tableView = getTableView(view);
        assertEquals(SERIES1.getData().size(), tableView.getItems().size());
                
        assertEquals(SERIES1.getData().get(0).getXValue(), getXColumn(view, 1).getCellData(0));
        assertEquals(SERIES1.getData().get(0).getYValue(), getYColumn(view, 1).getCellData(0));
    }

    @Test
    public void testSeriesListColumns() {
        ObservableList<Series<Number, Number>> seriesList = FXCollections.observableArrayList();
        seriesList.add(SERIES1);
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(seriesList));
        
        assertEquals(seriesList.size() + 1, getTableView(view).getColumns().size());
        seriesList.add(SERIES2);
        assertEquals(seriesList.size() + 1, getTableView(view).getColumns().size());

        assertEquals(SERIES1.getName(), getSeriesColumn(view, 1).getText());
        assertEquals(SERIES2.getName(), getSeriesColumn(view, 2).getText());
    }
    
    @Test
    public void testSeriesListRows() {
        ObservableList<Series<Number, Number>> seriesList = FXCollections.observableArrayList();
        seriesList.add(SERIES1);

        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(seriesList));
        
        assertEquals(SERIES1.getData().size(), getTableView(view).getItems().size());
        seriesList.add(SERIES2);
        assertEquals(SERIES2.getData().size(), getTableView(view).getItems().size());

        assertEquals(SERIES1.getData().get(0).getXValue(), getXColumn(view, 1).getCellData(0));
        assertEquals(SERIES2.getData().get(1).getYValue(), getYColumn(view, 2).getCellData(1));
    }
    
    @Test
    public void testDataChangeReflectedInTable() {
        Series<Number, Number> series = createSeries1();
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(series));
        
        Data<Number, Number> data = series.getData().get(0);
        data.setXValue(66);
        data.setYValue(77);
        
        assertEquals(data.getXValue(), getXColumn(view, 1).getCellData(0));
        assertEquals(data.getYValue(), getYColumn(view, 1).getCellData(0));
    }
    
    @Test
    public void testObservableValue() {
        SimpleObjectProperty<ObservableList<Series<Number, Number>>> obsValue = new SimpleObjectProperty<>();
        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(obsValue));
        
        TableView<?> tableView = getTableView(view);
        assertEquals(1, tableView.getColumns().size());
        ObservableList<Series<Number, Number>> seriesList = FXCollections.observableArrayList();
        seriesList.add(SERIES1);
        obsValue.set(seriesList);
        assertEquals(SERIES1.getName(), getSeriesColumn(view, 1).getText());
    }
    
    @Test
    public void testFormatters() {
        ObservableList<Series<Number, Number>> seriesList = FXCollections.observableArrayList();
        seriesList.add(SERIES1);
        seriesList.add(SERIES2);

        SeriesTableView<Number, Number> view = initSkin(new SeriesTableView<>(seriesList));
        NumberStringConverter defaultXFormatter = new NumberStringConverter("0");
        NumberStringConverter series2XFormatter = new NumberStringConverter("0.0");
        NumberStringConverter defaultYFormatter = new NumberStringConverter("0.00");
        NumberStringConverter series2YFormatter = new NumberStringConverter("0.0000");
        view.setXFormatter(defaultXFormatter);
        view.setXFormatter(series2XFormatter, SERIES2);
        view.setYFormatter(defaultYFormatter);
        view.setYFormatter(series2YFormatter, SERIES2);

        // Didn't find an easy way to get cell's text, so just testing setter/getter
        assertEquals(defaultXFormatter, view.getXFormatter());
        assertEquals(series2XFormatter, view.getXFormatter(SERIES2));
        assertEquals(defaultYFormatter, view.getYFormatter());
        assertEquals(series2YFormatter, view.getYFormatter(SERIES2));
    }
    
    
    /**
     * Adding the control to the Scene and triggering CSS creates skin and initialized TableView.
     */
    @SuppressWarnings("unused")
    private static <X, Y> SeriesTableView<X, Y> initSkin(SeriesTableView<X, Y> view) {
        new Scene(view, 100, 100);
        view.applyCss();
        return view;
    }

    private static TableView<?> getTableView(SeriesTableView<?, ?> view) {
        return (TableView<?>) view.getChildrenUnmodifiable().get(0);
    }
    
    private TableColumn<?, ?> getSeriesColumn(SeriesTableView<?, ?> view, int seriesNumber) {
        return getTableView(view).getColumns().get(seriesNumber);
    }
    
    private TableColumn<?, ?> getXColumn(SeriesTableView<?, ?> view, int seriesNumber) {
        return getSeriesColumn(view, seriesNumber).getColumns().get(0);
    }
    
    private TableColumn<?, ?> getYColumn(SeriesTableView<?, ?> view, int seriesNumber) {
        return getSeriesColumn(view, seriesNumber).getColumns().get(1);
    }
}
