package com.yoheimuta.intellij.plugin.protolint;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.*;

public class ProjectConfigurationPanel implements SearchableConfigurable {
    private static final Logger LOGGER = Logger.getInstance(ProjectConfigurationPanel.class.getPackage().getName());
    private final ProjectService state;
    private final Project project;

    private TextFieldWithHistoryWithBrowseButton protolintExeField;
    private TextFieldWithHistoryWithBrowseButton protolintConfigField;
    private JRadioButton searchForConfigRadioButton;
    private JRadioButton useSpecificConfigRadioButton;
    private JPanel rootPanel;

    public ProjectConfigurationPanel(@NotNull Project project) {
        this.state = ProjectService.getInstance(project).getState();
        this.project = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "Protolint";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Protocol Buffer Linter";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        addListeners();
        return rootPanel;
    }

    @Override
    public boolean isModified() {
        return !protolintExeField.getText().equals(state.executable)
                || !protolintConfigField.getText().equals(state.config);
    }

    @Override
    public void apply() throws ConfigurationException {
        state.executable = protolintExeField.getText();
        state.config = protolintConfigField.getText();
        LOGGER.info("Apply configuration");
    }

    @Override
    public void reset() {
        loadSettings();
    }

    private void loadSettings() {
        protolintExeField.setText(state.executable);
        protolintConfigField.setText(state.config);

        final boolean shouldSearchConfig = state.config.isEmpty();
        searchForConfigRadioButton.setSelected(shouldSearchConfig);
        useSpecificConfigRadioButton.setSelected(!shouldSearchConfig);
        protolintConfigField.setEnabled(!shouldSearchConfig);
    }

    private void addListeners() {
        searchForConfigRadioButton.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                protolintConfigField.setEnabled(false);
                protolintConfigField.setText("");
                useSpecificConfigRadioButton.setSelected(false);
            }
        });
        useSpecificConfigRadioButton.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                protolintConfigField.setEnabled(true);
                searchForConfigRadioButton.setSelected(false);
            }
        });
        protolintExeField.addActionListener((ActionEvent event) -> {
            final VirtualFile file = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFileDescriptor(),
                    project,
                    null
            );
            if (file == null) {
                return;
            }
            LOGGER.debug("Selected file: " + file.getPath());
            protolintExeField.setText(file.getPath());
        });
        protolintConfigField.addActionListener((ActionEvent event) -> {
            final VirtualFile file = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    project,
                    null
            );
            if (file == null) {
                return;
            }
            LOGGER.debug("Selected file: " + file.getPath());
            protolintConfigField.setText(file.getPath());
            useSpecificConfigRadioButton.setSelected(true);
        });
    }
}
