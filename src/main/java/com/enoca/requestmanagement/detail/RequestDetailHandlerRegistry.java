package com.enoca.requestmanagement.detail;

import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestDetailHandlerRegistry {

    private final Map<RequestType, RequestDetailHandler> handlers = new EnumMap<>(RequestType.class);

    public RequestDetailHandlerRegistry(List<RequestDetailHandler> discoveredHandlers) {
        discoveredHandlers.forEach(handler -> {
            RequestDetailHandler previous = handlers.put(handler.supportedType(), handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Aynı talep tipi için birden fazla handler tanımlı: " + handler.supportedType());
            }
        });
    }

    public RequestDetailHandler resolve(RequestType requestType) {
        RequestDetailHandler handler = handlers.get(requestType);
        if (handler == null) {
            throw new BusinessRuleException("Bu talep tipi desteklenmiyor: " + requestType);
        }
        return handler;
    }
}
