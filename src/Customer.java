import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by Andreas on 01.02.2019.
 */
public class Customer {

    //id
    //coordinates x and y
    // necessary service duration for this customer
    // demand for this customer

    public int id;
    public int xCoord;
    public int yCoord;
    public int serviceDuration;
    public int demand;
    public double distanceClosestDepot;
    public Depot closestDepot;
    public int nearestNeighboorID;


    public Customer(int id, int xCoord, int yCoord, int serviceDuration, int demand) {
        this.id = id;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.serviceDuration = serviceDuration;
        this.demand = demand;
    }

    public void setClosestNeighboorIndex(ArrayList<Customer> customers){
        double bestDistance = Double.POSITIVE_INFINITY;
        for(int i = 0; i < customers.size(); i++){
            double distance = Point2D.distance(this.xCoord, this.yCoord, customers.get(i).xCoord, customers.get(i).yCoord);
            if (distance < bestDistance){
                bestDistance = distance;
                this.nearestNeighboorID = customers.get(i).id;
            }
        }
    }

    public double getDistanceClosestDepot(){
        return this.distanceClosestDepot;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                ", serviceDuration=" + serviceDuration +
                ", demand=" + demand +
                ", distanceClosestDepot=" + distanceClosestDepot +
                ", closestDepot=" + closestDepot +
                '}';
    }
}




