package com.lemicare.delivery.service.strategy;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A factory for retrieving the correct DeliveryPartnerStrategy bean at runtime.
 * It uses Spring's dependency injection to automatically discover all available
 * strategy implementations. This means you never have to modify this factory
 * when you add a new delivery partner.
 */
@Component
public class DeliveryStrategyFactory {

    // Spring will automatically inject a list of all beans that implement DeliveryPartnerStrategy.
    private final List<DeliveryPartnerStrategy> strategies;
    private Map<String, DeliveryPartnerStrategy> strategyMap;

    // Constructor-based injection is a best practice.
    public DeliveryStrategyFactory(List<DeliveryPartnerStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * After the factory is constructed and dependencies are injected,
     * this method builds a map for quick, O(1) lookups of strategies by name.
     */
    @PostConstruct
    void initStrategies() {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DeliveryPartnerStrategy::getPartnerName, Function.identity()));
    }

    /**
     * Retrieves the strategy implementation for a given partner name.
     *
     * @param partnerName The name of the partner (e.g., "SHIPROCKET").
     * @return The corresponding strategy bean.
     * @throws IllegalArgumentException if no strategy is found for the given name.
     */
    public DeliveryPartnerStrategy getStrategy(String partnerName) {
        DeliveryPartnerStrategy strategy = strategyMap.get(partnerName);
        if (strategy == null) {
            throw new IllegalArgumentException("No delivery strategy found for partner: " + partnerName);
        }
        return strategy;
    }
}
