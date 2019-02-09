/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.extjfx.chart.SeriesTableView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Default skin of the SeriesTableView.
 *
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class SeriesTableViewSkin<X, Y> extends SkinBase<SeriesTableView<X, Y>> {

    private final TableView<Row> tableView;

    /**
     * Creates a new instance of SeriesTableViewSkin.
     *
     * @param seriesTableView the control
     */
    public SeriesTableViewSkin(SeriesTableView<X, Y> seriesTableView) {
        super(seriesTableView);

        tableView = new TableView<>();
        getChildren().add(tableView);

        seriesTableView.dataProperty().addListener(dataPropertyListener);
        addSeriesListChangeListener(seriesTableView.getData());

        refreshTable();
    }

    private final ChangeListener<ObservableList<Series<X, Y>>> dataPropertyListener = new ChangeListener<ObservableList<Series<X, Y>>>() {
        @Override
        public void changed(ObservableValue<? extends ObservableList<Series<X, Y>>> dataProperty,
                ObservableList<Series<X, Y>> oldData, ObservableList<Series<X, Y>> newData) {

            removeSeriesListChangeListener(oldData);
            addSeriesListChangeListener(newData);
            refreshTable();
        }
    };

    private void removeSeriesListChangeListener(ObservableList<Series<X, Y>> seriesList) {
        if (seriesList != null) {
            seriesList.removeListener(seriesListChangeListener);
            removeSeriesDataPropertyListener(seriesList);
        }
    }

    private void addSeriesListChangeListener(ObservableList<Series<X, Y>> seriesList) {
        if (seriesList != null) {
            seriesList.addListener(seriesListChangeListener);
            addSeriesDataPropertyListener(seriesList);
        }
    }

    private final ListChangeListener<Series<X, Y>> seriesListChangeListener = new ListChangeListener<Series<X, Y>>() {
        @Override
        public void onChanged(Change<? extends Series<X, Y>> change) {
            while (change.next()) {
                removeSeriesDataPropertyListener(change.getRemoved());
                addSeriesDataPropertyListener(change.getAddedSubList());
            }
            refreshTable();
        }
    };

    private void removeSeriesDataPropertyListener(List<? extends Series<X, Y>> series) {
        for (Series<X, Y> s : series) {
            s.dataProperty().removeListener(seriesDataPropertyListener);
            removeDataListChangeListener(s.getData());
        }
    }

    private void addSeriesDataPropertyListener(List<? extends Series<X, Y>> series) {
        for (Series<X, Y> s : series) {
            s.dataProperty().addListener(seriesDataPropertyListener);
            addDataListChangeListener(s.getData());
        }
    }

    private final ChangeListener<ObservableList<Data<X, Y>>> seriesDataPropertyListener = new ChangeListener<ObservableList<Data<X, Y>>>() {
        @Override
        public void changed(ObservableValue<? extends ObservableList<Data<X, Y>>> dataProperty,
                ObservableList<Data<X, Y>> oldData, ObservableList<Data<X, Y>> newData) {

            removeDataListChangeListener(oldData);
            addDataListChangeListener(newData);
            refreshRows();
        }
    };

    private void removeDataListChangeListener(ObservableList<Data<X, Y>> data) {
        if (data != null) {
            data.removeListener(dataListChangeListener);
        }
    }

    private void addDataListChangeListener(ObservableList<Data<X, Y>> data) {
        if (data != null) {
            data.removeListener(dataListChangeListener);
        }
    }

    private final ListChangeListener<Data<X, Y>> dataListChangeListener = new ListChangeListener<Data<X, Y>>() {
        @Override
        public void onChanged(Change<? extends Data<X, Y>> change) {
            refreshRows();
        }
    };

    private void refreshTable() {
        refreshColumns();
        refreshRows();
    }

    private void refreshColumns() {
        tableView.getColumns().clear();
        tableView.getColumns().add(new IndexColumn());
        if (getSkinnable().getData() == null) {
            return;
        }

        for (Series<X, Y> series : getSkinnable().getData()) {
            TableColumn<Row, Row> seriesColumn = new TableColumn<>();
            seriesColumn.textProperty().bind(series.nameProperty());
            seriesColumn.getColumns().add(new XColumn(series));
            seriesColumn.getColumns().add(new YColumn(series));

            tableView.getColumns().add(seriesColumn);
        }
    }

    private void refreshRows() {
        List<Row> rows = new ArrayList<>();
        final int tableRowCount = maxSeriesSize();
        ObservableList<Series<X, Y>> seriesList = getSkinnable().getData();
        for (int rowIndex = 0; rowIndex < tableRowCount; rowIndex++) {
            Row row = new Row(rowIndex + 1);
            for (Series<X, Y> series : seriesList) {
                ObservableList<Data<X, Y>> data = series.getData();
                if (data != null && rowIndex < data.size()) {
                    row.seriesData.put(series, data.get(rowIndex));
                }
            }
            rows.add(row);
        }
        tableView.setItems(FXCollections.observableArrayList(rows));
    }

    private int maxSeriesSize() {
        int maxSize = 0;
        if (getSkinnable().getData() != null) {
            for (Series<X, Y> series : getSkinnable().getData()) {
                ObservableList<Data<X, Y>> data = series.getData();
                maxSize = Math.max(maxSize, data == null ? 0 : data.size());
            }
        }
        return maxSize;
    }

    private class IndexColumn extends TableColumn<Row, Integer> {
        IndexColumn() {
            super("#");
            setCellValueFactory(cellData -> cellData.getValue().rowNumber);
        }
    }

    private class XColumn extends AbstractSeriesTableColumn<X> {
        XColumn(Series<X, Y> series) {
            super("X", series);
        }

        @Override
        protected ObjectProperty<X> getCellValue(Row row) {
            return row.getData(super.series).XValueProperty();
        }

        @Override
        protected StringConverter<X> getFormatter() {
            StringConverter<X> formatter = getSkinnable().getXFormatter(super.series);
            return formatter == null ? getSkinnable().getXFormatter() : formatter;
        }
    }

    private class YColumn extends AbstractSeriesTableColumn<Y> {
        YColumn(Series<X, Y> series) {
            super("Y", series);
        }

        @Override
        protected ObjectProperty<Y> getCellValue(Row row) {
            return row.getData(super.series).YValueProperty();
        }

        @Override
        protected StringConverter<Y> getFormatter() {
            StringConverter<Y> formatter = getSkinnable().getYFormatter(super.series);
            return formatter == null ? getSkinnable().getYFormatter() : formatter;
        }
    }

    private abstract class AbstractSeriesTableColumn<T> extends TableColumn<Row, T> {
        protected final Series<X, Y> series;

        AbstractSeriesTableColumn(String title, Series<X, Y> series) {
            super(title);
            this.series = series;
            setEditable(false);
            setCellValueFactory(new SeriesCellValueFactory());
            setCellFactory(new SeriesCellFactory());
        }

        protected abstract ObservableValue<T> getCellValue(Row row);

        protected abstract StringConverter<T> getFormatter();

        private class SeriesCellValueFactory implements Callback<CellDataFeatures<Row, T>, ObservableValue<T>> {
            @Override
            public ObservableValue<T> call(CellDataFeatures<Row, T> cellData) {
                if (cellData.getValue().containsDataFor(series)) {
                    return getCellValue(cellData.getValue());
                }
                return null;
            }
        }

        private class SeriesCellFactory implements Callback<TableColumn<Row, T>, TableCell<Row, T>> {
            @Override
            public TableCell<Row, T> call(TableColumn<Row, T> param) {
                return new SeriesTableCell();
            }
        }

        private class SeriesTableCell extends TableCell<Row, T> {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null) {
                    setText(null);
                } else {
                    StringConverter<T> formatter = getFormatter();
                    setText(formatter == null ? item.toString() : formatter.toString(item));
                }
            }
        }
    }

    /**
     * Contains data to be displayed in a single row of the table view.
     */
    private class Row {
        final ObservableValue<Integer> rowNumber;
        final Map<Series<X, Y>, Data<X, Y>> seriesData = new HashMap<>(3);

        Row(int rowNb) {
            rowNumber = new ReadOnlyObjectWrapper<>(rowNb);
        }

        Data<X, Y> getData(Series<X, Y> series) {
            return seriesData.get(series);
        }

        boolean containsDataFor(Series<X, Y> series) {
            return seriesData.containsKey(series);
        }
    }
}
