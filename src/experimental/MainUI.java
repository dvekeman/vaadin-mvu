package experimental;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;

public class MainUI extends com.vaadin.ui.UI {
	@Override
	public void init(VaadinRequest request) {
		VerticalLayout layout = new VerticalLayout();

		Component mainComponent = Main.view();
		layout.addComponent(mainComponent);

		setContent(layout);
	}
}
