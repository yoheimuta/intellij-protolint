package com.yoheimuta.intellij.plugin.protolint;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ProtolintExecutor {
    private static final Logger LOGGER = Logger.getInstance(ProtolintExecutor.class.getPackage().getName());

    static List<ProtolintWarning> execute(final PsiFile file) {
        final Process process = createProcess(file);
        if (process == null) {
            return new ArrayList<>();
        }
        return getOutput(process);
    }

    private static Process createProcess(final PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            LOGGER.info("The file only exists in memory. It will be checked after saved to disk.");
            return null;
        }

        final GeneralCommandLine commandLine = new GeneralCommandLine();
        final Project project = psiFile.getProject();
        final ProjectService state = ProjectService.getInstance(project);

        commandLine.setExePath(StringUtils.defaultIfEmpty(state.executable, getDefaultExe()));
        commandLine.setWorkDirectory(project.getBasePath());
        commandLine.withEnvironment(System.getenv());
        commandLine.addParameters("lint");
        if (!state.config.isEmpty()) {
            commandLine.addParameters("-config_dir_path=" + state.config);
        }
        commandLine.addParameter(virtualFile.getPath());

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
}
