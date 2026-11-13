package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;

/** Gaya grid yang konsisten. */
public final class GridStyles {
    private GridStyles() {
    }

    public static <T> void apply(Grid<T> grid, String ariaLabel) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMinHeight("18rem");
        grid.setWidthFull();
        grid.getElement().setAttribute("aria-label", ariaLabel);
    }

    public static Span typeBadge(Transaction transaction) {
        Span span = new Span(transaction.getType().getLabel());
        span.addClassName(transaction.getType() == TransactionType.INCOME
                ? "amount-income" : "amount-expense");
        return span;
    }
}
