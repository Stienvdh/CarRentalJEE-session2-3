package session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
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
        int bestValue = (Integer) resultList.get(0)[1];
        for (Object[] obj : resultList) {
            if ((Integer)  resultList.get(0)[1] == bestValue) {
                resultSet.add((String)obj[0]);
            } 
        }
        return resultSet;
    }
    
    @Override
    public CarType getMostPopularCarTypeOfCompany(String company, String year) {
        List<CarType> resultList = this.manager.createNamedQuery("getMostPopularCarTypeOfCompany")
                .setParameter("companyName", company)
                .setParameter("year", year)
                .getResultList();
        if (resultList == null)
            throw new IllegalStateException("No carTypes at company " + company);
        return resultList.get(0);
    }
    
    public CarType getCheapestCarType(ReservationConstraints constraints) {
        List<CarType> resultList = this.manager.createNamedQuery("getCheapestCarType")
                .setParameter("startDate", constraints.getStartDate())
                .setParameter("endDate", constraints.getEndDate())
                .setParameter("region", constraints.getRegion())
                .getResultList();
        if (resultList == null)
            throw new IllegalStateException("No carTypes");
        return resultList.get(0);
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
            else {
                company.addCarType(type);
            }
            companyEntry.addCar(car);
        }
    }

}