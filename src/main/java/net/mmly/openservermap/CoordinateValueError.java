package net.mmly.openservermap;

public class CoordinateValueError extends Exception { //done
    public CoordinateValueError(double lat, double lon) {
        super("Coordnates " + lat + ", " + lon + " out of range for projection.");
    }
}
