package mvu.sample

import com.vaadin.server.VaadinRequest
import com.vaadin.ui.VerticalLayout

class MainUI : com.vaadin.ui.UI() {
    public override fun init(request: VaadinRequest) {
        val layout = VerticalLayout()

        val mainComponent = Main.view()
        layout.addComponent(mainComponent)

        content = layout
    }
}
