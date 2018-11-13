package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    private EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        List<String> resultList = this.em.createNamedQuery("getAllRentalCompanyNames").getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarRentalCompany> resultList = this.em.createNamedQuery("getAllRentalCompanies").getResultList();
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
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        CarRentalCompany crc = this.em.find(CarRentalCompany.class, company);
        if (crc == null) {
            throw new ReservationException("Company " + company + " doesn't exist.");
        }
        Quote out = crc.createQuote(constraints, renter);
        quotes.add(out);
        return out;
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany crc = this.em.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(crc.confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done){
                CarRentalCompany crcRollBack = this.em.find(CarRentalCompany.class, r.getRentalCompany());
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
}