package com.lemicare.delivery.service.config;

import com.cosmicdoc.common.repository.BranchRepository;
import com.cosmicdoc.common.repository.DeliveryOrderRepository;
import com.cosmicdoc.common.repository.StorefrontOrderRepository;
import com.cosmicdoc.common.repository.impl.BranchRepositoryImpl;
import com.cosmicdoc.common.repository.impl.DeliveryOrderRepositoryImpl;
import com.cosmicdoc.common.repository.impl.StorefrontOrderRepositoryImpl;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityConfiguration {

    @Bean
    DeliveryOrderRepository deliveryOrderRepository (Firestore firestore) {
        return new DeliveryOrderRepositoryImpl(firestore);
    }

    @Bean
    StorefrontOrderRepository storefrontOrderRepository (Firestore firestore) {
        return new StorefrontOrderRepositoryImpl(firestore);
    }

    @Bean
    BranchRepository branchRepository (Firestore firestore) {
        return new BranchRepositoryImpl(firestore);
    }
}
