package com.example.ja.airpollution;

/*
Symulacja odpowiedzi od API
 */


public class AirDataModel {
    public String cityname;
    public String pollutionlevel;
    public String datetime;
    public String childreninfo;
    public String sportinfo;

    public AirDataModel(){
        cityname = "Warsaw";
        pollutionlevel = "Good air quality";
        datetime = "datetime\": \"2017-03-06T06:53:58";
        childreninfo = "children: No reason to panic, but pay attention to changes in air quality and any signals of breathing problems in your children";
        sportinfo = "sport: You can go on a run - just keep your nose open for any changes!";
    }


}
