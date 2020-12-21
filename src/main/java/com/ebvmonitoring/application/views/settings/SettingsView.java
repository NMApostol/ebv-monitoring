package com.ebvmonitoring.application.views.settings;

import com.ebvmonitoring.application.views.main.MainView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "settings", layout = MainView.class)
@PageTitle("Einstellungen")
public class SettingsView extends Div{

    public SettingsView() {
        setId("settings-view");
        add(new Text("Content placeholder"));
    }

}
