package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		// TODO: Some tests are failing here. Need to check if this logic is correct
		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		double durationMilliSeconds = outHour - inHour;
		double duration = durationMilliSeconds / (1000 * 60 * 60);
		if (duration <= 0.5) {
			ticket.setPrice(duration * Fare.FIRST_THIRTY_MINUTE_FREE);
		} else {

			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {

				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				calculFareWithDiscount(ticket.getPrice(), ticket);
				
				break;
			}

			case BIKE: {

				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				calculFareWithDiscount(ticket.getPrice(), ticket);

				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		}

	}

	public void calculFareWithDiscount(Double price, Ticket ticket) {

		if (ticket.isIsAvailableDiscount()) {
			double discount = (price * 5) / 100;
			price = price - discount;
		}
		ticket.setPrice(price);
	}
}