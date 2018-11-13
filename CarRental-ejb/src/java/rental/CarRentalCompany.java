package rental;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.*;

@NamedQueries({
    
@NamedQuery(
    name = "getAllRentalCompanies",
    query= "SELECT company FROM CarRentalCompany company"
),
    
@NamedQuery(
    name = "getAllRentalCompanyNames",
    query= "SELECT company.name FROM CarRentalCompany company"
),
    
@NamedQuery(
    name = "getAllCarTypesOfCompany",
    query= "SELECT DISTINCT car.type FROM Car car, CarRentalCompany company "
        + "WHERE car MEMBER OF company.cars "
        + "AND company.name = :companyName"
),

// TODO er stond zo'n methode provided maar is dit nog nodig?
@NamedQuery(
    name = "getAllCarIds",
    query= "SELECT car.id FROM Car car, CarRentalCompany company "
        + "WHERE company.name = :companyName "
        + "AND car MEMBER OF company.cars"
),

//TODO nergens gebruikt
@NamedQuery(
    name = "getAllCarIdsOfType",
    query= "SELECT car.id FROM Car car, CarRentalCompany company "
        + "WHERE company.name = :companyName "
        + "AND car MEMBER OF company.cars "
        + "AND car.type.name = :carTypeName"
),

// TODO er stond zo'n methode provided maar is dit nog nodig?
@NamedQuery(
    name = "getAllReservationsForCarId",
    query= "SELECT reservation FROM Reservation reservation, CarRentalCompany company, Car car "
        + "WHERE company.name = :companyName "
        + "AND car.id = :carId "
        + "AND reservation MEMBER OF car.reservations"
),

@NamedQuery(
    name = "getAllReservationsForCarType",
    query= "SELECT reservation FROM Reservation reservation, Car car "
        + "WHERE reservation.rentalCompany = :companyName "
        + "AND car.type.name = :carTypeName "
        + "AND reservation MEMBER OF car.reservations"
),

@NamedQuery(
    name = "getBestClients",
    query = "SELECT reservation.carRenter, COUNT(reservation) AS total FROM CarRentalCompany company, Reservation reservation "
        + "WHERE reservation.rentalCompany = company.name "
        + "GROUP BY reservation.carRenter "
        + "ORDER BY total DESC"
),

//@NamedQuery(
//    name = "getMostPopularCarTypeOfCompany",
//    query= "SELECT carType, COUNT(carType) AS total FROM Reservation reservation, CarType carType "
//        + "WHERE reservation.rentalCompany = :companyName "
//            +"AND carType.companyName = :companyName "
//            +"AND reservation.getStartDate >= :year+'0101'"
//            +"AND reservation.getStartDate <= :year+'1231'"
//        + "GROUP BY carType "
//        + "ORDER BY total DESC"
//),

//@NamedQuery(
//     name = "getCheapestCarType",
//     query = "SELECT carType, MIN(car.type.rentalPricePerDay) AS price FROM CarRentalCompany company, Car car, CarType carType "
//        + "WHERE car.isAvailable(:startDate,:endDate) "
//        + "AND company.regions.contains(:region) "
//        + "GROUP BY carType "
//        + "ORDER BY price ASC"
//)
})

@Entity
public class CarRentalCompany implements Serializable {

    private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
    @Id private String name;
    @OneToMany(cascade=CascadeType.ALL) private List<Car> cars;
    @ManyToMany(cascade=CascadeType.ALL) private Set<CarType> carTypes = new HashSet<CarType>();
    private List<String> regions;
	
    /***************
     * CONSTRUCTOR *
     ***************/

    public CarRentalCompany(){};
    
    public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
        logger.log(Level.INFO, "<{0}> Starting up CRC {0} ...", name);
        setName(name);
        this.cars = cars;
        setRegions(regions);
    }

    /********
     * NAME *
     ********/
    
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /***********
     * Regions *
     **********/
    private void setRegions(List<String> regions) {
        this.regions = regions;
    }
    
    public List<String> getRegions() {
        return this.regions;
    }

    /*************
     * CAR TYPES *
     *************/
    
    public Collection<CarType> getAllTypes() {
        return carTypes;
    }

    public CarType getType(String carTypeName) {
        for(CarType type:carTypes){
            if(type.getName().equals(carTypeName))
                return type;
        }
        throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);
    }

    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
        return getAvailableCarTypes(start, end).contains(getType(carTypeName));
    }

    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (Car car : cars) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }
    
    public void addCarType(CarType type) {
        this.carTypes.add(type);
    }

    /*********
     * CARS * 
     *********/
    
    public List<Car> popAllCars() {
        this.cars = new ArrayList<Car>();
        return this.cars;
    }
    
    public void addCar(Car car) {
        this.cars.add(car);
    }
    
    public Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
    }

    public Set<Car> getCars(CarType type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (car.getType().equals(type)) {
                out.add(car);
            }
        }
        return out;
    }
    
    public Set<Car> getCars(String type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (type.equals(car.getType().getName())) {
                out.add(car);
            }
        }
        return out;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<Car>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[]{name, guest, constraints.toString()});


        if (!this.regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + name
                    + "> No cars available to satisfy the given constraints.");
        }
		
        CarType type = getType(constraints.getCarType());

        double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(), constraints.getEndDate());

        return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
                / (1000 * 60 * 60 * 24D));
    }

    public Reservation confirmQuote(Quote quote) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));

        Reservation res = new Reservation(quote, car.getId());
        car.addReservation(res);
        return res;
    }

    public void cancelReservation(Reservation res) {
        logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
        getCar(res.getCarId()).removeReservation(res);
    }
    
    public Set<Reservation> getReservationsBy(String renter) {
        logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
        Set<Reservation> out = new HashSet<Reservation>();
        for(Car c : cars) {
            for(Reservation r : c.getReservations()) {
                if(r.getCarRenter().equals(renter))
                    out.add(r);
            }
        }
        return out;
    }
}