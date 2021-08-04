package com.yoheimuta.intellij.plugin.protolint;

public class ProtolintWarning {
    private final Integer line;
    private final Integer column;
    private final String reason;

    public ProtolintWarning(
        Integer line,
        Integer column,
        String reason
    ) {
        this.line = line;
        this.column = column;
        this.reason = reason;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column - 1;
    }

    public String getReason() {
        return reason;
    }
}
