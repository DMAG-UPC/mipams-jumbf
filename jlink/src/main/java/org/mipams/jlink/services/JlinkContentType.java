package org.mipams.jlink.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.ParseMetadata;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.services.content_types.ContentTypeService;
import org.mipams.jumbf.services.content_types.ContiguousCodestreamContentType;
import org.mipams.jumbf.services.content_types.XmlContentType;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
import java.util.logging.Level;

@Service
public class JlinkContentType implements ContentTypeService {

    private static final Logger logger = Logger.getLogger(JlinkContentType.class.getName());

    @Autowired
    JumbfBoxService jumbfBoxService;

    @Override
    public String getContentTypeUuid() {
        return "4C494E4B-0011-0010-8000-00AA00389B71";
    }

    @Override
    public List<BmffBox> parseContentBoxesFromJumbfFile(InputStream input, ParseMetadata parseMetadata)
            throws MipamsException {

        logger.log(Level.FINE, "Parsing new JLINK box");

        List<BmffBox> contentBoxes = new ArrayList<>();

        JumbfBox xmlContentTypeJumbfBox = jumbfBoxService.parseFromJumbfFile(input, parseMetadata);

        if (!isXmlContentType(xmlContentTypeJumbfBox)) {
            throw new MipamsException("First content box shall be of XML Content Type");
        }

        contentBoxes.add(xmlContentTypeJumbfBox);

        long remainingBytes = parseMetadata.getAvailableBytesForBox()
                - xmlContentTypeJumbfBox.getBoxSizeFromBmffHeaders();

        JumbfBox jumbfBox = null;

        while (remainingBytes > 0) {

            if (CoreUtils.isPaddingBoxNext(input)) {
                break;
            }

            jumbfBox = jumbfBoxService.parseFromJumbfFile(input, parseMetadata);

            if (isContiguousCodestreamContentType(jumbfBox)) {
                logger.log(Level.FINE, "JP2C Content JUMBF Box");
            } else if (isJlinkContentType(jumbfBox)) {
                logger.log(Level.FINE, "JLINK Content JUMBF Box");
            } else {
                throw new MipamsException("Only Codestream and JLINK Content types are supported.");
            }

            if (jumbfBox.getDescriptionBox().getLabel() == null) {
                throw new MipamsException(
                        "Label not found. JLINK requires content JUMBF Boxes to have label: " + jumbfBox.toString());
            }

            contentBoxes.add(jumbfBox);
            remainingBytes -= jumbfBox.getBoxSizeFromBmffHeaders();

        }

        return contentBoxes;
    }

    private boolean isJlinkContentType(JumbfBox jumbfBox) {
        return getContentTypeUuid().equals(jumbfBox.getDescriptionBox().getUuid());
    }

    private boolean isXmlContentType(JumbfBox jumbfBox) {
        ContentTypeService xmlContentTypeService = new XmlContentType();
        return xmlContentTypeService.getContentTypeUuid().equals(jumbfBox.getDescriptionBox().getUuid());
    }

    private boolean isContiguousCodestreamContentType(JumbfBox jumbfBox) {
        ContentTypeService contiguousCodestreamContentTypeService = new ContiguousCodestreamContentType();
        return contiguousCodestreamContentTypeService.getContentTypeUuid()
                .equals(jumbfBox.getDescriptionBox().getUuid());
    }

    @Override
    public void writeContentBoxesToJumbfFile(List<BmffBox> inputBoxList, OutputStream outputStream)
            throws MipamsException {

        logger.info("Calculating content: ");

        for (BmffBox contentBox : inputBoxList) {
            JumbfBox jumbfBox = (JumbfBox) contentBox;

            logger.info("Content Box size: " + jumbfBox.getBoxSizeFromBmffHeaders());
            jumbfBoxService.writeToJumbfFile(jumbfBox, outputStream);
        }
    }
}
