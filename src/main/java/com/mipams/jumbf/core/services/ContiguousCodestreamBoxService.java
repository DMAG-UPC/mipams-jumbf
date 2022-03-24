package com.mipams.jumbf.core.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.mipams.jumbf.core.entities.ContiguousCodestreamBox;
import com.mipams.jumbf.core.entities.XTBox;
import com.mipams.jumbf.core.util.MipamsException;
import com.mipams.jumbf.core.util.BadRequestException;
import com.mipams.jumbf.core.util.BoxTypeEnum;
import com.mipams.jumbf.core.util.CoreUtils;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContiguousCodestreamBoxService extends XTBoxService{

    private static final Logger logger = LoggerFactory.getLogger(ContiguousCodestreamBoxService.class); 

    @Value("${mipams.core.image_folder}")
    private String IMAGE_FOLDER;

    @Value("${mipams.core.max_file_size_in_bytes}")
    private long MAX_FILE_SIZE;

    @Override
    protected ContiguousCodestreamBox initializeBox() throws MipamsException{
        return new ContiguousCodestreamBox();
    }

    @Override
    protected void populateBox(XTBox xtBox, ObjectNode input) throws MipamsException{
        
        ContiguousCodestreamBox contiguousCodeStreamBox = (ContiguousCodestreamBox) xtBox;
        
        String type = input.get("type").asText();

        if( !contiguousCodeStreamBox.getBoxType().equals(type)){
            throw new BadRequestException("Box type does not match with description type.");
        }

        String path = input.get("path").asText();

        if(path == null) {
            throw new BadRequestException("Path is not specified");
        }

        contiguousCodeStreamBox.setPathToCodestream(path);

        if(doesFileSizeExceedApplicationLimits(path)){
            throw new BadRequestException("File is too large for the application. Check the available limits.");
        }
    }

    protected boolean doesFileSizeExceedApplicationLimits(String filePath) throws MipamsException{
        double size = CoreUtils.getFileSizeFromPath(filePath);
        return size > MAX_FILE_SIZE || size > Long.MAX_VALUE;        
    }

    @Override
    protected void writeXTBoxPayloadToJumbfFile(XTBox xtBox, FileOutputStream fileOutputStream) throws MipamsException{

        ContiguousCodestreamBox contiguousCodeStreamBox = (ContiguousCodestreamBox) xtBox;

        try (FileInputStream inputStream = new FileInputStream(contiguousCodeStreamBox.getPathToCodestream())){

            int n;
            while ((n = inputStream.read()) != -1) {
                fileOutputStream.write(n);
            }  
        } catch(FileNotFoundException e){
            throw new MipamsException("Coulnd not locate file", e);
        } catch (IOException e){
            throw new MipamsException("Coulnd not write to file", e);
        }
    }

    @Override
    protected void populatePayloadFromJumbfFile(XTBox xtBox, InputStream input) throws MipamsException{
        logger.debug("Contiguous Codestream box");

        ContiguousCodestreamBox contiguousCodeStreamBox = (ContiguousCodestreamBox) xtBox;

        String fileName = CoreUtils.randomStringGenerator() + ".jpeg";

        String fullPath = CoreUtils.getFullPath(IMAGE_FOLDER, fileName);

        contiguousCodeStreamBox.setPathToCodestream(fullPath);

        try (FileOutputStream fileOutputStream = new FileOutputStream(fullPath)){            
            
            long nominalTotalSizeInBytes = contiguousCodeStreamBox.getPayloadSizeFromXTBoxHeaders();

            int actualBytes = 0, n;

            while ((actualBytes < nominalTotalSizeInBytes) && ((n = input.read()) != -1)){
                fileOutputStream.write(n);
                actualBytes++;
            }

            logger.debug("Finished writing file to: "+fullPath);

        } catch (IOException e){
            throw new MipamsException("Coulnd not read Json content", e);
        } 
    }

    @Override
    public BoxTypeEnum serviceIsResponsibleForBoxType(){
        return BoxTypeEnum.ContiguousCodestreamBox;
    }
}