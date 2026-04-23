package events;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Timer;

import javax.sound.SoundClip;

import events.controllers.EventController;
import events.controllers.TaskController;
import events.controllers.UserController;
import planned_tasks.ReminderTask;
import planned_tasks.AlterDBRecurringTasks;
import planned_tasks.ReminderEvent;

public class Main {


	public static void main(String[] args) {

		System.out.println("BIENVENUE SUR EVENTS !");
		System.out.println();

		UserController uc= new UserController();
		TaskController tc= new TaskController();
		EventController ec= new EventController();

		Timer timer= new Timer();
		timer.schedule(new ReminderTask(), 2000, 20000);
		timer.schedule(new ReminderEvent(), 2000, 20000);
		
		timer.schedule(new AlterDBRecurringTasks(), 3000, 5000);

		while (true){
			displayHome(tc, ec);

			int choixActionHome= -1;
			do {
				Scanner sc= new Scanner(System.in);
				try {
					choixActionHome= sc.nextInt();
					sc.nextLine();
				} catch (InputMismatchException e) {
					System.out.println("Veillez saisir un nombre");
				}
			} while (!uc.manageHomeUserChoice(choixActionHome));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Echec Thread.sleep: Main");
				e.printStackTrace();
			}
		}


	}


	public static void displayHome(TaskController tc, EventController ec) {
		System.out.println("Que souhaitez vous faire ?");
		
		System.out.println("====TÂCHES====");
		System.out.println("1- Creer une nouvelle tache");
		System.out.println("3- Modifier une tache");
		System.out.println("5- Supprimer une tache");
		System.out.println("7- Terminer une tâche");
		System.out.println("9- Terminer une occurence de tâche réccurente");
		
		System.out.println();
		
		System.out.println("====ÉVÉNEMENTS====");
		System.out.println("2- Creer un nouvel événement");
		System.out.println("4- Modifier un événement");
		System.out.println("6- Supprimer un événement");
		
		System.out.println();
		
		System.out.println("====UTILISATION====");
		System.out.println("10- Voir les taches et événements à venir");
		System.out.println("11- Rafraichir");
		System.out.println("100- Quitter");
		
		System.out.println();
		
		System.out.println("====PARAMETRES====");
		System.out.println("51- Changer fichier de notification");
		
		System.out.println();

		displayPermanentsReminds(tc, ec);
	}


	public static void displayPermanentsReminds(TaskController tc, EventController ec) {

		//CONNEXION BDD
		Connection co= null;
		Statement st= null;
		try {
			co= DriverManager.getConnection("jdbc:sqlite:C:\\Users\\wotso\\Desktop\\codage\\projets_info\\java\\events\\src\\bdd\\events_db.db");
		}catch (SQLException e) {
			System.out.println("Impossible d'établir la connection à la BDD: Main/liste parmanente");
			System.out.println(e.getMessage());
		}

		ResultSet[] ressTask= tc.checkPermanentReminds(co);
		ResultSet[] ressEvent= ec.checkPermanentReminds(co);

		//PLAY THE SOUND NOTIFICATION IF THERE IS SOMETHING IN THE LIST
		if(ressTask[0]!= null || ressTask[1]!= null || ressEvent[0]!= null || ressEvent[1]!= null) {
			SoundClip sound= null;
			try {
				File file= new File("assets/notification_default.wav");
				sound= SoundClip.createSoundClip(file);
				sound.play();
			} catch (IOException e) {
				System.out.println("Problème lors de la création ou lecture du son: Main/liste permanente");
				e.printStackTrace();
			}
		}

		if (ressTask[0]!= null) {
			try {
				while(ressTask[0].next()) {
					String taskName= ressTask[0].getString("nom_task");
					String taskStartDate= ressTask[0].getString("date_debut");
					String reccurringTask= "";
					if(ressTask[0].getInt("is_daily")== 1 || ressTask[0].getInt("is_weekly")== 1 || ressTask[0].getInt("is_monthly")== 1 || ressTask[0].getInt("is_yearly")== 1) {
						reccurringTask= " Tâche réccurente";
					}
					System.out.println("++RAPPEL: " + taskName + "[" + taskStartDate + "]" + reccurringTask + "++");
				}
			} catch (SQLException e) {
				System.out.println("Impossible d'afficher les rappels des taches: Main/liste permanente");
				e.printStackTrace();
			}
		}

		if (ressTask[1]!= null) {
			try {
				while(ressTask[1].next()) {
					String taskName= ressTask[1].getString("nom_task");
					String taskStartDate= ressTask[1].getString("date_debut");
					String reccurringTask= "";
					if(ressTask[1].getInt("is_daily")== 1 || ressTask[1].getInt("is_weekly")== 1 || ressTask[1].getInt("is_monthly")== 1 || ressTask[1].getInt("is_yearly")== 1) {
						reccurringTask= " Tâche réccurente";
					}
					System.out.println("++MAINTENANT: " + taskName + "[" + taskStartDate + "]" + reccurringTask + "++");
				}
			} catch (SQLException e) {
				System.out.println("Impossible d'afficher les taches en cours: Main/liste permanente");
				e.printStackTrace();
			}
		}

		if (ressEvent[0]!= null) {
			try {
				while(ressEvent[0].next()) {
					String taskName= ressEvent[0].getString("nom_event");
					String taskStartDate= ressEvent[0].getString("date_debut");
					String taskEndDate= ressEvent[0].getString("date_fin");
					String reccurringEvent= "";
					if(ressEvent[0].getInt("is_daily")== 1 || ressEvent[0].getInt("is_weekly")== 1 || ressEvent[0].getInt("is_monthly")== 1 || ressEvent[0].getInt("is_yearly")== 1) {
						reccurringEvent= " Événement réccurent";
					}
					System.out.println("++RAPPEL: " + taskName + "[" + taskStartDate + " => " + taskEndDate + "]" + reccurringEvent + "++");
				}
			} catch (SQLException e) {
				System.out.println("Impossible d'afficher les rappels des événements: Main/liste permanente");
				e.printStackTrace();
			}
		}

		if (ressEvent[1]!= null) {
			try {
				while(ressEvent[1].next()) {
					String taskName= ressEvent[1].getString("nom_event");
					String taskStartDate= ressEvent[1].getString("date_debut");
					String taskEndDate= ressEvent[1].getString("date_fin");
					String reccurringEvent= "";
					if(ressEvent[1].getInt("is_daily")== 1 || ressEvent[1].getInt("is_weekly")== 1 || ressEvent[1].getInt("is_monthly")== 1 || ressEvent[1].getInt("is_yearly")== 1) {
						reccurringEvent= " Événement réccurent";
					}
					System.out.println("++MAINTENANT: " + taskName + "[" + taskStartDate + " => " + taskEndDate + "]" + reccurringEvent + "++");
				}
			} catch (SQLException e) {
				System.out.println("Impossible d'afficher les événements en cours: Main/liste permanente");
				e.printStackTrace();
			}
		}

		//DECONNEXION BDD
		try {
			co.close();
		} catch (SQLException e) {
			System.out.println("Impossible de fermer la connexion à la BDD: Main/liste permanente");
			e.printStackTrace();
		}
		//-----------
	}


}