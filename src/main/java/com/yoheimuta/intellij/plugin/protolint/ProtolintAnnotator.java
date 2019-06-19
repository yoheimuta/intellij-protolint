package com.yoheimuta.intellij.plugin.protolint;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProtolintAnnotator extends ExternalAnnotator<PsiFile, List<ProtolintWarning>> {
    private static final Logger LOGGER = Logger.getInstance(ProtolintAnnotator.class);

    public ProtolintAnnotator() {
        LOGGER.info("ProtolintAnnotator");
    }

    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return super.collectInformation(file, editor, hasErrors);
    }

    @Nullable
    @Override
    public List<ProtolintWarning> doAnnotate(PsiFile file) {
        LOGGER.info("Called doAnnotate");
        return ProtolintExecutor.execute(file);
    }

    @Override
    public void apply(@NotNull PsiFile file, List<ProtolintWarning> warnings, @NotNull AnnotationHolder holder) {
        LOGGER.info("Called apply");
        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) {
            LOGGER.warn("Not Found document");
            return;
        }
        warnings.forEach(warning -> {
            int line = warning.getLine()-1;
            int lineEndOffset = document.getLineEndOffset(line);
            int startOffset = StringUtil.lineColToOffset(file.getText(), line, warning.getColumn());
            TextRange range = new TextRange(startOffset, lineEndOffset);
            holder.createWarningAnnotation(range, warning.getReason());
            LOGGER.info("Create annotation");
        });
    }
}

