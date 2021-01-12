package com.ebvmonitoring.application.views.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
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

@Route(value = "servicestatus", layout = MainView.class)
@PageTitle("Servicestatus")
@CssImport(value = "./styles/views/service/service-view.css", include="lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@RouteAlias(value = "", layout = MainView.class)
@EnableScheduling
public class ServiceView extends Div implements AfterNavigationObserver{


    //--------------------------------------------------LOG-------------------------------------------------------------
    protected Chart servicestatus_pie;

    private GridPro<Service> grid;
    private Grid.Column<Service> serviceNameColumn;
    private Grid.Column<Service> dateColumn;
    private Grid.Column<Service> timeColumn;
    private Grid.Column<Service> statusColumn;
    private Grid.Column<Service> responseColumn;
    private ListDataProvider<Service> dataProvider;

    private LocalTime tillnextUpdate = LocalTime.now();

    //--------------
    int error;
    int warning;
    int success;
    //--------------

    public ServiceView() {
        setId("service-view");

        //------------------------------Pie Chart-----------------------------------------------------------------------
        servicestatus_pie = new Chart(ChartType.PIE);
        Configuration conf = servicestatus_pie.getConfiguration();

        Tooltip tooltip = new Tooltip();
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        //SQL Statement gibt die drei Daten zurück count status
        error = 1; //count(select * FROM s_service WHERE status != 200)
        warning = 1; //count(select * FROM s_service WHERE status == 200 and status != 200 ORDER BY DATE_ADDED DESC LIMIT 1)
        success = 3; //count(select * FROM s_service WHERE status == 200)
        DataSeries series = new DataSeries();

        DataSeriesItem fehler = new DataSeriesItem("Fehler", error, 2);
        series.add(fehler);
        DataSeriesItem warnung = new DataSeriesItem("Warnung", warning, 1);
        series.add(warnung);
        DataSeriesItem laeuft = new DataSeriesItem("Läuft", success, 0);
        laeuft.setSliced(true);
        series.add(laeuft);

        conf.setSeries(series);
        servicestatus_pie.setVisibilityTogglingDisabled(true);

        //----------------------------------Buttons für die Services----------------------------------------------------
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
        button1.addClickListener(e -> { Notification.show(button1.getText()); });
        Button button2 = new Button("Service 2");
        button2.getStyle().set("background-color",red).set("color", "black").set("height", "100px");
        Button button3 = new Button("Service 3");
        button3.getStyle().set("background-color",green).set("color", "black").set("height", "100px");
        Button button4 = new Button("Service 4");
        button4.getStyle().set("background-color",yellow).set("color", "black").set("height", "100px");
        Button button5 = new Button("Service 5");
        button5.getStyle().set("background-color",green).set("color", "black").set("height", "100px");

        columnLayout.add(button1, button2, button3, button4, button5, button5);

        Button btn_requestdata = new Button("Aktuelle Daten aller Services abfragen");
        btn_requestdata.addClickListener(e -> {
            requestAllServices();
            refreshGrid();

            //error = 10;
            series.clear();
            series.add(new DataSeriesItem("Fehler", error,2));
            series.add(new DataSeriesItem("Warnung", warning,1));
            series.add(new DataSeriesItem("Läuft", success,0));
            laeuft.setSliced(true);
            servicestatus_pie.getConfiguration().setSeries(series);
            servicestatus_pie.drawChart();
            servicestatus_pie.setVisibilityTogglingDisabled(true);
            Notification.show("Alle Services aktualisiert");

            //UI.getCurrent().getPage().reload();

        });

        //--------------------------------------------------------------------------------------------------------------
        //Grid erstellen
        setSizeFull();
        createGrid();
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();

        //Wrapper erstellen und festlegen
        WrapperCard pieChartWrapper = new WrapperCard("wrapper", new Component[] {servicestatus_pie}, "card");
        WrapperCard buttonGridWrapper = new WrapperCard("wrapper", new Component[] {  buttoninfo, columnLayout}, "card");
        WrapperCard logGridWrapper = new WrapperCard("wrapper", new Component[] { new H3("LOG"), grid },  "card");

        Board board = new Board();
        board.addRow(buttonGridWrapper, pieChartWrapper);
        board.addRow(btn_requestdata);
        board.addRow(logGridWrapper);
        add(board);

        repeatedTasksTest();
    }

    private void refreshGrid() {
        grid.select(null);

    }

    public LocalTime repeatedTasksTest() {
        TimerTask repeatedTask = new TimerTask() {
            final double seconds = 901;
            int i = 0;
            final LocalTime time = LocalTime.of(0, 15,0);
            public void run () {
                i++;
                double ii = i % seconds;
                tillnextUpdate = time.minusSeconds((new Double(ii)).longValue());
                System.out.println(tillnextUpdate);
                if (tillnextUpdate.toString().equals("00:00")) {
                    System.out.println("Aktualisieren...");
                }
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(repeatedTask, 0, 1000);

        if (tillnextUpdate.toString().equals("00:00")) {
            Notification.show("Aktualisieren...");
        }

        return tillnextUpdate;
    }


    private void requestAllServices(){
        System.out.println("Log geupdated");
    }

    //------------Button Erstellung-------------------------------------------------------------------------------------

    /*@RequestMapping("/")
    @Scheduled(cron = "0 0/15 * * * *")
    private void ButtonCreate(){
        for(int i = 0; i < getServices().size(); i++) {
            Button button = new Button();
            if (getServices(status).equals("200")) {
                button.getStyle().set("background-color","green").set("color", "white");
            }
            else if(getServices(status).equals( "200") && getServices(status).equals("404")) {
                button.getStyle().set("background-color","yellow").set("color", "white");
            }
            else if(!getServices(status).equals( "200")){
                button.getStyle().set("background-color","red").set("color", "white");
            }
            callOneStatus(servicename);
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
//-------------------------------------Spalten erstellen----------------------------------------------------------------

    private void createServiceImgColumn(){
        Grid.Column<Service> statusImgColumn = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            Image img = new Image(client.getStatusimg(), "");
            Span span = new Span();
            span.setClassName("name");
            hl.add(img);
            return hl;
        })).setComparator(Service::getStatusimg).setHeader("Status").setAutoWidth(true);

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

//---------------------------------------------Filter-------------------------------------------------------------------
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

    //-----------------------------------------Equal Funktionen (FIlter)------------------------------------------------
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

//--------------------------------------Liste erstellen-----------------------------------------------------------------

    //Muss noch auf Datenbankwerte umgeschrieben werden
    private List<Service> getServices() {
        return Arrays.asList(
                createService("images/StatusImgGruen.png", "Service 1", "2020-11-23", "12:00", "Success", "113 ms"),
                createService("images/StatusImgRot.png", "Service 2", "2020-11-23", "12:00", "Failure", "115 ms"),
                createService("images/StatusImgGruen.png", "Service 3", "2020-11-23", "12:00", "Success", "113 ms"),
                createService("images/StatusImgGelb.png", "Service 4", "2020-11-23", "12:00", "Success", "115 ms"),
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

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("mm:ss");
        LocalTime nextUpdateAt = LocalTime.of(0,15,0);
        Notification.show("Bis zum nächsten Update: " + df.format(nextUpdateAt));
    }
}
