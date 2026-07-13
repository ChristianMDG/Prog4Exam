package com.exam.app.file.hash;

import com.exam.app.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
