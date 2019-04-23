/**
 * Created by Andreas on 01.02.2019.
 */
public class Depot {

    //coordinates
    //max duration of a route
    // max load for vehicles from this depot

    public int id;
    public int xCoord;
    public int yCoord;
    public int maxRouteDuration;
    public int maxLoadVehicles;


    public Depot(int id, int maxRouteDuration, int maxLoadVehicles) {
        this.id = id;

        this.maxRouteDuration = maxRouteDuration;
        this.maxLoadVehicles = maxLoadVehicles;
    }


    @Override
    public String toString() {
        return "Depot{" +
                "id=" + id +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                ", maxRouteDuration=" + maxRouteDuration +
                ", maxLoadVehicles=" + maxLoadVehicles +
                '}';
    }
}
