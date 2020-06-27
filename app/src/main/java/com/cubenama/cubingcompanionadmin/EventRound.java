package com.cubenama.cubingcompanionadmin;

import com.google.firebase.Timestamp;

class EventRound {

    String roundId;
    long roundNo;
    long participantCount;
    Timestamp startTimestamp;
    Timestamp endTimestamp;

    EventRound(String roundId, long roundNo, long participantCount, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.roundId = roundId;
        this.roundNo = roundNo;
        this.participantCount = participantCount;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
