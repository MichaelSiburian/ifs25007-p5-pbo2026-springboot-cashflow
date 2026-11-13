package org.delcom.ifs25007springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Konfigurasi global indikator loading Vaadin dan atribut bahasa halaman. */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var ui = uiEvent.getUI();
            var config = ui.getLoadingIndicatorConfiguration();
            config.setFirstDelay(150);
            config.setSecondDelay(600);
            config.setThirdDelay(1500);
            ui.getPage().executeJs("document.documentElement.lang='id'");
        });
    }
}
