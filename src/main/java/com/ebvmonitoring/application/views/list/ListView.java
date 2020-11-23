package com.ebvmonitoring.application.views.list;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.*;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.ebvmonitoring.application.views.main.MainView;


@Route(value = "list", layout = MainView.class)
@PageTitle("List")
@CssImport(value = "./styles/views/list/list-view.css", include="lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@RouteAlias(value = "", layout = MainView.class)
public class ListView extends Div implements AfterNavigationObserver {


    //------------------------------------------------------------------LOG----------------------------------------------------------------------------------------------------------
    private GridPro<Service> grid;
    private ListDataProvider<Service> dataProvider;

    protected Chart servicestatus;


    private Grid.Column<Service> serviceNameColumn;
    private Grid.Column<Service> dateColumn;
    private Grid.Column<Service> timeColumn;
    private Grid.Column<Service> statusColumn;
    private Grid.Column<Service> responseColumn;

    private final H2 servicesH2 = new H2();

    public ListView() {
        setId("list-view");





        //------------------------------Pie Chart-----------------------------------------------
        servicestatus = new Chart(ChartType.PIE);

        Configuration conf = servicestatus.getConfiguration();

        conf.setTitle("Servicestatus");

        Tooltip tooltip = new Tooltip();
        conf.setTooltip(tooltip);



        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);

        conf.setPlotOptions(plotOptions);

        int error = 1;
        int warning = 2;
        int success = 10;
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("Fehler", error));
        series.add(new DataSeriesItem("Warnung", warning));
        series.add(new DataSeriesItem("Läuft", success));

        conf.setSeries(series);
        servicestatus.setVisibilityTogglingDisabled(true);

        Board board = new Board();
        board.addRow(
                createBadge("Services", servicesH2, "primary-text"),
                servicestatus
        );

        add(board);



        setSizeFull();
        createGrid();
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
        add(grid);

        //WrapperCard gridWrapper = new WrapperCard("wrapper", new Component[] { new H3("Log"), grid }, "card");

        //board.addRow(gridWrapper);

    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        dataProvider = new ListDataProvider<>(getServices());
        grid.setDataProvider(dataProvider);
    }

    private void addColumnsToGrid() {
        createServiceNameColumn();
        createDateColumn();
        createTimeColumn();
        createStatusColumn();
        createResponseTimeColumn();
    }

//-------------------------------------Spalten erstellen---------------------------------------------
    private void createServiceNameColumn() {
        serviceNameColumn = grid.addColumn(Service::getService, "id").setHeader("Servicebezeichnung").setAutoWidth(true)
                /*.setWidth("400px")*/.setFlexGrow(0);
    }

    private void createDateColumn() {
        dateColumn = grid
                .addColumn(new LocalDateRenderer<>(
                        client -> LocalDate.parse(client.getDatum()),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .setComparator(Service::getDatum).setHeader("Datum")
                .setWidth("200px").setFlexGrow(0);
    }

    private void createTimeColumn() {
        timeColumn = grid.addColumn(Service::getUhrzeit, "uhrzeit").setHeader("Uhrzeit")
                .setWidth("200px").setFlexGrow(0);
    }

    private void createStatusColumn() {
        statusColumn = grid.addColumn(Service::getStatus, "status").setHeader("Status")
                .setWidth("200px").setFlexGrow(0);
    }

    private void createResponseTimeColumn() {
        responseColumn = grid.addColumn(Service::getAntwortzeit, "antwortzeit").setHeader("Antwortzeit")
                .setWidth("200px").setFlexGrow(0);
    }

//---------------------------------------------Filter-------------------------------------------------
    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        //Servicebezeichnung Filter
        TextField serviceFilter = new TextField();
        serviceFilter.setPlaceholder("Filter");
        serviceFilter.setClearButtonVisible(true);
        serviceFilter.setWidth("100%");
        serviceFilter.setValueChangeMode(ValueChangeMode.EAGER);
        serviceFilter.addValueChangeListener(event -> dataProvider.addFilter(
                client -> StringUtils.containsIgnoreCase(client.getService(),
                        serviceFilter.getValue())));
        filterRow.getCell(serviceNameColumn).setComponent(serviceFilter);

        //Datum Filter
        DatePicker dateFilter = new DatePicker();
        dateFilter.setPlaceholder("Filter");
        dateFilter.setClearButtonVisible(true);
        dateFilter.setWidth("100%");
        dateFilter.addValueChangeListener(event -> dataProvider
                .addFilter(client -> areDatesEqual(client, dateFilter)));
        filterRow.getCell(dateColumn).setComponent(dateFilter);

        //Uhrzeit Filter
        TimePicker timeFilter = new TimePicker();
        timeFilter.setPlaceholder("Filter");
        timeFilter.setClearButtonVisible(true);
        timeFilter.setWidth("100%");
        timeFilter.addValueChangeListener(event -> dataProvider
                .addFilter(client -> areTimesEqual(client, timeFilter)));
        filterRow.getCell(timeColumn).setComponent(timeFilter);

        //Status Filter
        //ArrayList muss noch auf die verschiedenen HTTP Responses angepasst werden
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.setItems(Arrays.asList("Success", "Warning", "Error"));
        statusFilter.setPlaceholder("Filter");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("100%");
        statusFilter.addValueChangeListener(event -> dataProvider
                .addFilter(client -> areStatusesEqual(client, statusFilter)));
        filterRow.getCell(statusColumn).setComponent(statusFilter);

        //Antwortzeit Filter
        TextField responseTimeFilter = new TextField();
        responseTimeFilter.setPlaceholder("Filter");
        responseTimeFilter.setClearButtonVisible(true);
        responseTimeFilter.setWidth("100%");
        responseTimeFilter.setValueChangeMode(ValueChangeMode.EAGER);
        responseTimeFilter.addValueChangeListener(event -> dataProvider.addFilter(
                client -> StringUtils.containsIgnoreCase(client.getService(),
                        responseTimeFilter.getValue())));
        filterRow.getCell(responseColumn).setComponent(responseTimeFilter);
    }
//---------------------------------------------Equal Funktionen--------------------------------------
    private boolean areStatusesEqual(Service client,
                                     ComboBox<String> statusFilter) {
        String statusFilterValue = statusFilter.getValue();
        if (statusFilterValue != null) {
            return StringUtils.equals(client.getStatus(), statusFilterValue);
        }
        return true;
    }

    private boolean areDatesEqual(Service client, DatePicker dateFilter) {
        LocalDate dateFilterValue = dateFilter.getValue();
        if (dateFilterValue != null) {
            LocalDate clientDate = LocalDate.parse(client.getDatum());
            return dateFilterValue.equals(clientDate);
        }
        return true;
    }

    private boolean areTimesEqual(Service client, TimePicker timeFilter){
        LocalTime timeFilterValue = timeFilter.getValue();
        if(timeFilterValue != null){
            LocalTime clientTime = LocalTime.parse(client.getUhrzeit());
            return timeFilterValue.equals(clientTime);
        }
        return true;
    }
//--------------------------------------Liste erstellen--------------------------------

    //Muss noch auf Datenbankwerte umgeschrieben werden
    private List<Service> getServices() {
        return Arrays.asList(
                createService("Service 1696296362639629863982683682638", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 2", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 3", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 4", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 5", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 6", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 7", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 8", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 2", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 3", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("Service 4", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("Service 1", "2020-11-22", "12:00", "Success", "113 ms")
        );
    }

    private Service createService(String servicename, String date, String time,
                                 String status, String response) {
        Service c = new Service();
        c.setService(servicename);
        c.setDatum(date);
        c.setUhrzeit(time);
        c.setStatus(status);
        c.setAntwortzeit(response);

        return c;
    }


    private WrapperCard createBadge(String title, H2 h2, String h2ClassName) {
        Span titleSpan = new Span(title);
        h2.addClassName(h2ClassName);
        return new WrapperCard("wrapper",
                new Component[] { titleSpan, h2 }, "card",
                "space-m");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        servicesH2.setText("Service 1,Service 2, Service 3, Service4, ...");
    }
}
