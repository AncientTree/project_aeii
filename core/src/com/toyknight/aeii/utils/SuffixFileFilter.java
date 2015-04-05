package com.toyknight.aeii.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by toyknight on 4/4/2015.
 */
public class SuffixFileFilter implements FileFilter {

    private final String suffix;

    public SuffixFileFilter(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        } else {
            return f.isFile() && f.getName().endsWith("." + suffix);
        }
    }

}
