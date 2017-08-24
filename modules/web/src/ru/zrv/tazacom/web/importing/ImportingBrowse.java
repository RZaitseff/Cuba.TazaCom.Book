package ru.zrv.tazacom.web.importing;

import ru.zrv.tazacom.entity.Importing;
import com.haulmont.cuba.core.entity.Entity;
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

public class ImportingBrowse extends AbstractLookup {

    /**
     * The {@link CollectionDatasource} instance that loads a list of {@link Importing} records
     * to be displayed in {@link ImportingBrowse#importingsTable} on the left
     */
    @Inject
    private CollectionDatasource<Importing, UUID> importingsDs;

    /**
     * The {@link Datasource} instance that contains an instance of the selected entity
     * in {@link ImportingBrowse#importingsDs}
     * <p/> Containing instance is loaded in {@link CollectionDatasource#addItemChangeListener}
     * with the view, specified in the XML screen descriptor.
     * The listener is set in the {@link ImportingBrowse#init(Map)} method
     */
    @Inject
    private Datasource<Importing> importingDs;

    /**
     * The {@link Table} instance, containing a list of {@link Importing} records,
     * loaded via {@link ImportingBrowse#importingsDs}
     */
    @Inject
    private Table<Importing> importingsTable;

    /**
     * The {@link BoxLayout} instance that contains components on the left side
     * of {@link SplitPanel}
     */
    @Inject
    private BoxLayout lookupBox;

    /**
    * The {@link BoxLayout} instance that contains components on the right side
    * of {@link SplitPanel}
    */
    @Inject
    private BoxLayout editBox;

    /**
     * The {@link BoxLayout} instance that contains buttons to invoke Save or Cancel actions in edit mode
     */
    @Inject
    private BoxLayout actionsPane;

    /**
     * The {@link FieldGroup} instance that is linked to {@link ImportingBrowse#importingDs}
     * and shows fields of the selected {@link Importing} record
     */
    @Inject
    private FieldGroup fieldGroup;

    /**
     * The {@link RemoveAction} instance, related to {@link ImportingBrowse#importingsTable}
     */
    @Named("importingsTable.remove")
    private RemoveAction importingsTableRemove;

    @Inject
    private DataSupplier dataSupplier;

    /**
     * {@link Boolean} value, indicating if a new instance of {@link Importing} is being created
     */
    private boolean creating;

    @Override
    public void init(Map<String, Object> params) {

        /*
         * Adding {@link com.haulmont.cuba.gui.data.Datasource.ItemChangeListener} to {@link importingsDs}
         * The listener reloads the selected record with the specified view and sets it to {@link importingDs}
         */
        importingsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                Importing reloadedItem = dataSupplier.reload(e.getDs().getItem(), importingDs.getView());
                importingDs.setItem(reloadedItem);
            }
        });

        /*
         * Adding {@link CreateAction} to {@link importingsTable}
         * The listener removes selection in {@link importingsTable}, sets a newly created item to {@link importingDs}
         * and enables controls for record editing
         */
        importingsTable.addAction(new CreateAction(importingsTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                importingsTable.setSelected(Collections.emptyList());
                importingDs.setItem((Importing) newItem);
                refreshOptionsForLookupFields();
                enableEditControls(true);
            }
        });

        /*
         * Adding {@link EditAction} to {@link importingsTable}
         * The listener enables controls for record editing
         */
        importingsTable.addAction(new EditAction(importingsTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                if (importingsTable.getSelected().size() == 1) {
                    refreshOptionsForLookupFields();
                    enableEditControls(false);
                }
            }

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
         * Setting {@link RemoveAction#afterRemoveHandler} for {@link importingsTableRemove}
         * to reset record, contained in {@link importingDs}
         */
        importingsTableRemove.setAfterRemoveHandler(removedItems -> importingDs.setItem(null));

        /*
         * Adding ESCAPE shortcut that invokes cancel() method
         */
        editBox.addShortcutAction(new ShortcutAction(new KeyCombination(KeyCombination.Key.ESCAPE),
        shortcutTriggeredEvent -> cancel()));

        disableEditControls();
    }

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

        Importing editedItem = importingDs.getItem();
        if (creating) {
            importingsDs.includeItem(editedItem);
        } else {
            importingsDs.updateItem(editedItem);
        }
        importingsTable.setSelected(editedItem);

        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Cancel button, discards changes and disables controls for record editing
     */
    public void cancel() {
        Importing selectedItem = importingsDs.getItem();
        if (selectedItem != null) {
            Importing reloadedItem = dataSupplier.reload(selectedItem, importingDs.getView());
            importingsDs.setItem(reloadedItem);
        } else {
            importingDs.setItem(null);
        }

        disableEditControls();
    }

    /**
     * Enabling controls for record editing
     * @param creating indicates if a new instance of {@link Importing} is being created
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
        importingsTable.requestFocus();
    }

    /**
     * Initiating edit controls, depending on if they should be enabled/disabled
     * @param enabled if true - enables editing controls and disables controls on the left side of the splitter
     *                if false - visa versa
     */
    private void initEditComponents(boolean enabled) {
        fieldGroup.setEditable(enabled);
        actionsPane.setVisible(enabled);
        lookupBox.setEnabled(!enabled);
    }
}