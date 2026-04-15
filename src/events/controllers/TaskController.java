package events.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TaskController {


	public ResultSet[] checkPermanentReminds(Connection co) {
		ResultSet[] ress= new ResultSet[2];
		
		Statement stRemindTask= null;
		Statement stInProgressTask= null;
		try {
			stRemindTask = co.createStatement();
			stInProgressTask = co.createStatement();
		} catch (SQLException e) {
			System.out.println("Impossible de créer les statements: TaskController");
			e.printStackTrace();
		}

		//TASKS WICH HAVEN'T STARTED BUT THE REMINDER IS PASSED
		try {
			ress[0]= stRemindTask.executeQuery("SELECT * from task where DATETIME(rappel)< datetime('now', 'localtime') AND DATETIME(date_debut)> datetime('now', 'localtime') AND is_done= 0");
			if (!ress[0].isBeforeFirst()) {
				ress[0]= null;
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: TaskController/liste permanente");
			e.printStackTrace();
		}

		//TASKS IN PROGRESS
		try {
			ress[1]= stInProgressTask.executeQuery("SELECT * from task where DATETIME(date_debut)< datetime('now', 'localtime') AND is_done= 0");
			if (!ress[1].isBeforeFirst()) {
				ress[1]= null;
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: TaskController/liste permanente");
			e.printStackTrace();
		}

		return ress;
	}

}
