/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.index.fielddata;

import org.apache.lucene.tests.util.LuceneTestCase;

@LuceneTestCase.AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/98720")
public class SortedSetDVStringFieldDataTests extends AbstractStringFieldDataTestCase {

    @Override
    protected String getFieldDataType() {
        return "string";
    }

    @Override
    protected boolean hasDocValues() {
        return true;
    }

    @Override
    protected long minRamBytesUsed() {
        return 0;
    }
}
