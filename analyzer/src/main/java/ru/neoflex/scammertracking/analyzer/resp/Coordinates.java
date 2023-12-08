package ru.neoflex.scammertracking.analyzer.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coordinates {
    public Coordinates(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinates() {
    }

    private float latitude;
    private float longitude;
}
