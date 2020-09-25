package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

	// private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	@InjectMocks
	private static FareCalculatorService fareCalculatorService;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void calculateFareCar() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareUnkownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th parking
																							// fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th parking
																							// fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																									// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThirtyMinuteParkingTime() {
		long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

		Calendar inTime = Calendar.getInstance();
		long t = inTime.getTimeInMillis();
		Date outTime = new Date(t + (30 * ONE_MINUTE_IN_MILLIS));
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime.getTime());
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		// assertEquals( (0.5 * Fare.FREE_FARE) , ticket.getPrice() );
		assertThat(ticket.getPrice()).isEqualTo(0.5 * Fare.FIRST_THIRTY_MINUTE_FREE);

	}

	@Test
	public void calculateFareBikeWithLessThirtyMinuteParkingTime() {
		long ONE_MINUTE_IN_MILLIS = 60000;// millisecs
		Calendar inTime = Calendar.getInstance();
		long t = inTime.getTimeInMillis();
		Date outTime = new Date(t + (30 * ONE_MINUTE_IN_MILLIS));
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime.getTime());
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertThat(ticket.getPrice()).isEqualTo(0.5 * Fare.FIRST_THIRTY_MINUTE_FREE);

	}
/**
	@Test
	public void calculateFareWithDiscountCinqPerCentReccurentCar() {
		long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

		Calendar inTime = Calendar.getInstance();
		long t = inTime.getTimeInMillis();
		Date outTime = new Date(t + (60 * ONE_MINUTE_IN_MILLIS));
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime.getTime());
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setIsAvailableDiscount(true);
		fareCalculatorService.calculateFare(ticket);
		Double discount = (Fare.CAR_RATE_PER_HOUR * 5) / 100;
		assertThat(ticket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR - discount);
	}
	*/

/**	@Test
	public void calculateFareWithDiscountCinqPerCentReccurentBike() {
		long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

		Calendar inTime = Calendar.getInstance();

		long t = inTime.getTimeInMillis();
		Date outTime = new Date(t + (60 * ONE_MINUTE_IN_MILLIS));
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime.getTime());
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setIsAvailableDiscount(true);
		fareCalculatorService.calculateFare(ticket);
		Double discount = (Fare.BIKE_RATE_PER_HOUR * 5) / 100;
		assertThat(ticket.getPrice()).isEqualTo(Fare.BIKE_RATE_PER_HOUR - discount);
	}
	*/
	
	  @Test
		public void discountCarUser() {
	    	 ticket.setId(1);
	    	 ticket.setVehicleRegNumber("ABCDEF");
	    	 ticket.setPrice(45.0);
	    	 when(parkingSpotDAO.checkClientExist(any(Ticket.class))).thenReturn(true);
	 		double fare = fareCalculatorService.userDiscount(ticket);
			assertEquals(42.75, fare);   

		}

}
