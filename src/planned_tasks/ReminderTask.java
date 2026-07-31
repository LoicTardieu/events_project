package planned_tasks;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;

import javax.sound.SoundClip;

public class ReminderTask extends TimerTask {

	@Override
	public void run() {
		Connection co= null;
		Statement st= null;
		Statement stUpdate= null;

		SoundClip sound= null;
		try {
			String projectPath = System.getProperty("user.dir");
			co= DriverManager.getConnection("jdbc:sqlite:" + projectPath + "\\src\\bdd\\events_db.db");
			st= co.createStatement();
			stUpdate= co.createStatement();
		}catch (SQLException e) {
			System.out.println("Impossible d'établir la connection à la BDD: ReminderTask");
			System.out.println(e.getMessage());
		}

		//MAKE THE REMIND
		try {
			ResultSet res= null;
			res= st.executeQuery("SELECT * from task where DATETIME(rappel) < DATETIME('now', 'localtime') AND is_reminded= 0");
			while (res.next()) {
				int taskId= res.getInt("id_task");
				String taskName= res.getString("nom_task");
				String taskStartDate= res.getString("date_debut");
				String reccurringTask= "";
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringTask= " Tâche réccurente";
				}
				System.out.println("RAPPEL: " + taskName + " [" + taskStartDate + "]" + reccurringTask);

				//PLAY THE SOUND NOTIFICATION
				try {
					File file= new File("assets/notification.wav");
					sound= SoundClip.createSoundClip(file);
					sound.play();
				} catch (IOException e) {
					System.out.println("Problème lors de la création ou lecture du son: ReminderTask/rappel tache");
					e.printStackTrace();
				}

				stUpdate.executeUpdate("UPDATE task set is_reminded= 1 where id_task= " + taskId);
			}
		} catch (Exception e) {
			System.out.println("Erreur lors de la requette: ReminderTask/rappel tache");
			e.printStackTrace();
		}


		//NOTIFY THE EVENT HAVE TO START
		try {
			ResultSet res= null;
			res= st.executeQuery("SELECT * from task where DATETIME(date_debut) < DATETIME('now', 'localtime') AND is_started= 0");
			while (res.next()) {
				int taskId= res.getInt("id_task");
				String taskName= res.getString("nom_task");
				String taskStartDate= res.getString("date_debut");
				String reccurringTask= "";
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringTask= " Tâche réccurente";
				}
				System.out.println("MAINTENANT: " + taskName + " [" + taskStartDate + "]" + reccurringTask);

				//PLAY THE SOUND NOTIFICATION
				try {
					File file= new File("assets/notification_now.wav");
					sound= SoundClip.createSoundClip(file);
					sound.play();
				} catch (IOException e) {
					System.out.println("Problème lors de la création ou lecture du son: ReminderTask/début tache");
					e.printStackTrace();
				}

				stUpdate.executeUpdate("UPDATE task set is_started= 1 where id_task= " + taskId);
			}
		} catch (Exception e) {
			System.out.println("Erreur lors de la requette: ReminderTask/début tache");
			e.printStackTrace();
		}

		
		try {
			co.close();
		} catch (SQLException e) {
			System.out.println("Impossible de fermer la connexion à la BDD: ReminderTask");
			e.printStackTrace();
		}
	}

}
