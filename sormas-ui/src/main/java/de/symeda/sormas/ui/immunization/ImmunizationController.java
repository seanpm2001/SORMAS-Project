package de.symeda.sormas.ui.immunization;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.immunization.components.MainHeaderLayout;
import de.symeda.sormas.ui.immunization.components.form.ImmunizationCreationForm;
import de.symeda.sormas.ui.immunization.components.form.ImmunizationDataForm;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class ImmunizationController {

	public void registerViews(Navigator navigator) {
		navigator.addView(ImmunizationsView.VIEW_NAME, ImmunizationsView.class);
		navigator.addView(ImmunizationDataView.VIEW_NAME, ImmunizationDataView.class);
		navigator.addView(ImmunizationPersonView.VIEW_NAME, ImmunizationPersonView.class);
	}

	public void create() {
		CommitDiscardWrapperComponent<ImmunizationCreationForm> immunizationCreateComponent = getImmunizationCreateComponent();
		if (immunizationCreateComponent != null) {
			VaadinUiUtil.showModalPopupWindow(immunizationCreateComponent, I18nProperties.getString(Strings.headingCreateNewImmunization));
		}
	}

	public void navigateToImmunization(String uuid) {
		final String navigationState = ImmunizationDataView.VIEW_NAME + "/" + uuid;
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}

	private CommitDiscardWrapperComponent<ImmunizationCreationForm> getImmunizationCreateComponent() {
		UserProvider currentUserProvider = UserProvider.getCurrent();
		if (currentUserProvider != null) {
			ImmunizationCreationForm createForm = new ImmunizationCreationForm();
			ImmunizationDto immunization = ImmunizationDto.build(null);
			immunization.setReportingUser(currentUserProvider.getUserReference());
			createForm.setValue(immunization);
			final CommitDiscardWrapperComponent<ImmunizationCreationForm> viewComponent = new CommitDiscardWrapperComponent<>(
				createForm,
				currentUserProvider.hasUserRight(UserRight.IMMUNIZATION_CREATE),
				createForm.getFieldGroup());

			viewComponent.addCommitListener(() -> {
				if (!createForm.getFieldGroup().isModified()) {

					final ImmunizationDto dto = createForm.getValue();
					final PersonDto person = createForm.getPerson();
					ControllerProvider.getPersonController()
						.selectOrCreatePerson(person, I18nProperties.getString(Strings.infoSelectOrCreatePersonForImmunization), selectedPerson -> {
							if (selectedPerson != null) {
								dto.setPerson(selectedPerson);
								FacadeProvider.getImmunizationFacade().save(dto);
							}
						}, true);
				}
			});
			return viewComponent;
		}
		return null;
	}

	public CommitDiscardWrapperComponent<ImmunizationDataForm> getImmunizationDataEditComponent(String immunizationUuid) {

		ImmunizationDto immunizationDto = findImmunization(immunizationUuid);

		ImmunizationDataForm immunizationDataForm = new ImmunizationDataForm(immunizationUuid, immunizationDto.isPseudonymized());
		immunizationDataForm.setValue(immunizationDto);

		CommitDiscardWrapperComponent<ImmunizationDataForm> editComponent = new CommitDiscardWrapperComponent<>(
			immunizationDataForm,
			UserProvider.getCurrent().hasUserRight(UserRight.IMMUNIZATION_EDIT),
			immunizationDataForm.getFieldGroup());

		editComponent.addCommitListener(() -> {
			if (!immunizationDataForm.getFieldGroup().isModified()) {
				ImmunizationDto immunizationDtoValue = immunizationDataForm.getValue();
				FacadeProvider.getImmunizationFacade().save(immunizationDtoValue);
				Notification.show(I18nProperties.getString(Strings.messageImmunizationSaved), Notification.Type.WARNING_MESSAGE);
				SormasUI.refreshView();
			}
		});

		// Initialize 'Delete' button
		if (UserProvider.getCurrent().hasUserRight(UserRight.IMMUNIZATION_DELETE)) {
			editComponent.addDeleteListener(() -> {
				FacadeProvider.getImmunizationFacade().deleteImmunization(immunizationDto.getUuid());
				UI.getCurrent().getNavigator().navigateTo(ImmunizationsView.VIEW_NAME);
			}, I18nProperties.getString(Strings.entityImmunization));
		}

		// Initialize 'Archive' button
		if (UserProvider.getCurrent().hasUserRight(UserRight.IMMUNIZATION_ARCHIVE)) {
			boolean archived = FacadeProvider.getImmunizationFacade().isArchived(immunizationUuid);
			Button archiveButton = ButtonHelper.createButton(archived ? Captions.actionDearchive : Captions.actionArchive, e -> {
				editComponent.commit();
				archiveOrDearchiveImmunization(immunizationUuid, !archived);
			}, ValoTheme.BUTTON_LINK);

			editComponent.getButtonsPanel().addComponentAsFirst(archiveButton);
			editComponent.getButtonsPanel().setComponentAlignment(archiveButton, Alignment.BOTTOM_LEFT);
		}

		return editComponent;
	}

	public MainHeaderLayout getImmunizationMainHeaderLayout(String uuid) {
		ImmunizationDto immunizationDto = findImmunization(uuid);

		String shortUuid = DataHelper.getShortUuid(immunizationDto.getUuid());
		PersonReferenceDto person = immunizationDto.getPerson();
		String text = person.getFirstName() + " " + person.getLastName() + " (" + shortUuid + ")";

		return new MainHeaderLayout(text);
	}

	private ImmunizationDto findImmunization(String uuid) {
		return FacadeProvider.getImmunizationFacade().getByUuid(uuid);
	}

	private void archiveOrDearchiveImmunization(String uuid, boolean archive) {

		if (archive) {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationArchiveImmunization),
					I18nProperties.getString(Strings.entityImmunization).toLowerCase(),
					I18nProperties.getString(Strings.entityImmunization).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingArchiveImmunization),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				e -> {
					if (e.booleanValue() == true) {
						FacadeProvider.getImmunizationFacade().archiveOrDearchiveImmunization(uuid, true);
						Notification.show(
							String.format(
								I18nProperties.getString(Strings.messageImmunizationArchived),
								I18nProperties.getString(Strings.entityImmunization)),
							Notification.Type.ASSISTIVE_NOTIFICATION);
						navigateToImmunization(uuid);
					}
				});
		} else {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationDearchiveImmunization),
					I18nProperties.getString(Strings.entityImmunization).toLowerCase(),
					I18nProperties.getString(Strings.entityImmunization).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingDearchiveImmunization),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				e -> {
					if (e.booleanValue()) {
						FacadeProvider.getImmunizationFacade().archiveOrDearchiveImmunization(uuid, false);
						Notification.show(
							String.format(
								I18nProperties.getString(Strings.messageImmunizationDearchived),
								I18nProperties.getString(Strings.entityImmunization)),
							Notification.Type.ASSISTIVE_NOTIFICATION);
						navigateToImmunization(uuid);
					}
				});
		}
	}
}
