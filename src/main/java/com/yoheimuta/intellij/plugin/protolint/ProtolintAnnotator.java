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

public class ProtolintAnnotator extends ExternalAnnotator<Editor, List<ProtolintWarning>> {
    private static final Logger LOGGER = Logger.getInstance(ProtolintAnnotator.class);

    public ProtolintAnnotator() {
        LOGGER.info("ProtolintAnnotator");
    }

    @Nullable
    @Override
    public Editor collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        LOGGER.debug("Called collectInformation");
        return editor;
    }

    @Nullable
    @Override
    public List<ProtolintWarning> doAnnotate(Editor editor) {
        LOGGER.debug("Called doAnnotate");
        return ProtolintExecutor.execute(editor);
    }

    @Override
    public void apply(@NotNull PsiFile file, List<ProtolintWarning> warnings, @NotNull AnnotationHolder holder) {
        LOGGER.debug("Called apply");
        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) {
            LOGGER.warn("Not Found document");
            return;
        }
        warnings.forEach(warning -> {
            int line = warning.getLine()-1;
            int endOffset = document.getLineEndOffset(line);
            int startOffset = StringUtil.lineColToOffset(file.getText(), line, warning.getColumn());

            // See https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/844
            if (!isProperRange(startOffset, endOffset)) {
                LOGGER.info("Skip negative text range");
                return;
            }
            TextRange range = new TextRange(startOffset, endOffset);
            holder.createWarningAnnotation(range, warning.getReason());
            LOGGER.info("Create an annotation");
        });
    }

    private boolean isProperRange(int startOffset, int endOffset) {
        return startOffset <= endOffset && startOffset >= 0;
    }
}

