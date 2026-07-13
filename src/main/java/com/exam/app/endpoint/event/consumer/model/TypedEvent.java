package com.exam.app.endpoint.event.consumer.model;

import com.exam.app.PojaGenerated;
import com.exam.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
