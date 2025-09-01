package com.lemicare.delivery.service.config;

import com.cosmicdoc.common.repository.DeliveryOrderRepository;
import com.cosmicdoc.common.repository.impl.DeliveryOrderRepositoryImpl;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityConfiguration {

    @Bean
    DeliveryOrderRepository deliveryOrderRepository (Firestore firestore) {
        return new DeliveryOrderRepositoryImpl(firestore);
    }
}
