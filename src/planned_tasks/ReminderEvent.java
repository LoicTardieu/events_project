package planned_tasks;

import javax.sound.SoundClip;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.TimerTask;


public class ReminderEvent extends TimerTask{

	@Override
	public void run() {
		Connection co= null;
		Statement st= null;
		Statement stUpdate= null;

		SoundClip sound= null;
		try {
			co= DriverManager.getConnection("jdbc:sqlite:C:\\Users\\wotso\\Desktop\\codage\\projets_info\\java\\events\\src\\bdd\\events_db.db");
			st= co.createStatement();
			stUpdate= co.createStatement();
		}catch (SQLException e) {
			System.out.println("Impossible d'établir la connection à la BDD: ReminderEvent");
			System.out.println(e.getMessage());
		}

		//MAKE THE REMIND
		try {
			ResultSet res= null;
			res= st.executeQuery("SELECT * from event where DATETIME(rappel) < DATETIME('now', 'localtime') AND is_reminded= 0");
			while (res.next()) {
				int eventId= res.getInt("id_event");
				String eventName= res.getString("nom_event");
				String eventStartDate= res.getString("date_debut");
				String eventEndDate= res.getString("date_fin");
				String reccurringEvent= "";
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringEvent= " Événement réccurent";
				}
				System.out.println("RAPPEL: " + eventName + " [" + eventStartDate + " => " + eventEndDate + "]" + reccurringEvent);
				
				//PLAY THE SOUND NOTIFICATION
				try {
					File file= new File("assets/notification.wav");
					sound= SoundClip.createSoundClip(file);
					sound.play();
				} catch (IOException e) {
					System.out.println("Problème lors de la création ou lecture du son: ReminderEvent/rappel événement");
					e.printStackTrace();
				}
				
				stUpdate.executeUpdate("UPDATE event set is_reminded= 1 where id_event= " + eventId);
			}
		} catch (Exception e) {
			System.out.println("Erreur lors de la requette: ReminderEvent/rappel événement");
			e.printStackTrace();
		}
		
		
		//NOTIFY THE EVENT HAVE TO START
		try {
			ResultSet res= null;
			res= st.executeQuery("SELECT * from event where DATETIME(date_debut) < DATETIME('now', 'localtime') AND is_started= 0");
			while (res.next()) {
				int eventId= res.getInt("id_event");
				String eventName= res.getString("nom_event");
				String eventStartDate= res.getString("date_debut");
				String eventEndDate= res.getString("date_fin");
				String[] slicedEventEndDate= eventEndDate.split("-|T|:");
				String reccurringEvent= "";
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringEvent= " Événement réccurent";
				}
				if(LocalDateTime.of(Integer.parseInt(slicedEventEndDate[0]), Integer.parseInt(slicedEventEndDate[1]), Integer.parseInt(slicedEventEndDate[2]), Integer.parseInt(slicedEventEndDate[3]), Integer.parseInt(slicedEventEndDate[4])).isBefore(LocalDateTime.now())) {
					System.out.println("PASSÉ: " + eventName + " [" + eventStartDate + "=>" + eventEndDate + "]" + reccurringEvent);
				}
				else {
					System.out.println("MAINTENANT: " + eventName + " [" + eventStartDate + "=>" + eventEndDate + "]" + reccurringEvent);
				}
				
				
				//PLAY THE SOUND NOTIFICATION
				try {
					File file= new File("assets/notification_now.wav");
					sound= SoundClip.createSoundClip(file);
					sound.play();
				} catch (IOException e) {
					System.out.println("Problème lors de la création ou lecture du son: ReminderEvent/début événement");
					e.printStackTrace();
				}
				
				stUpdate.executeUpdate("UPDATE event set is_started= 1 where id_event= " + eventId);
			}
		} catch (Exception e) {
			System.out.println("Erreur lors de la requette: ReminderEvent/début événement");
			e.printStackTrace();
		}
		

		try {
			co.close();
		} catch (SQLException e) {
			System.out.println("Impossible de fermer la connexion à la BDD: : ReminderEvent");
			e.printStackTrace();
		}

	}

}
