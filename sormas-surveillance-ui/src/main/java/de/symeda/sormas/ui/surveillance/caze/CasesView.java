package de.symeda.sormas.ui.surveillance.caze;

import java.util.Collection;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid.SelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.caze.CaseDto;
import de.symeda.sormas.samples.ResetButtonForTextField;

/**
 * A view for performing create-read-update-delete operations on products.
 *
 * See also {@link CaseController} for fetching the data, the actual CRUD
 * operations and controlling the view based on events from outside.
 */
public class CasesView extends CssLayout implements View {

	private static final long serialVersionUID = -3533557348144005469L;
	
	public static final String VIEW_NAME = "cases";
    private CaseGrid grid;
    private CaseForm form;

    private CaseController viewLogic = new CaseController(this);
    private Button newCase;

	private VerticalLayout formLayout;

	private VerticalLayout gridLayout;

    public CasesView() {
        setSizeFull();
        addStyleName("crud-view");
        HorizontalLayout topLayout = createTopBar();

        grid = new CaseGrid();
        grid.addSelectionListener(new SelectionListener() {

			private static final long serialVersionUID = 673897391366410556L;

			@Override
            public void select(SelectionEvent event) {
                viewLogic.rowSelected(grid.getSelectedRow());
            }
        });

        formLayout = new VerticalLayout();
        form = new CaseForm(viewLogic);
        formLayout.addComponent(form);
        formLayout.setSizeFull();
        formLayout.setExpandRatio(form, 1);

        gridLayout = new VerticalLayout();
        gridLayout.addComponent(topLayout);
        gridLayout.addComponent(grid);
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        gridLayout.setSizeFull();
        gridLayout.setExpandRatio(grid, 1);
        gridLayout.setStyleName("crud-main-layout");

        addComponent(gridLayout);
        addComponent(formLayout);

        viewLogic.init();
    }

    public HorizontalLayout createTopBar() {
        TextField filter = new TextField();
        filter.setStyleName("filter-textfield");
        filter.setInputPrompt("Filter");
        ResetButtonForTextField.extend(filter);
        filter.setImmediate(true);
        filter.addTextChangeListener(new FieldEvents.TextChangeListener() {

        	private static final long serialVersionUID = -3350923354333230347L;

			@Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                grid.setFilter(event.getText());
            }
        });

        newCase = new Button("New case");
        newCase.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newCase.setIcon(FontAwesome.PLUS_CIRCLE);
        newCase.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -6938781760347121633L;

			@Override
            public void buttonClick(ClickEvent event) {
                viewLogic.newCase();
            }
        });

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setSpacing(true);
        topLayout.setWidth("100%");
        topLayout.addComponent(filter);
        topLayout.addComponent(newCase);
        topLayout.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
        topLayout.setExpandRatio(filter, 1);
        topLayout.setStyleName("top-bar");
        return topLayout;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        viewLogic.enter(event.getParameters());
    }

    public void showError(String msg) {
        Notification.show(msg, Type.ERROR_MESSAGE);
    }

    public void showSaveNotification(String msg) {
        Notification.show(msg, Type.TRAY_NOTIFICATION);
    }

    public void setNewCaseEnabled(boolean enabled) {
        newCase.setEnabled(enabled);
    }

    public void clearSelection() {
        grid.getSelectionModel().reset();
    }

    public void selectRow(CaseDto row) {
        ((SelectionModel.Single) grid.getSelectionModel()).select(row);
    }

    public CaseDto getSelectedRow() {
        return grid.getSelectedRow();
    }

    public void edit(CaseDto caze) {
        if (caze != null) {
            formLayout.setVisible(true);
            gridLayout.setVisible(false);
        } else {
        	formLayout.setVisible(false);
        	gridLayout.setVisible(true);
        }
        form.editCase(caze);
    }

    public void show(Collection<CaseDto> cases) {
        grid.setCases(cases);
    }

    public void refresh(CaseDto product) {
        grid.refresh(product);
        grid.scrollTo(product);
    }

    public void remove(CaseDto caze) {
        grid.remove(caze);
    }

}
