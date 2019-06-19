package com.yoheimuta.intellij.plugin.protolint;

public class ProtolintWarning {
    private Integer line;
    private Integer column;
    private String reason;

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
        return column;
    }

    public String getReason() {
        return reason;
    }
}
