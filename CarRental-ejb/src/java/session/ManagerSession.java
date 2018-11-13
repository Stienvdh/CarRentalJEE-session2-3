package session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.*;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager manager;
    
    private String managerName;
    private String crcName;
    
    @Override
    public void setManagerName(String name) {
        this.managerName = name;
    }
    
    @Override
    public void setCrcName(String name) {
        this.crcName = name;
    }
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            return new HashSet<CarType>(manager.createQuery("getCarTypes").getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            return RentalStore.getRental(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }
    
    @Override
    public void addCarRentalCompany(String name, List<Object[]> cars, List<String> regions) {
        List<Car> carsList = new ArrayList<Car>();
        
        for (Object[] arr : cars) {
            Car newCar = new Car((Integer)arr[0],(CarType)arr[1]);
            carsList.add(newCar);
        }
        
        CarRentalCompany company = new CarRentalCompany(name, regions, carsList);
        List<Car> carsToCopy = company.popAllCars();
        manager.persist(company);
        CarRentalCompany companyEntry = manager.find(CarRentalCompany.class, company.getName());
        
        for (Car car : carsToCopy) {
            CarType type = manager.find(CarType.class,car.getType().getName());
            if (type != null) {
                car.setType(type);
            }
            companyEntry.addCar(car);
        }
    }

}