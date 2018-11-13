package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<String> getAllRentalCompanies();
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
   
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public void addCarRentalCompany(String name, List<Object[]> cars, List<String> regions);
    
    public void setCrcName(String name);
    
    public void setManagerName(String name);
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarTypeOfCompany(String company, String year);
      
}