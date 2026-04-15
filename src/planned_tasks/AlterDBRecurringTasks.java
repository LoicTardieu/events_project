package planned_tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimerTask;

public class AlterDBRecurringTasks extends TimerTask{

	@Override
	public void run() {

		Connection co= null;
		Statement st= null;
		Statement stUpdate= null;

		ResultSet res= null;

		try {
			co= DriverManager.getConnection("jdbc:sqlite:C:\\Users\\wotso\\Desktop\\codage\\projets_info\\java\\events\\src\\bdd\\events_db.db");
			co.setAutoCommit(false);
			st= co.createStatement();
			stUpdate= co.createStatement();
		}catch (SQLException e) {
			System.out.println("Impossible d'établir la connection à la BDD: AlterDBRecurringTasks");
			System.out.println(e.getMessage());
		}

		this.checkDailyTasks(co, st, stUpdate, res);
		this.checkWeeklyTasks(co, st, stUpdate, res);
		this.checkMonthlyTasks(co, st, stUpdate, res);
		this.checkYearlyTasks(co, st, stUpdate, res);

		try {
			co.setAutoCommit(true);
			co.close();
		} catch (SQLException e) {
			System.out.println("Impossible de fermer la connexion à la BDD: AlterDBRecurringTasks");
			e.printStackTrace();
		}
	}


	public void checkDailyTasks(Connection co, Statement st, Statement stUpdate, ResultSet res) {
		try {
			res= st.executeQuery("SELECT id_task, date_debut, rappel from task where is_daily= 1 AND is_validate_occurence= 1 AND DATETIME(date_debut)< DATETIME('now', 'localtime')");
			//	if start hour is passed
			//	then
			//		date_debut= datetime(now) + 1 day
			//	else
			//		date_debut= datetime(now)
			//	end if
			while(res.next()) {
				int idTask= res.getInt("id_task");
				String[] startDateStrings= res.getString("date_debut").split("-|T|:");
				String startTime= startDateStrings[3] + ":" + startDateStrings[4];
				String[] remindDateStrings= res.getString("rappel").split("-|T|:");
				String remindTime= remindDateStrings[3] + ":" + remindDateStrings[4];
				if(LocalTime.of(Integer.parseInt(startDateStrings[3]), Integer.parseInt(startDateStrings[4])).isBefore(LocalTime.now())) {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('now', '+1 day'), 'T', TIME('" + startTime + "')), rappel= CONCAT(DATE('now', '+1 day'), 'T', TIME('" + remindTime +"')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 where id_task= " + idTask);
				}
				else {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('now'), 'T', TIME('" + startTime +"')), rappel= CONCAT(DATE('now'), 'T', TIME('" + remindTime + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 where id_task= " + idTask);
				}
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: AlterDBRecurringTasks/daily");
			e.printStackTrace();
		}
	}


	public void checkWeeklyTasks(Connection co, Statement st, Statement stUpdate, ResultSet res) {
		try {
			res= st.executeQuery("SELECT id_task, date_debut, rappel from task where is_weekly= 1 AND is_validate_occurence= 1 AND DATETIME(date_debut)< DATETIME('now', 'localtime')");
			while(res.next()) {
				int idTask= res.getInt("id_task");
				int nbDayOfWeekToday= LocalDateTime.now().getDayOfWeek().getValue();
				long nbDaysToAddToToday= 0;
				
				String[] startDateStrings= res.getString("date_debut").split("-|T|:");
				LocalDateTime ldtStartDate= LocalDateTime.of(Integer.parseInt(startDateStrings[0]), Integer.parseInt(startDateStrings[1]), Integer.parseInt(startDateStrings[2]), Integer.parseInt(startDateStrings[3]), Integer.parseInt(startDateStrings[4]));
				int nbDayOfWeekStartDate= ldtStartDate.getDayOfWeek().getValue();
				nbDaysToAddToToday= nbDayOfWeekStartDate - nbDayOfWeekToday;
				LocalTime ltStartDate= LocalTime.of(ldtStartDate.getHour(), ldtStartDate.getMinute());
				LocalDateTime occurenceStartDate= LocalDateTime.of(LocalDate.now().plusDays(nbDaysToAddToToday), ltStartDate);
				LocalDate ldOccurenceStartDate= LocalDate.of(occurenceStartDate.getYear(), occurenceStartDate.getMonthValue(), occurenceStartDate.getDayOfMonth()); 
				LocalTime ltOccurenceStartDate= LocalTime.of(occurenceStartDate.getHour(), occurenceStartDate.getMinute()); 

				String[] remindDateStrings= res.getString("rappel").split("-|T|:");
				LocalDateTime ldtRemindDate= LocalDateTime.of(Integer.parseInt(remindDateStrings[0]), Integer.parseInt(remindDateStrings[1]), Integer.parseInt(remindDateStrings[2]), Integer.parseInt(remindDateStrings[3]), Integer.parseInt(remindDateStrings[4]));
				int dayOfWeekRemindDate= ldtRemindDate.getDayOfWeek().getValue();
				nbDaysToAddToToday= dayOfWeekRemindDate - nbDayOfWeekToday;
				LocalTime ltRemindDate= LocalTime.of(ldtRemindDate.getHour(), ldtRemindDate.getMinute());
				LocalDateTime occurenceRemindDate= LocalDateTime.of(LocalDate.now().plusDays(nbDaysToAddToToday), ltRemindDate);
				LocalDate ldOccurenceRemindtDate= LocalDate.of(occurenceRemindDate.getYear(), occurenceRemindDate.getMonthValue(), occurenceRemindDate.getDayOfMonth()); 
				LocalTime ltOccurenceRemindDate= LocalTime.of(occurenceRemindDate.getHour(), occurenceRemindDate.getMinute());
				
				if(occurenceStartDate.isBefore(LocalDateTime.now())) {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "', '+7 days'), 'T', TIME('" + ltOccurenceStartDate + "')), rappel= CONCAT(DATE('" + ldOccurenceRemindtDate + "', '+7 days'), 'T', TIME('" + ltOccurenceRemindDate + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
				else {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "'), 'T', TIME('" + ltOccurenceStartDate + "')), rappel= CONCAT(DATE('" + ldOccurenceRemindtDate + "'), 'T', TIME('" + ltOccurenceRemindDate + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: AlterDBRecurringTasks/weekly");
			e.printStackTrace();
		}
	}


	public void checkMonthlyTasks(Connection co, Statement st, Statement stUpdate, ResultSet res) {
		try {
			res= st.executeQuery("SELECT id_task, date_debut, rappel from task where is_monthly= 1 AND is_validate_occurence= 1 AND DATETIME(date_debut)< DATETIME('now', 'localtime')");
			while(res.next()) {
				int idTask= res.getInt("id_task");
				
				String[] startDateStrings= res.getString("date_debut").split("-|T|:");
				LocalDateTime ldtStartDate= LocalDateTime.of(Integer.parseInt(startDateStrings[0]), Integer.parseInt(startDateStrings[1]), Integer.parseInt(startDateStrings[2]), Integer.parseInt(startDateStrings[3]), Integer.parseInt(startDateStrings[4]));
				int dayMonthStartDate= Integer.parseInt(startDateStrings[2]);
				LocalTime ltStartDate= LocalTime.of(ldtStartDate.getHour(), ldtStartDate.getMinute());
				LocalDateTime occurenceStartDate= LocalDateTime.of(LocalDate.now().withDayOfMonth(dayMonthStartDate), ltStartDate);
				LocalDate ldOccurenceStartDate= LocalDate.of(occurenceStartDate.getYear(), occurenceStartDate.getMonthValue(), occurenceStartDate.getDayOfMonth()); 
				LocalTime ltOccurenceStartDate= LocalTime.of(occurenceStartDate.getHour(), occurenceStartDate.getMinute()); 

				String[] remindDateStrings= res.getString("rappel").split("-|T|:");
				LocalDateTime ldtRemindDate= LocalDateTime.of(Integer.parseInt(remindDateStrings[0]), Integer.parseInt(remindDateStrings[1]), Integer.parseInt(remindDateStrings[2]), Integer.parseInt(remindDateStrings[3]), Integer.parseInt(remindDateStrings[4]));
				int dayMonthRemindDate= Integer.parseInt(remindDateStrings[2]);
				LocalTime ltRemindDate= LocalTime.of(ldtRemindDate.getHour(), ldtRemindDate.getMinute());
				LocalDateTime occurenceRemindDate= LocalDateTime.of(LocalDate.now().withDayOfMonth(dayMonthRemindDate), ltRemindDate);
				LocalDate ldOccurenceRemindDate= LocalDate.of(occurenceRemindDate.getYear(), occurenceRemindDate.getMonthValue(), occurenceRemindDate.getDayOfMonth()); 
				LocalTime ltOccurenceRemindDate= LocalTime.of(occurenceRemindDate.getHour(), occurenceRemindDate.getMinute()); 

				if(occurenceStartDate.isBefore(LocalDateTime.now())) {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "', '+1 month'), 'T', TIME('" + ltOccurenceStartDate + "')), rappel= CONCAT(DATE('" + ldOccurenceRemindDate + "', '+1 month'), 'T', TIME('" + ltOccurenceRemindDate +"')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
				else {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "'), 'T', TIME('" + ltOccurenceStartDate +"')), rappel= CONCAT(DATE('" + ldOccurenceRemindDate + "'), 'T', TIME('" + ltOccurenceRemindDate + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: AlterDBRecurringTasks/monthly");
			e.printStackTrace();
		}
	}


	public void checkYearlyTasks(Connection co, Statement st, Statement stUpdate, ResultSet res) {
		try {
			res= st.executeQuery("SELECT id_task, date_debut, rappel from task where is_yearly= 1 AND is_validate_occurence= 1 AND DATETIME(date_debut)< DATETIME('now', 'localtime')");
			while(res.next()) {
				int idTask= res.getInt("id_task");
				
				String[] startDateStrings= res.getString("date_debut").split("-|T|:");
				LocalDateTime ldtStartDate= LocalDateTime.of(Integer.parseInt(startDateStrings[0]), Integer.parseInt(startDateStrings[1]), Integer.parseInt(startDateStrings[2]), Integer.parseInt(startDateStrings[3]), Integer.parseInt(startDateStrings[4]));
				LocalDateTime occurenceStartDate= ldtStartDate.withYear(LocalDateTime.now().getYear());
				LocalDate ldOccurenceStartDate= LocalDate.of(occurenceStartDate.getYear(), occurenceStartDate.getMonthValue(), occurenceStartDate.getDayOfMonth()); 
				LocalTime ltOccurenceStartDate= LocalTime.of(occurenceStartDate.getHour(), occurenceStartDate.getMinute()); 

				String[] remindDateStrings= res.getString("rappel").split("-|T|:");
				LocalDateTime ldtRemindDate= LocalDateTime.of(Integer.parseInt(remindDateStrings[0]), Integer.parseInt(remindDateStrings[1]), Integer.parseInt(remindDateStrings[2]), Integer.parseInt(remindDateStrings[3]), Integer.parseInt(remindDateStrings[4]));
				LocalDateTime occurenceRemindDate= ldtRemindDate.withYear(LocalDateTime.now().getYear());
				LocalDate ldOccurenceRemindtDate= LocalDate.of(occurenceRemindDate.getYear(), occurenceRemindDate.getMonthValue(), occurenceRemindDate.getDayOfMonth()); 
				LocalTime ltOccurenceRemindDate= LocalTime.of(occurenceRemindDate.getHour(), occurenceRemindDate.getMinute()); 
				
				if (occurenceStartDate.isBefore(LocalDateTime.now())) {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "', '+1 year'), 'T', TIME('" + ltOccurenceStartDate + "')), rappel= CONCAT(DATE('" + ldOccurenceRemindtDate + "', '+1 year'), 'T', TIME('" + ltOccurenceRemindDate + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
				else {
					stUpdate.executeUpdate("UPDATE task set date_debut= CONCAT(DATE('" + ldOccurenceStartDate + "'), 'T', TIME('" + ltOccurenceStartDate + "')), rappel= CONCAT(DATE('" + ldOccurenceRemindtDate + "'), 'T', TIME('" + ltOccurenceRemindDate + "')), is_reminded= 0, is_started= 0, is_done= 0, is_validate_occurence= 0 WHERE id_task= " + idTask);
				}
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la requette: AlterDBRecurringTasks/yearly");
			e.printStackTrace();
		}
	}

}
