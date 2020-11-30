package com.ebvmonitoring.application.views.list;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery;

import javax.xml.transform.SourceLocator;


@Route(value = "list", layout = MainView.class)
@PageTitle("List")
@CssImport(value = "./styles/views/list/list-view.css", include="lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@RouteAlias(value = "", layout = MainView.class)
public class ListView extends Div implements AfterNavigationObserver {


    //------------------------------------------------------------------LOG----------------------------------------------------------------------------------------------------------
    private GridPro<Service> grid;
    private ListDataProvider<Service> dataProvider;

    private Grid<ServiceBox> serviceBoxGrid;

    protected Chart servicestatus;
    protected Chart servicesHeatMap;

    private Grid.Column<Service> statusImgColumn;
    private Grid.Column<Service> serviceNameColumn;
    private Grid.Column<Service> dateColumn;
    private Grid.Column<Service> timeColumn;
    private Grid.Column<Service> statusColumn;
    private Grid.Column<Service> responseColumn;

    private final H2 servicesH2 = new H2();

    public ListView() {
        setId("list-view");

        /*serviceBoxGrid.addColumn(ServiceBox::getServicesName);
        serviceBoxGrid.addColumn(ServiceBox::getServicesStatus);
        serviceBoxGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);*/

        //------------------------------Pie Chart-----------------------------------------------
        servicestatus = new Chart(ChartType.PIE);
        servicesHeatMap = new Chart(ChartType.HEATMAP);

        Configuration conf = servicestatus.getConfiguration();

        conf.setTitle("Servicestatus");

        Tooltip tooltip = new Tooltip();
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(false);
        plotOptions.setSize("100%");

        conf.setPlotOptions(plotOptions);

        int error = 3;
        int warning = 2;
        int success = 10;
        DataSeries series = new DataSeries();

        DataSeriesItem fehler = new DataSeriesItem("Fehler", error, 2);
        series.add(fehler);
        DataSeriesItem warnung = new DataSeriesItem("Warnung", warning, 1);
        series.add(warnung);
        DataSeriesItem laeuft = new DataSeriesItem("Läuft", success, 0);
        laeuft.setSliced(true);
        series.add(laeuft);

        conf.setSeries(series);
        servicestatus.setVisibilityTogglingDisabled(true);

        //--------------------------------------------------------------------------
        //Heatmap Chart
        Configuration config1 = servicesHeatMap.getConfiguration();
        config1.getChart().setType(ChartType.HEATMAP);
        config1.getChart().setMarginTop(40);
        config1.getChart().setMarginBottom(40);

        config1.getTitle().setText("Services");

        config1.getxAxis()
                .setCategories(" ", " ", " ", " ");
        config1.getyAxis().setCategories(" ", " ", " ", " ", " ");

        config1.getColorAxis().setMin(0);
        config1.getColorAxis().setMax(2);
        SolidColor gruen = new SolidColor(1, 255, 7);
        config1.getColorAxis().setMinColor(gruen);
        SolidColor gelb = new SolidColor(255, 247, 0);
        SolidColor rot = new SolidColor(249, 4, 3);
        config1.getColorAxis().setMaxColor(rot);

        Stop stop1 = new Stop(0.33f);
        Stop stop2 = new Stop(0.66f);
        Stop stop3 = new Stop(1.0f);
        //config1.getColorAxis().setStops(stop1,stop2,stop3);

        HeatSeries rs = new HeatSeries("Services", getRawData());

        PlotOptionsHeatmap plotOptionsHeatmap = new PlotOptionsHeatmap();
        plotOptionsHeatmap.setDataLabels(new DataLabels());
        plotOptionsHeatmap.getDataLabels().setEnabled(false);

        SeriesTooltip tooltip1 = new SeriesTooltip();
        tooltip1.setHeaderFormat("{series.name}<br/>");
        tooltip1.setPointFormat("Amount: <b>{point.value}</b> ");
        plotOptionsHeatmap.setTooltip(tooltip1);
        config1.setPlotOptions(plotOptionsHeatmap);

        config1.setSeries(rs);
        //------------------------------------------------------------------------------
        Board board = new Board();
        board.addRow(
                servicesHeatMap,
                //createBadge("Services", servicesH2, "primary-text"),
                servicestatus
        );

        add(board);
        Text textArea = new Text(" LOG");
        board.addRow(textArea);


        Button b1 = new Button("Aktuelle Daten anfordern");
        add(b1);

        //Grid erstellen
        setSizeFull();
        createGrid();
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
        add(grid);

        WrapperCard gridWrapper = new WrapperCard("wrapper", new Component[] { grid }, "card");

        board.addRow(gridWrapper);

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
        grid.setHeight("50%");

        dataProvider = new ListDataProvider<>(getServices());
        grid.setDataProvider(dataProvider);
    }

    private void addColumnsToGrid() {
        createServiceImgColumn();
        createServiceNameColumn();
        createDateColumn();
        createTimeColumn();
        createStatusColumn();
        createResponseTimeColumn();
    }

//-------------------------------------Spalten erstellen---------------------------------------------

    private void createServiceImgColumn(){
        statusImgColumn = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            Image img = new Image(client.getStatusimg(), "");
            Span span = new Span();
            span.setClassName("name");
            hl.add(img);
            return hl;
        })).setComparator( Service::getStatusimg).setHeader("Status").setAutoWidth(true);

    }

    private void createServiceNameColumn() {
        serviceNameColumn = grid.addColumn(Service::getService, "id").setHeader("Servicebezeichnung").setAutoWidth(true)
                .setFlexGrow(0);
    }

    private void createDateColumn() {
        dateColumn = grid
                .addColumn(new LocalDateRenderer<>(
                        client -> LocalDate.parse(client.getDatum()),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .setComparator(Service::getDatum).setHeader("Datum")
                .setAutoWidth(true);
    }

    private void createTimeColumn() {
        timeColumn = grid.addColumn(Service::getUhrzeit, "uhrzeit").setHeader("Uhrzeit")
                .setAutoWidth(true);
    }

    private void createStatusColumn() {
        statusColumn = grid.addColumn(Service::getStatus, "status").setHeader("Statuscode")
                .setAutoWidth(true);
    }

    private void createResponseTimeColumn() {
        responseColumn = grid.addColumn(Service::getAntwortzeit, "antwortzeit").setHeader("Antwortzeit")
                .setAutoWidth(true);
    }

//---------------------------------------------Filter-------------------------------------------------
    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        //Status Image (Rot | Grün) Filter


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
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 2", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 3", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 4", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 5", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Warning", "113 ms"),
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 2", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 3", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 4", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 5", "2020-11-23", "12:15", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-22", "12:00", "Warning", "113 ms")
        );
    }

    private Service createService(String statusimg, String servicename, String date, String time,
                                 String status, String response) {
        Service c = new Service();
        c.setStatusimg(statusimg);
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

        /*List<ServiceBox> gridItems = new ArrayList<>();
        gridItems.add(new ServiceBox("Service 1", "läuft"));
        serviceBoxGrid.setItems(gridItems);*/
    }


    /**
     * Raw data to the heatmap chart
     *
     * @return Array of arrays of numbers.
     */
    private Number[][] getRawData() {
        return new Number[][] { { 0, 1, 0 }, { 0, 1, 2 }, { 0, 2, 0 },
                { 0, 3, 1 }, { 0, 4, 2 }};
    }


}
