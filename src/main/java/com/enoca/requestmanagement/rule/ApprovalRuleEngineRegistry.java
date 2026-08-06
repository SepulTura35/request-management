package com.enoca.requestmanagement.rule;

import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ApprovalRuleEngineRegistry {

    private final Map<RequestType, ApprovalRuleEngine> engines = new EnumMap<>(RequestType.class);

    public ApprovalRuleEngineRegistry(List<ApprovalRuleEngine> discoveredEngines) {
        discoveredEngines.forEach(engine -> {
            ApprovalRuleEngine previous = engines.put(engine.supportedType(), engine);
            if (previous != null) {
                throw new IllegalStateException(
                        "Ayni talep tipi icin birden fazla onay kurali tanimli: " + engine.supportedType());
            }
        });
    }

    public ApprovalRuleEngine resolve(RequestType requestType) {
        ApprovalRuleEngine engine = engines.get(requestType);
        if (engine == null) {
            throw new BusinessRuleException("Bu talep tipi icin onay kurali tanimli degil: " + requestType);
        }
        return engine;
    }
}
