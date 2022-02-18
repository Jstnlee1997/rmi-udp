package common;

/* February 2022
 * Utility class that encapsulates the Location information to
 * be passed from client to server.  Class is Serializable to allow for the use of RMI
 */

import java.io.Serializable;

public class Location implements Serializable {
    private double longitude;
    private double latitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude () {
        return latitude;
    }

    public double getLongitude () {
        return longitude;
    }

}
