package com.ebvmonitoring.application.views.addrest;

import com.ebvmonitoring.application.views.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

@Route(value = "add_rest", layout = MainView.class)
@PageTitle("REST Schnittstelle hinzufügen")
@CssImport("./styles/views/addjson/add-json-view.css")
@RouteAlias(value = "restadd", layout = MainView.class)
public class AddRESTView extends Div {

    private final Grid<REST_Fields> grid = new Grid<>(REST_Fields.class, false);

    private final Button cancel = new Button("Abbrechen");
    private final Button save = new Button("Speichern");

    //private final BeanValidationBinder<JSON_Schst> binder;

    private REST_Fields rEST_fields;

    public AddRESTView(@Autowired REST_FieldsService rEST_FieldsService) {
        setId("add-rest-view");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("rest_name").setAutoWidth(true);
        grid.addColumn("rest_link").setAutoWidth(true);
        grid.addColumn("string_input").setAutoWidth(true);
        grid.setDataProvider(new CrudServiceDataProvider<>(rEST_FieldsService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        /*grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<JSON_Schst> jSON_SchstFromBackend = jSON_SchstService.get(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if (jSON_SchstFromBackend.isPresent()) {
                    populateForm(jSON_SchstFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(JSON_Schst.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);*/

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        //------------Backend Code, der die Daten zu einer Schnittstelle umwandelt, wird erstellt-----------------------------
        save.addClickListener(e -> {
            //try {
                if (this.rEST_fields == null) {
                    this.rEST_fields = new REST_Fields();
                }
                //binder.writeBean(this.jSON_Schst);

                rEST_FieldsService.update(this.rEST_fields);
                clearForm();
                refreshGrid();
                Notification.show("Schnittstellenbearbeitung abgeschlossen.");
            /*} catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the jSON_Schst details.");
            }*/
        });

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        TextField restName = new TextField("Name");
        TextField restlink = new TextField("Link");
        TextField stringinput = new TextField("String Input");
        Component[] fields = new Component[]{restlink, stringinput};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(REST_Fields value) {
        this.rEST_fields = value;
        //binder.readBean(this.jSON_Schst);

    }
}