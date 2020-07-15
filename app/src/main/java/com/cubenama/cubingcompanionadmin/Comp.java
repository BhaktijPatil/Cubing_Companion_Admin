package com.cubenama.cubingcompanionadmin;

import com.google.firebase.Timestamp;

public class Comp {
    String id;
    String name;
    String organizer;
    long competitorLimit;
    boolean resultsVerified;
    Timestamp startTime;
    Timestamp endTime;
    Timestamp regStartTime;
    Timestamp regEndTime;


    Comp(String id, String name, String organizer, long competitorLimit, boolean resultsVerified, Timestamp startTime, Timestamp endTime, Timestamp regStartTime, Timestamp regEndTime)
    {
        this.id = id;
        this.name = name;
        this.organizer = organizer;
        this.competitorLimit = competitorLimit;
        this.resultsVerified = resultsVerified;
        this.startTime = startTime;
        this.endTime = endTime;
        this.regStartTime = regStartTime;
        this.regEndTime = regEndTime;
    }

}
