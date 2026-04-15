package events.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EventController {


	public ResultSet[] checkPermanentReminds(Connection co) {
		ResultSet[] ress= new ResultSet[2];
		
		Statement stRemindEvent= null;
		Statement stInProgressEvent= null;
		try {
			stRemindEvent = co.createStatement();
			stInProgressEvent = co.createStatement();
		} catch (SQLException e) {
			System.out.println("Impossible de créer les statements: EventController");
			e.printStackTrace();
		}

		//EVENTS WICH HAVEN'T STARTED BUT THE REMINDER IS PASSED
		try {
			ress[0]= stRemindEvent.executeQuery("SELECT * from event where DATETIME(rappel)< datetime('now', 'localtime') AND DATETIME(date_debut)> DATETIME('now', 'localtime')");
			if (!ress[0].isBeforeFirst()) {
				ress[0]= null;
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: EventController/liste permanente");
			e.printStackTrace();
		}

		//EVENTS IN PROGRESS
		try {
			ress[1]= stInProgressEvent.executeQuery("SELECT * from event where DATETIME(date_debut)< datetime('now', 'localtime') AND DATETIME(date_fin)> DATETIME('now', 'localtime')");
			if (!ress[1].isBeforeFirst()) {
				ress[1]= null;
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: EventController/liste permanente");
			e.printStackTrace();
		}

		return ress;
	}

}
