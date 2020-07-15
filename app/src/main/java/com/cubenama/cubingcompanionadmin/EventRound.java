package com.cubenama.cubingcompanionadmin;

import com.google.firebase.Timestamp;

class EventRound {

    String roundId;
    long roundNo;
    long qualificationCriteria;
    Timestamp startTimestamp;
    Timestamp endTimestamp;

    EventRound(String roundId, long roundNo, long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.roundId = roundId;
        this.roundNo = roundNo;
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
