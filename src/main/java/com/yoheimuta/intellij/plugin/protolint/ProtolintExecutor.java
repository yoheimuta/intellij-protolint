package com.yoheimuta.intellij.plugin.protolint;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProtolintExecutor {
    private static final Logger LOGGER = Logger.getInstance(ProtolintExecutor.class.getPackage().getName());

    static List<ProtolintWarning> execute(@NotNull final Editor editor) {
        Path tmp;
        try {
            tmp = Files.createTempFile(null, ".proto");
        } catch (IOException ex) {
            LOGGER.error("There was a problem while preparing a temp file.", ex);
            throw new ProtolintPluginException(ex);
        }

        try {
            final Process process = createProcess(editor, tmp);
            return getOutput(process);
        } finally {
            try {
                Files.delete(tmp);
            } catch (IOException ex) {
                LOGGER.error("There was a problem while deleting a temp file.", ex);
            }
        }
    }

    private static Process createProcess(final Editor editor, final Path tmp) {
        // ref. https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004284939-How-to-trigger-ExternalAnnotator-running-immediately-after-saving-the-code-change-
        VirtualFile file = createSyncedFile(editor.getDocument(), tmp);

        final GeneralCommandLine commandLine = new GeneralCommandLine();
        final Project project = editor.getProject();
        final ProjectService state = ProjectService.getInstance(project);

        commandLine.setExePath(StringUtils.defaultIfEmpty(state.executable, getDefaultExe()));
        if (project != null) {
            commandLine.setWorkDirectory(project.getBasePath());
        } else {
            LOGGER.warn("Skip commandLine.setWorkDirectory because project is null");
        }
        commandLine.withEnvironment(System.getenv());
        commandLine.addParameters("lint");
        if (!state.config.isEmpty()) {
            commandLine.addParameters("-config_dir_path=" + state.config);
        }
        commandLine.addParameter(file.getPath());

        try {
            return commandLine.createProcess();
        } catch (ExecutionException ex) {
            LOGGER.error("Could not create protolint process.", ex);
            throw new ProtolintPluginException(ex);
        }
    }

    private static List<ProtolintWarning> getOutput(final Process process) {
        final Reader errorStreamReader = new InputStreamReader(process.getErrorStream());
        final BufferedReader error = new BufferedReader(errorStreamReader);

        List<ProtolintWarning> warnings = new ArrayList<>();
        try {
            String line = error.readLine();
            while (line != null) {
                LOGGER.debug("Protolint raw line: " + line);
                ProtolintWarning warning = parseWarning(line);
                if (warning != null) {
                    warnings.add(warning);
                }
                line = error.readLine();
            }

            process.waitFor();
            LOGGER.debug("Process exit code: " + process.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("There was a problem while trying to read the protolint process output.", ex);
            throw new ProtolintPluginException(ex);
        }

        return warnings;
    }

    private static ProtolintWarning parseWarning(final String raw) {
        // e.g. [/path/to/cloudEndpoints.proto:121:13] Field name "Disabled" must be LowerSnakeCase
        final String[] parts = raw.split("] ");
        if (parts.length < 2) {
            LOGGER.debug("Not found one ]. " + raw);
            return null;
        }
        final String[] meta = parts[0].split(":");
        if (meta.length < 3) {
            LOGGER.debug("Not found two :. " + raw);
            return null;
        }

        try {
            final Integer line = Integer.valueOf(meta[meta.length - 2]);
            final Integer column = Integer.valueOf(meta[meta.length - 1]);
            final String reason = parts[1];
            return new ProtolintWarning(line, column, reason);
        } catch (NumberFormatException ex) {
            LOGGER.debug("This is not warning output.");
            return null;
        }
    }

    private static String getDefaultExe() {
        return "protolint";
    }


    private static VirtualFile createSyncedFile(Document doc, Path tmp) {
        try {
            try(BufferedWriter out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                out.write(doc.getText());
            }
            return LocalFileSystem.getInstance().findFileByPath(tmp.toString());
        } catch (IOException ex) {
            LOGGER.error("There was a problem while preparing a temp file.", ex);
            throw new ProtolintPluginException(ex);
        }
    }
}
