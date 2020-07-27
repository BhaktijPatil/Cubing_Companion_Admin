package com.cubenama.cubingcompanionadmin;

import com.google.firebase.Timestamp;

class CompetitionEventRound {

    String roundId;
    long roundNo;
    String eventName;
    long qualificationCriteria;
    Timestamp startTime;
    Timestamp endTime;

    CompetitionEventRound(String eventName, long roundNo, String roundId, long qualificationCriteria, Timestamp startTime, Timestamp endTime)
    {
        this.eventName = eventName;
        this.roundNo = roundNo;
        this.roundId = roundId;
        this.qualificationCriteria = qualificationCriteria;
        this.startTime = startTime;
        this.endTime= endTime;
    }
}
