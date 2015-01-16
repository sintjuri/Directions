package com.onettm.directions;

public enum Cardinal {
    North(0f, 45f, "N "),
    NorthEast(45f, 90f, "NE"),
    East(90f, 135f, "E "),
    SouthEast(135f, 180f, "SE"),
    South(180f, 225f, "S "),
    SouthWest(225f, 270f, "SW"),
    West(270f, 315f, "W "),
    NorthWest(315f, 360f, "NW");


    private float from;
    private float to;
    private String label;


    Cardinal(float from, float to, String label){
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public static Cardinal getCardinal(float bearing){
        if(bearing < 0){
            bearing += 360;
        }
        bearing +=22.5f;
        bearing = bearing % 360;

        for(Cardinal cardinal : Cardinal.values()){
            if((cardinal.from <= bearing) && (bearing<cardinal.to)){
                return cardinal;
            }
        }
        return null;
    }

    public String getLabel(){
        return label;
    }
}
