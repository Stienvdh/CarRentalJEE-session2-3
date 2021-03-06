package session;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.persistence.*;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager manager;
    
    private String managerName;
    private String crcName;
    
    @Override 
    public String getCrcName() {
        return this.crcName;
    }
    
    @Override
    public void setManagerName(String name) {
        this.managerName = name;
    }
    
    @Override
    public void setCrcName(String name) {
        this.crcName = name;
    }
    
    @Override
    public Set<String> getAllRentalCompanies() {
        List<String> resultList = this.manager.createNamedQuery("getAllRentalCompanyNames").getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        List<CarType> resultList = this.manager.createNamedQuery("getAllCarTypesOfCompany")
                .setParameter("companyName", company)
                .getResultList();
        if (resultList == null)
            return new HashSet<CarType>();
        return new HashSet<CarType>(resultList);
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        List<Integer> resultList = this.manager.createNamedQuery("getAllCarIds").getResultList();
        if (resultList == null)
            return new HashSet<Integer>();
        return new HashSet<Integer>(resultList);
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        List<Reservation> resultList = this.manager.createNamedQuery("getAllReservationsForCarId")
                .setParameter("companyName", company)
                .setParameter("carId", id)
                .getResultList();
        if (resultList == null) 
            return 0;
        return resultList.size();
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        List<Reservation> resultList = this.manager.createNamedQuery("getAllReservationsForCarType")
                .setParameter("companyName", company)
                .setParameter("carTypeName", type)
                .getResultList();
        if (resultList == null)
            return 0;
        return resultList.size();
    }
    
    @Override
    public Set<String> getBestClients() {
        List<Object[]> resultList = this.manager.createNamedQuery("getBestClients").getResultList();
        if (resultList == null)
            return new HashSet<String>();
        Set<String> resultSet = new HashSet<String>();
        long bestValue = (long) resultList.get(0)[1];
        for (Object[] obj : resultList) {
            if ((long)  obj[1] == bestValue) {
                resultSet.add((String)obj[0]);
            } 
        }
        return resultSet;
    }
    
    @Override
    public CarType getMostPopularCarTypeOfCompany(String company, int year) {
        List<Object[]> resultList = this.manager.createNamedQuery("getMostPopularCarTypeOfCompany")
                .setParameter("companyName", company)
                .setParameter("startyear", Date.valueOf(year + "-01-01" ))
                .setParameter("endyear", Date.valueOf(year + "-12-31" ))
                .getResultList();
        if (resultList == null)
            throw new IllegalStateException("No carTypes at company " + company);
        String result = (String) (resultList.get(0))[0];
        CarRentalCompany crc = (CarRentalCompany) this.manager.createNamedQuery("getCarRentalCompany")
                .setParameter("companyName", company)
                .getResultList().get(0);
        CarType type = (CarType) this.manager.createNamedQuery("getCarTypeFromCompany")
                .setParameter("type", result)
                .setParameter("companyName", company)
                .getResultList().get(0);
        return type;
    }
    
    
    
    @Override
    @TransactionAttribute(REQUIRED)
    public void addCarRentalCompany(String name, List<CarType> cars, List<String> regions) {
        List<Car> carsList = new ArrayList<Car>();
        
        for (CarType car : cars) {
            carsList.add(new Car(car));
        }

        CarRentalCompany company = new CarRentalCompany(name, regions, carsList);
        List<Car> carsToCopy = company.popAllCars();
        manager.persist(company);
        CarRentalCompany companyEntry = manager.find(CarRentalCompany.class, company.getName());
        
        for (Car car : carsToCopy) {
            CarType type = manager.find(CarType.class, car.getType().toString());
            if (type != null) {
                companyEntry.addCarType(type);
                car.setType(type);
            }
            else {
                companyEntry.addCarType(car.getType());
                car.setType(car.getType());
            }
            companyEntry.addCar(car);
        }
    }
    
    @Override 
    public void addCar(CarType typecar) {
        Car car = new Car(typecar);
        CarRentalCompany companyEntry = manager.find(CarRentalCompany.class, this.getCrcName());
        CarType type = manager.find(CarType.class, car.getType().toString());
            if (type != null) {
                companyEntry.addCarType(type);
                car.setType(type);
            }
        companyEntry.addCar(car);
    }
    
    @Override
    public void addCarType(CarType carType) {
        CarRentalCompany companyEntry = manager.find(CarRentalCompany.class, this.getCrcName());
        CarType type = manager.find(CarType.class, carType.toString());
            if (type != null) {
                companyEntry.addCarType(type);
            }
    }

}