package org.mipams.jumbf.services.boxes;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import org.mipams.jumbf.entities.JsonBox;
import org.mipams.jumbf.entities.ServiceMetadata;

@Service
public class JsonBoxService extends MemoryBoxService<JsonBox> {

    ServiceMetadata serviceMetadata;

    @PostConstruct
    void init() {
        JsonBox box = initializeBox();
        serviceMetadata = new ServiceMetadata(box.getTypeId(), box.getType());
    }

    @Override
    protected JsonBox initializeBox() {
        return new JsonBox();
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }
}