package com.cubenama.cubingcompanionadmin;

import java.util.ArrayList;
import java.util.List;

class Event {
    String eventId;
    String eventName;
    Long solveCount;
    List<EventRound> eventRounds;

    Event(Long solveCount, String eventId, String eventName)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.solveCount = solveCount;
        eventRounds = new ArrayList<>();
    }
}
