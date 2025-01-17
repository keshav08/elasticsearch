/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.formatter;

import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.core.CheckedConsumer;
import org.elasticsearch.xpack.esql.action.EsqlQueryResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Formats {@link EsqlQueryResponse} for the textual representation.
 */
public class TextFormatter {
    /**
     * The minimum width for any column in the formatted results.
     */
    private static final int MIN_COLUMN_WIDTH = 15;

    private final EsqlQueryResponse response;
    private final int[] width;
    private final Function<Object, String> FORMATTER = Objects::toString;

    /**
     * Create a new {@linkplain TextFormatter} for formatting responses.
     */
    public TextFormatter(EsqlQueryResponse response) {
        this.response = response;
        var columns = response.columns();
        // Figure out the column widths:
        // 1. Start with the widths of the column names
        width = new int[columns.size()];
        for (int i = 0; i < width.length; i++) {
            // TODO read the width from the data type?
            width[i] = Math.max(MIN_COLUMN_WIDTH, columns.get(i).name().length());
        }

        // 2. Expand columns to fit the largest value
        for (var row : response.values()) {
            for (int i = 0; i < width.length; i++) {
                width[i] = Math.max(width[i], FORMATTER.apply(row.get(i)).length());
            }
        }
    }

    /**
     * Format the provided {@linkplain EsqlQueryResponse} optionally including the header lines.
     */
    public Iterator<CheckedConsumer<Writer, IOException>> format(boolean includeHeader) {
        return Iterators.concat(
            // The header lines
            includeHeader && response.columns().size() > 0 ? Iterators.single(this::formatHeader) : Collections.emptyIterator(),
            // Now format the results.
            formatResults()
        );
    }

    private void formatHeader(Writer writer) throws IOException {
        for (int i = 0; i < width.length; i++) {
            if (i > 0) {
                writer.append('|');
            }

            String name = response.columns().get(i).name();
            // left padding
            int leftPadding = (width[i] - name.length()) / 2;
            writer.append(" ".repeat(Math.max(0, leftPadding)));
            writer.append(name);
            // right padding
            writer.append(" ".repeat(Math.max(0, width[i] - name.length() - leftPadding)));
        }
        writer.append('\n');

        for (int i = 0; i < width.length; i++) {
            if (i > 0) {
                writer.append('+');
            }
            writer.append("-".repeat(Math.max(0, width[i]))); // emdash creates issues
        }
        writer.append('\n');
    }

    private Iterator<CheckedConsumer<Writer, IOException>> formatResults() {
        return Iterators.map(response.values().iterator(), row -> writer -> {
            for (int i = 0; i < width.length; i++) {
                if (i > 0) {
                    writer.append('|');
                }
                String string = FORMATTER.apply(row.get(i));
                if (string.length() <= width[i]) {
                    // Pad
                    writer.append(string);
                    writer.append(" ".repeat(Math.max(0, width[i] - string.length())));
                } else {
                    // Trim
                    writer.append(string, 0, width[i] - 1);
                    writer.append('~');
                }
            }
            writer.append('\n');
        });
    }
}
