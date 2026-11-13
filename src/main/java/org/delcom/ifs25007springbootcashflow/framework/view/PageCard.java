package org.delcom.ifs25007springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;

/** Kartu konten halaman. */
public class PageCard extends Div {
    public PageCard(String title) {
        addClassName("page-card");
        H2 heading = new H2(title);
        heading.getStyle().set("margin-top", "0").set("font-size", "1.25rem");
        add(heading);
    }
}
