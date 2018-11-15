package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    private EntityManager manager;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();
    private SessionContext context;

    @Override
    public Set<String> getAllRentalCompanies() {
        List<String> resultList = this.manager.createNamedQuery("getAllRentalCompanyNames").getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarRentalCompany> resultList = this.manager.createNamedQuery("getAllRentalCompanies").getResultList();
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        if(resultList == null) {
            resultList = new ArrayList<CarRentalCompany>();
        }
        for(CarRentalCompany crc : resultList) {
            for (CarType ct : crc.getAvailableCarTypes(start, end)){
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            } 
        }
        return availableCarTypes;
    }
    
    @Override
    public void printAvailableCarTypes(Date start, Date end) {
        List<CarRentalCompany> resultList = this.manager.createNamedQuery("getAllRentalCompanies").getResultList();
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        if(resultList == null) {
            resultList = new ArrayList<CarRentalCompany>();
        }
        for(CarRentalCompany crc : resultList) {
            for (CarType ct : crc.getAvailableCarTypes(start, end)){
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            } 
        }

        for (CarType type : availableCarTypes) {
            System.out.println(type.toString());
        }
    }

    @Override
    public Quote createQuote(String region, ReservationConstraints constraints) throws ReservationException {
        List<CarRentalCompany> companies = this.manager.createNamedQuery("getAllRentalCompanies").getResultList();
        for (CarRentalCompany company : companies) {
            try {
                Quote quote = company.createQuote(constraints, this.renter);
                this.quotes.add(quote);
                return quote;
            }
            catch (ReservationException exc) {
            }
        }
        throw new ReservationException("No cars available in region " + region + "for dates " + 
                constraints.getStartDate().toString() + "-" + constraints.getEndDate().toString());
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws Exception {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany crc = this.manager.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(crc.confirmQuote(quote));
            }
            
        } catch (Exception e) {
            for(Reservation r:done){
                CarRentalCompany crcRollBack = this.manager.find(CarRentalCompany.class, r.getRentalCompany());
                crcRollBack.cancelReservation(r);                
            }
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
    
    @Override
    public CarType getCheapestCarType(Date start, Date end, String region) {
        List<CarRentalCompany> crc = this.manager.createNamedQuery("getAllRentalCompanies").getResultList();
        CarType result = null;
        for (CarRentalCompany company : crc) {
            if (company.getRegions().contains(region)) {
                List<CarType> resultList = this.manager.createNamedQuery("getCheapestCarTypeOfCompany")
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("companyName", company.getName())
                .getResultList();
                if (resultList == null)
                    throw new IllegalStateException("No carTypes");
                if (result == null || resultList.get(0).getRentalPricePerDay() < result.getRentalPricePerDay())
                    result = resultList.get(0);
            }
        }
        return result;
    }
}