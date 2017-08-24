package ru.zrv.tazacom.web.book;

import ru.zrv.tazacom.entity.Author;
import ru.zrv.tazacom.entity.Book;
import ru.zrv.tazacom.entity.Genre;
import ru.zrv.tazacom.entity.Importing;
import ru.zrv.tazacom.entity.StateEnum;
import ru.zrv.tazacom.web.importing.ImportingUrlDialog;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.security.entity.EntityOp;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class BookBrowse extends AbstractLookup {
	
	@Inject
	Metadata metadata;
   
    @Inject
    private CollectionDatasource<Book, UUID> booksDs;

    @Inject
    private CollectionDatasource<Author, UUID> authorsDs;

    @Inject
    private CollectionDatasource<Genre, UUID> genresDs;

    @Inject
    private CollectionDatasource<Importing, UUID> importingsDs;

    @Inject
    private Datasource<Book> bookDs;

    @Inject
    private Table<Book> booksTable;

    @Inject
    private BoxLayout lookupBox;

    @Inject
    private BoxLayout editBox;

    @Inject
    private BoxLayout actionsPane;

    @Inject
    private FieldGroup fieldGroup;

    @Inject
    private TabSheet tabSheet;

    @Named("booksTable.remove")
    private RemoveAction booksTableRemove;

    @Inject
    private DataSupplier dataSupplier;

    private boolean creating;

    @Override
    public void init(Map<String, Object> params) {

        booksDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                Book reloadedItem = dataSupplier.reload(e.getDs().getItem(), bookDs.getView());
                bookDs.setItem(reloadedItem);
            }
        });

        /*
         * Adding {@link CreateAction} to {@link booksTable}
         * The listener removes selection in {@link booksTable}, sets a newly created item to {@link bookDs}
         * and enables controls for record editing
         */
        booksTable.addAction(new CreateAction(booksTable) {
            @SuppressWarnings("rawtypes")
			@Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                booksTable.setSelected(Collections.emptyList());
                bookDs.setItem((Book) newItem);
                refreshOptionsForLookupFields();
                enableEditControls(true);
            }
        });

        /*
         * Adding {@link EditAction} to {@link booksTable}
         * The listener enables controls for record editing
         */
        booksTable.addAction(new EditAction(booksTable) {
            @SuppressWarnings("rawtypes")
			@Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                if (booksTable.getSelected().size() == 1) {
                    refreshOptionsForLookupFields();
                    enableEditControls(false);
                }
            }

            @SuppressWarnings("rawtypes")
			@Override
            public void refreshState() {
                if (target != null) {
                    CollectionDatasource ds = target.getDatasource();
                    if (ds != null && !captionInitialized) {
                        setCaption(messages.getMainMessage("actions.Edit"));
                    }
                }
                super.refreshState();
            }

            @SuppressWarnings("rawtypes")
			@Override
            protected boolean isPermitted() {
                CollectionDatasource ownerDatasource = target.getDatasource();
                boolean entityOpPermitted = security.isEntityOpPermitted(ownerDatasource.getMetaClass(), EntityOp.UPDATE);
                if (!entityOpPermitted) {
                    return false;
                }
                return super.isPermitted();
            }
        });

        /*
         * Setting {@link RemoveAction#afterRemoveHandler} for {@link booksTableRemove}
         * to reset record, contained in {@link bookDs}
         */
        booksTableRemove.setAfterRemoveHandler(removedItems -> bookDs.setItem(null));

        /*
         * Adding ESCAPE shortcut that invokes cancel() method
         */
        editBox.addShortcutAction(new ShortcutAction(new KeyCombination(KeyCombination.Key.ESCAPE),
        shortcutTriggeredEvent -> cancel()));

        disableEditControls();
    }

    @SuppressWarnings("rawtypes")
	private void refreshOptionsForLookupFields() {
        for (Component component : fieldGroup.getOwnComponents()) {
            if (component instanceof LookupField) {
                CollectionDatasource optionsDatasource = ((LookupField) component).getOptionsDatasource();
                if (optionsDatasource != null) {
                    optionsDatasource.refresh();
                }
            }
        }
    }

    /**
     * Method that is invoked by clicking Ok button after editing an existing or creating a new record
     */
    public void save() {
        if (!validate(Collections.singletonList(fieldGroup))) {
            return;
        }
        getDsContext().commit();

        Book editedItem = bookDs.getItem();
        if (creating) {
            booksDs.includeItem(editedItem);
        } else {
            booksDs.updateItem(editedItem);
        }
        booksTable.setSelected(editedItem);

        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Cancel button, discards changes and disables controls for record editing
     */
    public void cancel() {
        Book selectedItem = booksDs.getItem();
        if (selectedItem != null) {
            Book reloadedItem = dataSupplier.reload(selectedItem, bookDs.getView());
            booksDs.setItem(reloadedItem);
        } else {
            bookDs.setItem(null);
        }

        disableEditControls();
    }

    /**
     * Enabling controls for record editing
     * @param creating indicates if a new instance of {@link Book} is being created
     */
    private void enableEditControls(boolean creating) {
        this.creating = creating;
        initEditComponents(true);
        fieldGroup.requestFocus();
    }

    /**
     * Disabling editing controls
     */
    private void disableEditControls() {
        initEditComponents(false);
        booksTable.requestFocus();
    }

    /**
     * Initiating edit controls, depending on if they should be enabled/disabled
     * @param enabled if true - enables editing controls and disables controls on the left side of the splitter
     *                if false - vice versa
     */
    @SuppressWarnings("rawtypes")
	private void initEditComponents(boolean enabled) {
        ComponentsHelper.walkComponents(tabSheet, (component, name) -> {
            if (component instanceof FieldGroup) {
                ((FieldGroup) component).setEditable(enabled);
            } else if (component instanceof Table) {
                ((Table) component).getActions().forEach(action -> action.setEnabled(enabled));
            } else if (!(component instanceof Container)) {
                component.setEnabled(enabled);
            }
        });
        actionsPane.setVisible(enabled);
        lookupBox.setEnabled(!enabled);
    }

    public void onImportClick() {
        enableEditControls(true);

        // Open the dialog for entering URL
    	ImportingUrlDialog dialog = (ImportingUrlDialog) openWindow("tazacom$importing.url-dialog",
                WindowManager.OpenType.DIALOG);   

        // Add a listener that will be notified when the dialog is closed by action with Window.COMMIT_ACTION_ID
        dialog.addCloseWithCommitListener(() -> {
        	// refresh screen with book, author and genre tables
        	genresDs.refresh();    
        	authorsDs.refresh();    
        	booksDs.refresh();     
        	importingsDs.refresh();
        	
        	refreshOptionsForLookupFields();
        	
        	disableEditControls();
        });
    }
    
    public void onBtnDeleteStateClick() {
       Book selectedItem = booksDs.getItem();
       if (selectedItem != null) {
           selectedItem.setState(StateEnum.DELETED);
           booksDs.commit();
       }
    }

}