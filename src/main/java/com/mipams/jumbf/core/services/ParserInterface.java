package com.mipams.jumbf.core.services;

import java.io.InputStream;

import com.mipams.jumbf.core.util.MipamsException;

public interface ParserInterface{

    public String parseMetadataFromJumbfFile(String path) throws MipamsException;

}