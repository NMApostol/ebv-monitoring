package com.ebvmonitoring.application.views.service;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.ebvmonitoring.application.DBConnection;
import com.ebvmonitoring.application.RequestServices;
import com.ebvmonitoring.application.views.mail.JavaEmail;
import com.ebvmonitoring.application.views.settings.Config;
import com.ebvmonitoring.application.views.settings.REST;
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

import javax.mail.MessagingException;

@EnableScheduling
@Route(value = "servicestatus", layout = MainView.class)
@PageTitle("Servicestatus")
@CssImport(value = "./styles/views/service/service-view.css", include="lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
@RouteAlias(value = "", layout = MainView.class)
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

    private final FormLayout columnLayout = new FormLayout();
    private Button[] buttonarray;
    private final List<Button> buttonlist = new ArrayList<>(Collections.emptyList());
    private final List<String> servicename = new ArrayList<>(Collections.emptyList());
    private final List<String> services = new ArrayList<>(Collections.emptyList());
    private Text buttoninfo;
    private Button btn_requestdata;
    private final DataSeries series = new DataSeries();
    private int error;
    private int warning;
    private int success;
    private int farben = 0;
    private int countdown = 0;
    private String green = "#00FF08";
    private String red = "#FF0000";
    private String yellow = "#FFF700";

    public static String errorService;


    public ServiceView() throws Exception {
        setId("service-view");

        //Buttons erstellen
        createButtonGrid();
        createRequestAllServicesButton();

        //Grid erstellen
        setSizeFull();
        createGrid();
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();

        //PieChart erstellen
        createPieChart();

        //Wrapper erstellen und festlegen
        WrapperCard pieChartWrapper = new WrapperCard("wrapper", new Component[] {servicestatus_pie}, "card");
        WrapperCard buttonGridWrapper = new WrapperCard("wrapper", new Component[] {  buttoninfo, columnLayout}, "card");
        WrapperCard logGridWrapper = new WrapperCard("wrapper", new Component[] { new H3("LOG"), grid },  "card");

        Board board = new Board();
        board.addRow(buttonGridWrapper, pieChartWrapper);
        board.addRow(btn_requestdata);
        board.addRow(logGridWrapper);
        add(board);


        Config.Main();

        autoRefresh();


    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

    }



    //-----------------------------Pie Chart erstellen------------------------------------------------------------------
    private void createPieChart(){
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

        DataSeriesItem fehler = new DataSeriesItem("Fehler", error, 2);
        series.add(fehler);
        DataSeriesItem warnung = new DataSeriesItem("Warnung", warning, 1);
        series.add(warnung);
        DataSeriesItem laeuft = new DataSeriesItem("Läuft", success, 0);
        laeuft.setSliced(true);
        series.add(laeuft);

        conf.setSeries(series);
        servicestatus_pie.setVisibilityTogglingDisabled(true);
    }

    private void refreshPieChart(){
        //error = x; warning = y; success = z;
        series.clear();
        series.add(new DataSeriesItem("Fehler", error,2));
        series.add(new DataSeriesItem("Warnung", warning,1));
        DataSeriesItem laeuft = new DataSeriesItem("Läuft", success, 0);
        laeuft.setSliced(true);
        series.add(laeuft);

        servicestatus_pie.getConfiguration().setSeries(series);
        servicestatus_pie.drawChart();
        servicestatus_pie.setVisibilityTogglingDisabled(true);
    }

    //------------------------------Button Erstellung-------------------------------------------------------------------
    private void createButtonGrid(){
        columnLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));
        columnLayout.getStyle().set("overflow", "auto");

        createButtonGridItems();
        addButtonGridValues();
        columnLayout.add(buttonarray);
    }

    private void createButtonGridItems(){

        buttoninfo = new Text("Click auf einen Service gibt alle Daten des gedrückten Services wieder");

        for (int i = 0; i < getServices().size(); i++) {
            if (!servicename.toString().contains(getServices().get(i).getService())) {
                Button new_button = new Button(getServices().get(i).getService());
                buttonlist.add(new_button);
                buttonarray = buttonlist.toArray(new Button[0]);
                servicename.add(getServices().get(i).getService());
            }
        }
    }

    private void addButtonGridValues(){
        //call specific CallServices-Methode vom Backend
        buttonGridColours();
        for (int i = 0; i < buttonarray.length; i++){
            int finalI1 = i;

            buttonlist.get(i).addClickListener(e -> {
                System.out.println(buttonarray[finalI1].getText() + " aktualisiert"); //getText wird zu value of Schnittstelle

                String statusimg = null;
                switch (String.valueOf(RequestServices.responseCode)) {
                    case "200":
                        statusimg = "images/StatusImgGruen.png";
                        break;
                    case "Warning":
                        /*&& getServices.get(i-1).getStatus().equals("404")*/
                        statusimg = "images/StatusImgGelb.png";
                        break;
                    case "404":
                        statusimg = "images/StatusImgRot.png";
                        break;
                }

                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    Connection dbcon = DBConnection.callDB();
                    PreparedStatement usedb=dbcon.prepareStatement("USE monitoredebv");
                    PreparedStatement posted = dbcon.prepareStatement("INSERT INTO rest (url,status,antwortzeit,aufgerufen) " +
                            "VALUES('"+ buttonarray[finalI1].getText() +"','"+RequestServices.responseCode+"','"+RequestServices.mstime+"'," +
                            "'"+dtf.format(LocalDateTime.now())+"')");
                    usedb.executeUpdate();
                    posted.executeUpdate();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                grid.getDataProvider().refreshAll();

                for (Button button : buttonarray) {
                    button.setEnabled(false);
                }

                switch (buttonarray[finalI1].getStyle().get("background-color")) {
                    case "#00FF08":
                        success -= 1;
                        break;
                    case "#FFF700":
                        warning -= 1;
                        break;
                    case "#FF0000":
                        error -= 1;
                        break;
                }

                btn_requestdata.setEnabled(false);
                buttonarray[finalI1].getStyle().set("background-color", "lightgrey");

                UI myUI = UI.getCurrent();
                myUI.setPollInterval(1500);
                myUI.addPollListener(event -> {

                    if(countdown==1){
                        countdown = 0;
                        System.out.println("Grün: " + success + " Rot: " + warning + " Gelb: " + error);
                        refreshGrid();
                        refreshPieChart();
                        btn_requestdata.setEnabled(true);
                        for (Button button : buttonarray) {
                            button.setEnabled(true);
                        }

                        myUI.setPollInterval(-1);
                    }

                    else{
                        for (int d = 0; d < getServices().size(); d++) {
                            if (buttonarray[finalI1].getText().equals(getServices().get(d).getService())) {
                                switch (getServices().get(d).getStatus()) {
                                    case "Success":
                                    case "200":
                                        buttonarray[finalI1].getStyle().set("background-color", green);
                                        break;
                                    case "Warning":
                                        buttonarray[finalI1].getStyle().set("background-color", yellow);
                                        break;
                                    case "Failure":
                                        buttonarray[finalI1].getStyle().set("background-color", red);
                                        break;
                                }
                            }
                        }
                        Notification.show(buttonarray[finalI1].getText() + " aktualisiert");
                    }
                });

                TimerTask task = new TimerTask() {
                    public void run() {
                        switch (buttonarray[finalI1].getStyle().get("background-color")) {
                            case "#00FF08":
                                success += 1;
                                break;
                            case "#FFF700":
                                warning += 1;
                                break;
                            case "#FF0000":
                                error += 1;
                                break;
                        }
                        countdown = 1;
                    }
                };
                Timer timer = new Timer("Timer");
                long delay = 2050;
                timer.schedule(task, delay);
            });
        }
    }

    private void buttonGridColours(){

        farben = 0;
        services.clear();
        success = 0;
        warning = 0;
        error = 0;
        for(int d = 0; d < getServices().size(); d++){
            if(services.size() == buttonarray.length){
                System.out.println("Grün: " + success + " Rot: " + warning + " Gelb: " + error);
                break;
            }
            else {
                if(!services.contains(getServices().get(d).getService())){
                    switch (getServices().get(d).getStatus()) {
                        case "200":
                            buttonarray[farben].getStyle().set("background-color", green).set("color", "black").set("height", "100px");
                            success += 1;
                            services.add(buttonarray[farben].getText());
                            farben += 1;
                            break;
                        case "Warning":
                            buttonarray[farben].getStyle().set("background-color", yellow).set("color", "black").set("height", "100px");
                            warning += 1;
                            services.add(buttonarray[farben].getText());
                            farben += 1;
                            break;
                        case "404":
                            buttonarray[farben].getStyle().set("background-color", red).set("color", "black").set("height", "100px");
                            error += 1;
                            services.add(buttonarray[farben].getText());
                            farben += 1;
                            break;
                    }
                }
            }
        }
    }

    private void refreshButtonGrid(){
        createButtonGridItems();
    }

    //------------------------Service Status Abfrage via Button---------------------------------------------------------
    private void createRequestAllServicesButton(){
        btn_requestdata = new Button("Aktuelle Daten aller Services abfragen");
        btn_requestdata.addClickListener(e -> {
            try {
                requestAllServices();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            Notification.show("Alle Services aktualisiert");

            UI myUI = UI.getCurrent();
            btn_requestdata.setEnabled(false);
            for (Button button : buttonarray) {
                button.setEnabled(false);
                button.getStyle().set("background-color", "lightgrey");
            }
            myUI.setPollInterval(2000);
            myUI.addPollListener(event -> {
                myUI.setPollInterval(-1);
                btn_requestdata.setEnabled(true);
                for (int i = 0; i < buttonarray.length; i++) {
                    buttonarray[i].setEnabled(true);
                }

                buttonGridColours();
                refreshGrid();
                refreshPieChart();
                System.out.println("Grün: " + success + " Rot: " + warning + " Gelb: " + error);
            });
        });
    }

    private void requestAllServices() throws IOException {
        System.out.println("Log geupdated");
        //RequestServices.sendPOST();
        //System.out.println(RequestServices.responseCode);
        //call specific CallServices-Methode vom Backend
    }

    //----------------------------------Log erstellen-------------------------------------------------------------------
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

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    //------------------------------------Refresh-----------------------------------------------------------------------

    public void autoRefresh(){
        UI refUI = UI.getCurrent();
        refUI.setPollInterval(900000); //alle 15 Minuten (900.000 ms)
        refUI.addPollListener(event -> {
            addButtonGridValues();
            try {
                sendAlertEmail();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Notification.show("Alle Services geupdated");
            refreshButtonGrid();
            refreshPieChart();
            refreshGrid();
        });
    }

    //--------------------------------Spalten erstellen-----------------------------------------------------------------
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
        serviceNameColumn = grid.addColumn(Service::getService, "id").setHeader("Servicebezeichnung").setAutoWidth(true);
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
        responseColumn = grid.addColumn(Service::getAntwortzeit, "antwortzeit").setHeader("Antwortzeit [ms]")
                .setAutoWidth(true);
    }

    //--------------------------------------Filter----------------------------------------------------------------------
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
        statusFilter.setItems(Arrays.asList("200", "404", "Success", "Warning", "Error"));
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

    //------------------------------Equal Funktionen (FIlter)-----------------------------------------------------------
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

    //----------------------------------Liste erstellen-----------------------------------------------------------------

    private List<Service> getServices() {

        LinkedList<Service> lkl = new LinkedList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/?user=root","root","");


            Statement st = con.createStatement();
            ResultSet srs = st.executeQuery("SELECT * FROM monitoredebv.rest order by aufgerufen desc");
            while (srs.next()) {
                Service service = new Service();
                service.setService(srs.getString("url"));
                service.setStatus(srs.getString("status"));
                service.setAntwortzeit(srs.getString("antwortzeit"));
                service.setDatum(srs.getString("aufgerufen").substring(0, 10));
                service.setUhrzeit(srs.getString("aufgerufen").substring(11, 16));

                if (service.getStatus().equals("200")) {
                    service.setStatusimg("images/StatusImgGruen.png");
                }
                else{
                    service.setStatusimg("images/StatusImgRot.png");
                }
                lkl.add(service);

            }
            con.close();


        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return lkl;
    }

    public void sendAlertEmail() throws Exception {
        for (int i = 0; i < buttonarray.length; i++) {
            if(buttonarray[i].getStyle().get("background-color").equals("#FF0000")) {
                //errorService = buttonarray[i].getText();
                JavaEmail.JavaEmailMain();
                System.out.println("---------------------Email Error------------------------");
            }
        }

    }
}