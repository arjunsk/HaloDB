/*
 * Copyright 2018, Oath Inc
 * Licensed under the terms of the Apache License 2.0. Please refer to accompanying LICENSE file for terms.
 */

package com.oath.halodb.pojo;

import com.oath.halodb.HaloDBFile;
import com.oath.halodb.IndexFile;
import com.oath.halodb.TombstoneFile;
import com.oath.halodb.domain.HaloDBFile;

import java.util.regex.Pattern;


public class Constants {

    // matches data and compacted files with extension .data and .datac respectively.
    static final Pattern DATA_FILE_PATTERN = Pattern.compile("([0-9]+)" + HaloDBFile.DATA_FILE_NAME + "c?");

    static final Pattern INDEX_FILE_PATTERN = Pattern.compile("([0-9]+)" + IndexFile.INDEX_FILE_NAME);

    static final Pattern TOMBSTONE_FILE_PATTERN = Pattern.compile("([0-9]+)" + TombstoneFile.TOMBSTONE_FILE_NAME);

    static final Pattern STORAGE_FILE_PATTERN = Pattern.compile("([0-9]+)\\.[a-z]+");
}
