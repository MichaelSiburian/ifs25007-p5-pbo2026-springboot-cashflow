package org.delcom.ifs25007springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/** Modal pratinjau gambar: klik gambar untuk toggle zoom 1.0x / 1.8x. */
public class ImagePreviewDialog extends Dialog {
    private final Image image;
    private double zoom = 1.0;

    public ImagePreviewDialog(String src, String alt) {
        setHeaderTitle("Pratinjau Gambar");
        setWidth("min(90vw, 900px)");

        image = new Image(src, alt);
        image.getStyle()
                .set("transform-origin", "top left")
                .set("transition", "transform 0.15s")
                .set("max-width", "100%")
                .set("cursor", "zoom-in");
        image.addClickListener(e -> applyZoom(zoom > 1.0 ? 1.0 : 1.8));

        Div box = new Div(image);
        box.getStyle().set("overflow", "auto").set("max-height", "60vh");
        add(box);

        Button smaller = new Button("Perkecil", e -> applyZoom(zoom - 0.4));
        Button larger = new Button("Perbesar", e -> applyZoom(zoom + 0.4));
        Button close = new Button("Tutup", e -> close());
        getFooter().add(smaller, larger, close);
    }

    private void applyZoom(double value) {
        zoom = Math.max(1.0, Math.min(3.0, value));
        image.getStyle().set("transform", "scale(" + zoom + ")");
    }
}
