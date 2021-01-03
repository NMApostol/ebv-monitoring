package com.ebvmonitoring.application.views.addjson;

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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.Optional;

@Route(value = "add_json", layout = MainView.class)
@PageTitle("JSON hinzufügen")
@CssImport("./styles/views/addjson/add-json-view.css")
@RouteAlias(value = "jsonadd", layout = MainView.class)
public class AddJSONView extends Div {

    private final Grid<JSON_Schst> grid = new Grid<>(JSON_Schst.class, false);

    private final Button cancel = new Button("Abrechen");
    private final Button save = new Button("Speichern");

    //private final BeanValidationBinder<JSON_Schst> binder;

    //private JSON_Schst jSON_Schst;

    public AddJSONView(@Autowired JSON_SchstService jSON_SchstService) {
        setId("add-json-view");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("wert1").setAutoWidth(true);
        grid.addColumn("wert2").setAutoWidth(true);
        grid.addColumn("wert3").setAutoWidth(true);
        grid.addColumn("wert4").setAutoWidth(true);
        grid.setDataProvider(new CrudServiceDataProvider<>(jSON_SchstService));
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
            //clearForm();
            refreshGrid();
        });

        /*save.addClickListener(e -> {
            try {
                if (this.jSON_Schst == null) {
                    this.jSON_Schst = new JSON_Schst();
                }
                binder.writeBean(this.jSON_Schst);

                jSON_SchstService.update(this.jSON_Schst);
                clearForm();
                refreshGrid();
                Notification.show("JSON_Schst details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the jSON_Schst details.");
            }
        });*/

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        TextField wert1 = new TextField("Wert1");
        TextField wert2 = new TextField("Wert2");
        TextField wert3 = new TextField("Wert3");
        TextField wert4 = new TextField("Wert4");
        Component[] fields = new Component[]{wert1, wert2, wert3, wert4};

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

    /*private void clearForm() {
        populateForm(null);
    }

    /*private void populateForm(JSON_Schst value) {
        this.jSON_Schst = value;
        binder.readBean(this.jSON_Schst);

    }*/
}
