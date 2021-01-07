package com.ebvmonitoring.application.views.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.ebvmonitoring.application.views.main.MainView;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Route(value = "servicestatus", layout = MainView.class)
@PageTitle("Servicestatus")
@CssImport(value = "./styles/views/service/service-view.css", include="lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@RouteAlias(value = "", layout = MainView.class)
@EnableScheduling
public class ServiceView extends Div implements AfterNavigationObserver {


    //------------------------------------------------------------------LOG----------------------------------------------------------------------------------------------------------
    private GridPro<Service> grid;
    private ListDataProvider<Service> dataProvider;

    protected Chart servicestatus;

    private Grid.Column<Service> statusImgColumn;
    private Grid.Column<Service> serviceNameColumn;
    private Grid.Column<Service> dateColumn;
    private Grid.Column<Service> timeColumn;
    private Grid.Column<Service> statusColumn;
    private Grid.Column<Service> responseColumn;

    public ServiceView() {
        setId("service-view");

        //------------------------------Pie Chart-----------------------------------------------
        servicestatus = new Chart(ChartType.PIE);
        Configuration conf = servicestatus.getConfiguration();

        Tooltip tooltip = new Tooltip();
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        //SQL Statement gibt die drei Daten zurück count status
        int error = 1; //count servicename where status != 200
        int warning = 1; //count servicename where status == 200 and servicename-1 != 200
        int success = 3; //count servicename where status == 200
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
        //----------------------------------Buttons für die Services-----------------------------------
        //for every unique service add Button; Statusfarbe und eigener Abfrage des spezifischen Services

        FormLayout columnLayout = new FormLayout();
        columnLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));
        columnLayout.getStyle().set("overflow", "auto");

        String green = "#00FF08";
        String red = "#FF0000";
        String yellow = "#FFF700";
        Text buttoninfo = new Text("Click auf einen Service gibt alle Daten des gedrückten Services wieder");

        Button button1 = new Button("Service 1");
        button1.getStyle().set("background-color",green).set("color", "black").set("height", "100px");
        Button button2 = new Button("Service 2");
        button2.getStyle().set("background-color",red).set("color", "black").set("height", "100px");
        Button button3 = new Button("Service 3");
        button3.getStyle().set("background-color",green).set("color", "black").set("height", "100px");
        Button button4 = new Button("Service 4");
        button4.getStyle().set("background-color",yellow).set("color", "black").set("height", "100px");
        Button button5 = new Button("Service 5");
        button5.getStyle().set("background-color",green).set("color", "black").set("height", "100px");

        columnLayout.add(button1, button2, button3, button4, button5, button5);

        //------------------------------------------------------------------------------
        Button requestdata = new Button("Aktuelle Daten aller Services abfragen");

        //Grid erstellen
        setSizeFull();
        createGrid();
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
        refreshGrid();
        //add(grid);

        //Wrapper erstellen und festlegen
        WrapperCard pieChartWrapper = new WrapperCard("wrapper", new Component[] {  servicestatus }, "card");
        WrapperCard buttonGridWrapper = new WrapperCard("wrapper", new Component[] {  buttoninfo, columnLayout}, "card");
        WrapperCard logGridWrapper = new WrapperCard("wrapper", new Component[] { new H3("LOG"), grid },  "card");

        Board board = new Board();
        board.addRow(buttonGridWrapper, pieChartWrapper);
        board.addRow(requestdata);
        board.addRow(logGridWrapper);
        add(board);
    }

    @Scheduled(fixedRate = 1000)
    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
        Notification.show("Log geupdated");
        System.out.println("----------------------------------------------------------");
    }

    //------------Button Erstellung---------------------
    /*
    @RequestMapping("/")
    @Scheduled(cron = "0 0/15 * * * *")
    private void ButtonCreate(){
        for(int i = 0; i < getServices().size(); i++) {
            Button button = new Button();
            if (getServices().equals("Success")) {
                button.getStyle().set("background-color","green").set("color", "white");
            }
            else if(getServices().equals( "Success") && getServices().equals("Error")) {
                button.getStyle().set("background-color","yellow").set("color", "white");
            }
            else if(getServices().equals( "Error")){
                button.getStyle().set("background-color","red").set("color", "white");
            }
            //callStatusOne(servicename);
            columnLayout.add(button);
        }
    }*/

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
        //-------------------------------------------ArrayList muss noch auf die verschiedenen HTTP Responses angepasst werden----------------------------------------
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

    //---------------------------------------------Equal Funktionen (FIlter)--------------------------------------
    private boolean areStatusesEqual(Service client, ComboBox<String> statusFilter) {
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
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-23", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 2", "2020-11-23", "12:00", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 3", "2020-11-23", "12:00", "Success", "113 ms"),
                createService("images/StatusImgGruen.png", "Service 4", "2020-11-23", "12:00", "Success", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 5", "2020-11-23", "12:00", "Success", "115 ms"),
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

    private Service createService(String statusimg, String servicename, String date, String time, String status, String response) {
        Service c = new Service();
        c.setStatusimg(statusimg);
        c.setService(servicename);
        c.setDatum(date);
        c.setUhrzeit(time);
        c.setStatus(status);
        c.setAntwortzeit(response);
        return c;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

    }
}
