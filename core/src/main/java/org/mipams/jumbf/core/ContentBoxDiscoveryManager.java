package org.mipams.jumbf.core;

import org.mipams.jumbf.core.util.BoxTypeEnum;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mipams.jumbf.core.services.ContentBoxService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class ContentBoxDiscoveryManager {

    private static final Logger logger = LoggerFactory.getLogger(ContentBoxDiscoveryManager.class);

    @Autowired
    protected List<ContentBoxService> contentBoxServiceList;

    public ContentBoxService getContentBoxServiceBasedOnContentUUID(UUID uuid) throws MipamsException {

        BoxTypeEnum boxType = BoxTypeEnum.getBoxTypeFromContentUuidOrNull(uuid);

        if (boxType == null) {
            throw new MipamsException("Box with uuid" + uuid.toString() + " is not a Content Box");
        }

        return generateContentBoxServiceBasedOnBoxWithId(boxType.getTypeId());
    }

    public ContentBoxService generateContentBoxServiceBasedOnBoxWithId(int boxId) throws MipamsException {

        for (ContentBoxService service : contentBoxServiceList) {
            logger.info(service.getClass().getName());
            if (boxId == service.serviceIsResponsibleForBoxTypeId()) {
                return service;
            }
        }

        throw new MipamsException("Box type with id: 0x" + Integer.toHexString(boxId) + " is not supported yet");
    }
}