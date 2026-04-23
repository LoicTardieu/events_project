package events.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.sound.SoundClip;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserController {
	
	public boolean manageHomeUserChoice(int choix) {
		boolean isGoodSaisieInt= false;

		//CONNEXION BDD
		Connection co= null;
		Statement st= null;
		try {
			co= DriverManager.getConnection("jdbc:sqlite:C:\\Users\\wotso\\Desktop\\codage\\projets_info\\java\\events\\src\\bdd\\events_db.db");
			st= co.createStatement();
		}catch (SQLException e) {
			System.out.println("Impossible d'établir la connection à la BDD: UserController/manageHomeUserChoice");
			System.out.println(e.getMessage());
		}
		//-----------


		switch (choix) {
		case 1: //Create task
			isGoodSaisieInt= true;
			this.createTask(st);
			break;
		case 2://Create event
			isGoodSaisieInt= true;
			this.createEvent(st);
			break;
		case 3://Modify task
			isGoodSaisieInt= true;
			this.modifyTask(st);
			break;
		case 4://Modify event
			isGoodSaisieInt= true;
			this.modifyEvent(st);
			break;
		case 5://Suppress task
			isGoodSaisieInt= true;
			this.suppressTask(st);
			break;
		case 6://Suppress event
			isGoodSaisieInt= true;
			this.suppressEvent(st);
			break;
		case 7://Terminate a task
			isGoodSaisieInt= true;
			this.terminateTask(st);
			break;
		case 9://Terminate an occurrence of recurring task
			isGoodSaisieInt= true;
			this.terminateOccurrenceOfOccuringTask(st);
			break;
		case 10://Read DB
			isGoodSaisieInt= true;
			this.ReadDB(st);
			break;
		case 11://Refresh
			isGoodSaisieInt= true;
			break;
		case 51://Change notification file
			isGoodSaisieInt= true;
			this.changeNotificationFile();
			break;
		case 100://Exit
			System.out.println("A bientot");
			SoundClip sound= null;
			try {
				File file= new File("assets/exit.wav");
				sound= SoundClip.createSoundClip(file);
				sound.play();
			} catch (IOException e) {
				System.out.println("Problème lors de la création ou lecture du son: UserController/exit");
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				System.out.println("Echec Thread.sleep: UserController/exit");
				e.printStackTrace();
			}
			System.exit(0);
			break;
		default:
			System.out.println("Veuillez saisir une action listée. Votre choix: " + choix);
			isGoodSaisieInt= false;
			break;
		}

		//DECONNEXION BDD
		try {
			co.close();
		} catch (SQLException e) {
			System.out.println("Impossible de fermer la connexion à la BDD");
			e.printStackTrace();
		}
		//-----------

		//		scanner.close();
		return isGoodSaisieInt;
	}


	public void createTask(Statement st) {
		Scanner scanner= new Scanner(System.in);

		boolean isGoodStartDateEntry= false;
		String taskStartDateEntry= "";
		LocalDateTime taskStartDate= null;

		boolean isGoodReminderDateEntry= false;
		String taskReminderDateEntry= "";
		LocalDateTime taskReminderDate= null;
		
		boolean isGoodReccurenceOptionEntry= false;
		int taskReccurenceOptionEntry= 0;
		String reccurenceOption= "";

		System.out.println("Quel est le nom de la tache ?");
		String taskNameEntry= scanner.nextLine();

		do {
			System.out.println("Quelle est la date de début ? (format dd/mm/yyyy:HH:MM)");
			taskStartDateEntry= scanner.nextLine();
			taskStartDate= this.createDate(taskStartDateEntry);
			if (taskStartDate!= null && this.verifyStartDate(taskStartDate)){
				isGoodStartDateEntry= true;
			}
		} while (!isGoodStartDateEntry);


		do {
			System.out.println("Quelle est la date de rappel ? (format dd/mm/yyyy:HH:MM)");
			taskReminderDateEntry= scanner.nextLine();
			taskReminderDate= this.createDate(taskReminderDateEntry);
			if (taskReminderDate!= null && this.verifyReminderDate(taskStartDate, taskReminderDate)) {
				isGoodReminderDateEntry= true;
			}
		} while (!isGoodReminderDateEntry);
		
		do {
			System.out.println("La tâche est elle récurrente ? 0:non, 1:jour, 2:semaine, 3:mois, 4:annee");
			try {
				taskReccurenceOptionEntry= scanner.nextInt();
				scanner.nextLine();
			} catch (InputMismatchException e) {
				System.out.println("Veuillez saisir un nombre");
				scanner.nextLine();
				continue;
			}
			reccurenceOption= this.verifyCreateReccurenceOption(taskReccurenceOptionEntry);
			if(reccurenceOption!= "") {
				isGoodReccurenceOptionEntry= true;
			}
		} while (!isGoodReccurenceOptionEntry);

		this.saveNewTask(st, taskNameEntry, taskStartDate, taskReminderDate, reccurenceOption);
	}


	public void saveNewTask(Statement st, String taskName, LocalDateTime startDate, LocalDateTime reminderDate, String reccurenceOption) {
		try {
			st.executeUpdate("INSERT INTO task (date_debut, nom_task, rappel, is_daily, is_weekly, is_monthly, is_yearly) values ('" + startDate + "', '" + taskName + "', '" + reminderDate + "', " + reccurenceOption + ")");
			System.out.println("La tache à bien été créee");
		}catch (SQLException e) {
			System.out.println("Erreur lors de la création de la tache: UserController/saveNewTask");
			e.printStackTrace();
		}
	}


	public void createEvent(Statement st) {
		Scanner scanner= new Scanner(System.in);

		boolean isGoodStartDateEntry= false;
		String eventStartDateEntry= "";
		LocalDateTime eventStartDate= null;

		boolean isGoodEndDateEntry= false;
		String eventEndDateEntry= "";
		LocalDateTime eventEndDate= null;

		boolean isGoodReminderDateEntry= false;
		String eventReminderDateEntry= "";
		LocalDateTime eventReminderDate= null;

		System.out.println("Quel est le nom de l'événement ?");
		String eventNameEntry= scanner.nextLine();

		do {
			System.out.println("Quelle est la date de début ? (format dd/mm/yyyy:HH:MM)");
			eventStartDateEntry= scanner.nextLine();
			eventStartDate= this.createDate(eventStartDateEntry);
			if (eventStartDate!= null && this.verifyStartDate(eventStartDate)) {
				isGoodStartDateEntry= true;
			}
		} while (!isGoodStartDateEntry);

		do {
			System.out.println("Quelle est la date de fin ? (format dd/mm/yyyy:HH:MM)");
			eventEndDateEntry= scanner.nextLine();
			eventEndDate= this.createDate(eventEndDateEntry);
			if (eventEndDate!= null && this.verifyEndDate(eventStartDate, eventEndDate)) {
				isGoodEndDateEntry= true;
			}
		} while (!isGoodEndDateEntry);


		do {
			System.out.println("Quelle est la date de rappel ? (format dd/mm/yyyy:HH:MM)");
			eventReminderDateEntry= scanner.nextLine();
			eventReminderDate= this.createDate(eventReminderDateEntry);
			if (eventReminderDate!= null && this.verifyReminderDate(eventStartDate, eventReminderDate)) {
				isGoodReminderDateEntry= true;
			}
		} while (!isGoodReminderDateEntry);

		this.saveNewEvent(st, eventNameEntry, eventStartDate, eventEndDate, eventReminderDate);
	}


	public void saveNewEvent(Statement st, String eventName, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime reminderDate) {
		try {
			st.executeUpdate("INSERT INTO event (nom_event, date_debut, date_fin, rappel) values ('" + eventName + "', '" + startDate + "', '" + endDate + "', '" + reminderDate + "')");
			System.out.println("L'événement à bien été créé");
		}catch(SQLException e) {
			System.out.println("Erreur lors de la création de l'événement: UserController/saveNewEvent");
			e.printStackTrace();
		}
	}


	public void modifyTask(Statement st) {
		Scanner scanner= new Scanner(System.in);
		ResultSet res= null;
		int idTask= -1;
		int nbResults= 0;

		this.displayTasks(st);

		System.out.println("Saisissez l'ID de la tache à modifier");
		do {
			try {
				idTask= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}

			try {
				res= st.executeQuery("SELECT count(id_task) as nbResults from task where id_task= " + idTask);
				while (res.next()) {
					nbResults= res.getInt("nbResults");
				}
			} catch (SQLException e) {
				System.out.println("Erreur lors de la recupération de la tache " + idTask + ": UserController/modifyTask");
				idTask= -1;
				continue;
			}

			//SI ON A TROUVER UNE TACHE AVEC L'ID SÉLÉCTIONNÉE	
			if (nbResults== 1) {

				String oldTaskName= "";
				String oldTaskStartDate= "";
				String oldTaskReminderDate= "";
				int isDaily= 0;
				int isWeekly= 0;
				int isMonthly= 0;
				int isYearly= 0;
				
				boolean isGoodReccurenceOptionEntry= false;
				int taskReccurenceOptionEntry= 0;
				String reccuringTask= "";
				String reccurenceOption= "";

				boolean isGoodNewTaskStartDateEntry= false;
				String newTaskStartDateEntry= "";
				LocalDateTime newTaskStartDate= null;

				boolean isGoodNewTaskReminderDateEntry= false;
				String newTaskReminderDateEntry= "";
				LocalDateTime newTaskReminderDate= null;

				try {//On recupère les infos de la tache
					res= st.executeQuery("SELECT * from task where id_task= " + idTask);
					while (res.next()) {
						oldTaskName= res.getString("nom_task");
						oldTaskStartDate= res.getString("date_debut");
						oldTaskReminderDate= res.getString("rappel");
						isDaily= res.getInt("is_daily");
						isWeekly= res.getInt("is_weekly");
						isMonthly= res.getInt("is_monthly");
						isYearly= res.getInt("is_yearly");
						if (isDaily== 1 || isWeekly== 1 || isMonthly== 1 || isYearly== 1) {
							reccuringTask= " Tâche réccurente";
						}
						//On rappelle la tache séléctionnée
						System.out.println(oldTaskName + " debut: " + oldTaskStartDate + " rappel: " + oldTaskReminderDate + reccuringTask);
					}
				} catch (SQLException e) {
					System.out.println("Erreur lors de la recupération des données de la tache " + idTask + ": UserController/modifyTask");
					idTask= -1;
					continue;
				}

				//ASK FOR NEW VALUES
				System.out.println("Quel est le nouveau nom ? (" + oldTaskName + ")");
				String newTaskNameEnty= scanner.nextLine();

				do {
					System.out.println("Quel est la nouvelle date de début ? (format dd/mm/yyyy:HH:MM) (" + oldTaskStartDate + ")");
					newTaskStartDateEntry= scanner.nextLine();
					newTaskStartDate= this.createDate(newTaskStartDateEntry);
					if(newTaskStartDate!= null && this.verifyStartDate(newTaskStartDate)) {
						isGoodNewTaskStartDateEntry= true;
					}
				}while(!isGoodNewTaskStartDateEntry);

				do {
					System.out.println("Quel est la nouvelle date de rappel ? (format dd/mm/yyyy:HH:MM) (" + oldTaskReminderDate + ")");
					newTaskReminderDateEntry= scanner.nextLine();
					newTaskReminderDate= this.createDate(newTaskReminderDateEntry);
					if(newTaskReminderDate!= null && this.verifyReminderDate(newTaskStartDate, newTaskReminderDate)) {
						isGoodNewTaskReminderDateEntry= true;
					}
				} while (!isGoodNewTaskReminderDateEntry);

				do {
					System.out.println("Quel est la réccurence de la tâche ? 0:aucune, 1:jour, 2:semaine, 3:mois, 4:annee");
					try {
						taskReccurenceOptionEntry= scanner.nextInt();
						scanner.nextLine();
					}catch (InputMismatchException e) {
						System.out.println("Veuillez saisir un nombre");
						scanner.nextLine();
						continue;
					}
					reccurenceOption= this.verifyModifyReccurenceOption(taskReccurenceOptionEntry);
					if(reccurenceOption!= "") {
						isGoodReccurenceOptionEntry= true;
					}
				} while (!isGoodReccurenceOptionEntry);
				this.saveTask(st, idTask, newTaskNameEnty, newTaskStartDate, newTaskReminderDate, reccurenceOption);
			}
			else {
				System.out.println("Tache " + idTask + " introuvable ou plusieurs taches avec le même ID: UserController/modifyTask");
				idTask= -1;
			}
		} while (idTask== -1);
	}


	public void saveTask(Statement st, int idTask, String name, LocalDateTime startDate, LocalDateTime ReminderDate, String reccurenceOption) {
		try {
			st.executeUpdate("UPDATE task set nom_task= '" + name + "', date_debut= '" + startDate + "', rappel= '" + ReminderDate + "', is_reminded= 0, is_started= 0, " + reccurenceOption + " where id_task=" + idTask);
			System.out.println("La tache à bien été modifiée");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la mise à jour de la tache " + idTask + ": UserController/saveTask");
			e.printStackTrace();
		}
	}


	public void modifyEvent(Statement st) {
		Scanner scanner= new Scanner(System.in);
		ResultSet res= null;
		int idEvent= -1;
		int nbResults= 0;

		this.displayEvents(st);

		System.out.println("Saisissez l'ID de l'événnement à modifier");
		do {
			try {
				idEvent= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}

			try {
				res= st.executeQuery("SELECT count(id_event) as nbResults from event where id_event= " + idEvent);
				while (res.next()) {
					nbResults= res.getInt("nbResults");
				}
			} catch (SQLException e) {
				System.out.println("Erreur lors de la recupération de l'événement " + idEvent + ": UserController/modifyEvent");
				idEvent= -1;
				continue;
			}

			//SI ON A TROUVER UN ÉVÉNEMENT AVEC L'ID SÉLÉCTIONNÉE	
			if (nbResults== 1) {

				String oldEventName= "";
				String oldEventStartDate= "";
				String oldEventEndDate= "";
				String oldEventReminderDate= "";

				boolean isGoodNewEventStartDateEntry= false;
				String newEventStartDateEntry= "";
				LocalDateTime newEventStartDate= null;

				boolean isGoodNewEventEndDateEntry= false;
				String newEventEndDateEntry= "";
				LocalDateTime newEventEndDate= null;

				boolean isGoodNewEventReminderDateEntry= false;
				String newEventReminderDateEntry= "";
				LocalDateTime newEventReminderDate= null;

				try {//On recupère les infos de l'évenement
					res= st.executeQuery("SELECT * from event where id_event= " + idEvent);
					while (res.next()) {
						oldEventName= res.getString("nom_event");
						oldEventStartDate= res.getString("date_debut");
						oldEventEndDate= res.getString("date_fin");
						oldEventReminderDate= res.getString("rappel");
						//On rappelle la l'événement séléctionné
						System.out.println(oldEventName + " debut: " + oldEventStartDate + " fin: " + oldEventEndDate + " rappel: " + oldEventReminderDate);
					}
				} catch (SQLException e) {
					System.out.println("Erreur lors de la recupération des données de l'événement " + idEvent + ": UserController/modifyEvent");
					idEvent= -1;
					continue;
				}

				//ON DEMMANDE LES NOUVELLES VALEURS
				System.out.println("Quel est le nouveau nom ? (" + oldEventName + ")");
				String newEventNameEnty= scanner.nextLine();

				do {
					System.out.println("Quel est la nouvelle date de début ? (format dd/mm/yyyy:HH:MM) (" + oldEventStartDate + ")");
					newEventStartDateEntry= scanner.nextLine();
					newEventStartDate= this.createDate(newEventStartDateEntry);
					if(newEventStartDate!= null && this.verifyStartDate(newEventStartDate)) {
						isGoodNewEventStartDateEntry= true;
					}
				}while(!isGoodNewEventStartDateEntry);

				do {
					System.out.println("Quel est la nouvelle date de fin ? (format dd/mm/yyyy:HH:MM) (" + oldEventEndDate + ")");
					newEventEndDateEntry= scanner.nextLine();
					newEventEndDate= this.createDate(newEventEndDateEntry);
					if(newEventEndDate!= null && this.verifyEndDate(newEventStartDate, newEventEndDate)) {
						isGoodNewEventEndDateEntry= true;
					}
				}while(!isGoodNewEventEndDateEntry);

				do {
					System.out.println("Quel est la nouvelle date de rappel ? (format dd/mm/yyyy:HH:MM) (" + oldEventReminderDate + ")");
					newEventReminderDateEntry= scanner.nextLine();
					newEventReminderDate= this.createDate(newEventReminderDateEntry);
					if(newEventReminderDate!= null && this.verifyReminderDate(newEventStartDate, newEventReminderDate)) {
						isGoodNewEventReminderDateEntry= true;
					}
				} while (!isGoodNewEventReminderDateEntry);

				this.saveEvent(st, idEvent, newEventNameEnty, newEventStartDate, newEventEndDate, newEventReminderDate);
			}
			else {
				System.out.println("Événement " + idEvent + " introuvable ou plusieurs événements avec le même ID: UserController/modifyEvent");
				idEvent= -1;
			}
		} while (idEvent== -1);
	}


	public void saveEvent(Statement st, int idEvent, String name, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime ReminderDate) {
		try {
			st.executeUpdate("UPDATE event set nom_event= '" + name + "', date_debut= '" + startDate + "', date_fin= '" + endDate + "', rappel= '" + ReminderDate + "', is_reminded= 0, is_started= 0 where id_event=" + idEvent);
			System.out.println("L'événement à bien été modifiée");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la mise à jour de l'événement " + idEvent + ": UserController/saveEvent");
			e.printStackTrace();
		}
	}


	public void suppressTask(Statement st) {
		Scanner scanner= new Scanner(System.in);
		int idTask= -1;

		this.displayTasks(st);

		System.out.println("Saisissez l'ID de la tache à supprimer");
		do {
			try {
				idTask= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}
		}while(idTask== -1);

		try {
			st.executeUpdate("DELETE from task where id_task= " + idTask);
			System.out.println("La tache " + idTask + " à bien été supprimée");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la suppression de la tache " + idTask + ": UserController/suppressTask");
			e.printStackTrace();
		}
	}


	public void suppressEvent(Statement st) {
		Scanner scanner= new Scanner(System.in);
		int idEvent= -1;

		this.displayEvents(st);

		System.out.println("Saisissez l'ID de l'événement à supprimer");
		do {
			try {
				idEvent= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}
		}while(idEvent== -1);

		try {
			st.executeUpdate("DELETE from event where id_event= " + idEvent);
			System.out.println("L'événement " + idEvent + " à bien été supprimée");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la suppression de l'événement " + idEvent + ": UserController/suppressEvent");
			e.printStackTrace();
		}
	}
	
	
	public void terminateTask(Statement st) {
		Scanner scanner= new Scanner(System.in);
		ResultSet res= null;
		int nbResults= 0;
		int idTask= -1;

		this.displayTasks(st, "DATETIME(date_debut)< DATETIME('now', 'localtime') AND is_daily= 0 AND is_weekly= 0 AND is_monthly= 0 AND is_yearly= 0 AND is_done= 0");
		
		System.out.println("Saisissez l'ID de la tache à terminer");
		do {
			try {
				idTask= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}
			
			try {
				res= st.executeQuery("SELECT *, count(id_task) as nbResults from task where id_task= " + idTask);
				while (res.next()) {
					LocalDateTime startDate= LocalDateTime.of(1, 1, 1, 1, 1);
					if(res.getString("date_debut")!= null) {
						int yearStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[0]);
						int monthStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[1]);
						int dayStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[2]);
						int hourStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[3]);
						int minuteStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[4]);
						startDate= LocalDateTime.of(yearStartDate, monthStartDate, dayStartDate, hourStartDate, minuteStartDate);
					}
					
					if(res.getInt("is_daily")== 0 && res.getInt("is_weekly")== 0 && res.getInt("is_monthly")== 0 && res.getInt("is_yearly")== 0 && startDate.isBefore(LocalDateTime.now())) {
						nbResults= res.getInt("nbResults");
					}
					else {
						System.out.println("Vous ne pouvez pas terminer la tâche " + idTask);
						idTask= -1;
						nbResults= -1;
					}
					
				}
			} catch (SQLException e) {
				System.out.println("Erreur lors de la recupération de la tache " + idTask + ": UserController/terminateTask");
				e.printStackTrace();
				idTask= -1;
				continue;
			}
						
			if(nbResults== 1) {
				try {
					st.executeUpdate("UPDATE task set is_done= 1 where id_task= " + idTask);
					System.out.println("La tâche " + idTask + " à bien été términée");
				} catch (SQLException e) {
					System.out.println("Impossible de terminer la tâche " + idTask + ": UserController/terminateTask" );
					idTask= -1;
					continue;
				}
			}
			else if (nbResults== 0) {
				System.out.println("Tâche " + idTask + " introuvable ou plusieurs événements avec le même ID: UserController/terminateTask");
				idTask= -1;
			}
			
		}while(idTask== -1);
	}
	
	
	public void terminateOccurrenceOfOccuringTask(Statement st) {
		Scanner scanner= new Scanner(System.in);
		ResultSet res= null;
		int nbResults= 0;
		int idTask= -1;
		
		this.displayTasks(st, "DATETIME(date_debut)< DATETIME('now', 'localtime') AND (is_daily= 1 OR is_weekly= 1 OR is_monthly= 1 OR is_yearly= 1) AND is_validate_occurence= 0");
		
		System.out.println("Saisissez l'ID de la tache dont il faut terminer l'occurrence");
		do {
			try {
				idTask= scanner.nextInt();
				scanner.nextLine();
			}catch (InputMismatchException e) {
				System.out.println("Veillez saisir un nombre");
				scanner.nextLine();
				continue;
			}
			
			try {
				res= st.executeQuery("SELECT *, count(id_task) as nbResults from task where id_task= " + idTask);
				while (res.next()) {
					LocalDateTime startDate= LocalDateTime.of(1, 1, 1, 1, 1);
					if(res.getString("date_debut")!= null) {
						int yearStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[0]);
						int monthStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[1]);
						int dayStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[2]);
						int hourStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[3]);
						int minuteStartDate= Integer.parseInt(res.getString("date_debut").split("-|T|:")[4]);
						startDate= LocalDateTime.of(yearStartDate, monthStartDate, dayStartDate, hourStartDate, minuteStartDate);
					}
					
					
					if(((res.getInt("is_daily")== 1 || res.getObject("is_daily")== null) || (res.getInt("is_weekly")== 1 || res.getObject("is_weekly")== null) || (res.getInt("is_monthly")== 1 || res.getObject("is_monthly")== null) || (res.getInt("is_yearly")== 1 || res.getObject("is_yearly")== null)) && startDate.isBefore(LocalDateTime.now())) {
						nbResults= res.getInt("nbResults");
					}
					else {
						System.out.println("Vous ne pouvez pas terminer l'occurrence de la tâche " + idTask);
						idTask= -1;
						nbResults= -1;
					}
					
				}
			} catch (SQLException e) {
				System.out.println("Erreur lors de la recupération de la tache " + idTask + ": UserController/terminateTask");
				e.printStackTrace();
				idTask= -1;
				continue;
			}
						
			if(nbResults== 1) {
				try {
					st.executeUpdate("UPDATE task set is_validate_occurence= 1 where id_task= " + idTask);
					System.out.println("L'occurrence de la tâche " + idTask + " à bien été términée");
				} catch (SQLException e) {
					System.out.println("Impossible de terminer l'occurrence de la tâche " + idTask + ": UserController/terminateTask" );
					idTask= -1;
					continue;
				}
			}
			else if (nbResults== 0) {
				System.out.println("Tâche " + idTask + " introuvable ou plusieurs événements avec le même ID: UserController/terminateTask");
				idTask= -1;
			}
			
		}while(idTask== -1);
	}


	public void ReadDB(Statement st) {
		System.out.println("Voici les taches à venir");
		this.displayTasks(st);
		System.out.println("-----------------------------------");
		System.out.println("Voici les événements à venir");
		this.displayEvents(st);
	}


	public void displayTasks(Statement st) {
		ResultSet res= null;
		int idTask= -1;
		try {
			res= st.executeQuery("SELECT * from task");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la séléction des taches: UserController/displayTasks");
		}
		
		try {
			String taskName;
			String taskStartDate;
			String taskReminderDate;
			String reccurringTask;
			while (res.next()) {
				idTask= res.getInt("id_task");
				taskName= res.getString("nom_task");
				taskStartDate= res.getString("date_debut");
				taskReminderDate= res.getString("rappel");
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringTask= " Réccurente";
				}
				else {
					reccurringTask= "";
				}
				System.out.println(idTask + "- " + taskName + " " + taskStartDate + " -- " + taskReminderDate + reccurringTask);
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la recupération des données de la tache " + idTask + ": UserController/displayTasks");
			e.printStackTrace();
		}
	}

	
	public void displayTasks(Statement st, String whereConditionSQL) {
		ResultSet res= null;
		int idTask= -1;
		try {
			res= st.executeQuery("SELECT * from task where " + whereConditionSQL + " ORDER BY date_debut");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la séléction des taches à terminer: UserController/displayTasks(whereConditionSQL)");
		}

		try {
			String taskName;
			String taskStartDate;
			String taskReminderDate;
			String reccurringTask;
			while (res.next()) {
				idTask= res.getInt("id_task");
				taskName= res.getString("nom_task");
				taskStartDate= res.getString("date_debut");
				taskReminderDate= res.getString("rappel");
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringTask= " Réccurente";
				}
				else {
					reccurringTask= "";
				}
				System.out.println(idTask + "- " + taskName + " " + taskStartDate + " -- " + taskReminderDate + reccurringTask);
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la recupération des données de la tache " + idTask + ": UserController/displayTasks(whereConditionSQL)");
			e.printStackTrace();
		}
	}


	public void displayEvents(Statement st) {
		ResultSet res= null;
		int idEvent= -1;
		try {
			res= st.executeQuery("SELECT * from event");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la séléction des événements");
		}

		try {
			String eventName;
			String eventStartDate;
			String eventEndDate;
			String eventReminderDate;
			String reccurringEvent;
			while (res.next()) {
				idEvent= res.getInt("id_event");
				eventName= res.getString("nom_event");
				eventStartDate= res.getString("date_debut");
				eventEndDate= res.getString("date_fin");
				eventReminderDate= res.getString("rappel");
				if(res.getInt("is_daily")== 1 || res.getInt("is_weekly")== 1 || res.getInt("is_monthly")== 1 || res.getInt("is_yearly")== 1) {
					reccurringEvent= " Réccurent";
				}
				else {
					reccurringEvent= "";
				}
				System.out.println(idEvent + "- " + eventName + " [" + eventStartDate + " => " + eventEndDate + "] " + eventReminderDate + reccurringEvent);
			}
		} catch (SQLException e) {
			System.out.println("Erreur lors de la recupération des données de l'événement " + idEvent + ": UserController/displayEvents");
			e.printStackTrace();
		}
	}
	
	
	public String verifyCreateReccurenceOption(int option) {
		String stringInsertReccurence= "";
		switch (option) {
		case 0: 
			stringInsertReccurence= "0, 0, 0, 0";
			break;
		case 1: 
			stringInsertReccurence= "1, 0, 0, 0";
			break;
		case 2: 
			stringInsertReccurence= "0, 1, 0, 0";
			break;
		case 3: 
			stringInsertReccurence= "0, 0, 1, 0";
			break;
		case 4: 
			stringInsertReccurence= "0, 0, 0, 1";
			break;
		default:
			stringInsertReccurence= "";
			System.out.println("Veuillez saisir une option valide");
		}
		return stringInsertReccurence;
	}

	
	public String verifyModifyReccurenceOption(int option) {
		String stringInsertReccurence= "";
		switch (option) {
		case 0: 
			stringInsertReccurence= "is_daily= 0, is_weekly= 0, is_monthly= 0, is_yearly= 0";
			break;
		case 1: 
			stringInsertReccurence= "is_daily= 1, is_weekly= 0, is_monthly= 0, is_yearly= 0";
			break;
		case 2: 
			stringInsertReccurence= "is_daily= 0, is_weekly= 1, is_monthly= 0, is_yearly= 0";
			break;
		case 3: 
			stringInsertReccurence= "is_daily= 0, is_weekly= 0, is_monthly= 1, is_yearly= 0";
			break;
		case 4: 
			stringInsertReccurence= "is_daily= 0, is_weekly= 0, is_monthly= 0, is_yearly= 1";
			break;
		default:
			stringInsertReccurence= "";
			System.out.println("Veuillez saisir une option valide");
		}
		return stringInsertReccurence;
	}
	
	
	public void changeNotificationFile() {
		Scanner scanner= new Scanner(System.in);
		int nbNotificationChange= 0;
		File fileNotification;
		
		System.out.println("Quelle notification voulez vous modifier ?");
		System.out.println("1: Notification de la liste permanente");
		System.out.println("2: Notification de rappel");
		System.out.println("3: Notification de maintenant");
		System.out.println("4: Notification de sortie d'application");
		
		try {
			nbNotificationChange= scanner.nextInt();
			
		}catch (InputMismatchException e) {
			System.out.println("Veuillez saisir un nombre");
			e.printStackTrace();
		}
		
		fileNotification= this.selectNotificationFile();
		if (fileNotification!= null) {
			switch (nbNotificationChange) {
			case 1:
				try {
					Files.copy(Paths.get(fileNotification.getPath()), Paths.get("assets/notification_default.wav"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.out.println("Impossible de changer le fichier de notification: UserController/changeNotificationFile");
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					Files.copy(Paths.get(fileNotification.getPath()), Paths.get("assets/notification.wav"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.out.println("Impossible de changer le fichier de notification: UserController/changeNotificationFile");
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					Files.copy(Paths.get(fileNotification.getPath()), Paths.get("assets/notification_now.wav"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.out.println("Impossible de changer le fichier de notification: UserController/changeNotificationFile");
					e.printStackTrace();
				}
				break;
			case 4:
				try {
					Files.copy(Paths.get(fileNotification.getPath()), Paths.get("assets/exit.wav"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.out.println("Impossible de changer le fichier de notification: UserController/changeNotificationFile");
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			
		}
	}
	
	
	public File selectNotificationFile() {
		File file= null;
		
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV Files", "wav");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(chooser);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
	       if(filter.accept(chooser.getSelectedFile())) {
	    	   file= chooser.getSelectedFile();
	       }
	       else {
			System.out.println("Séléction non autorisé");
	       }
	    }
	    
		return file;
	}


	public LocalDateTime createDate(String dateString) {
		LocalDateTime taskStartDate= null;
		String[]  tabDateDebutTache= dateString.split("/|:");

		try {
			taskStartDate= LocalDateTime.of(Integer.parseInt(tabDateDebutTache[2]), Integer.parseInt(tabDateDebutTache[1]), Integer.parseInt(tabDateDebutTache[0]), Integer.parseInt(tabDateDebutTache[3]), Integer.parseInt(tabDateDebutTache[4]));
			System.out.println(taskStartDate);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException | DateTimeException e) {
			System.out.println("La date fournie n'est pas valide. Respectez le format dd/mm/yyyy:HH:MM");
		}
		return taskStartDate;
	}


	public boolean verifyStartDate(LocalDateTime stratDate) {
		if (stratDate.isAfter(LocalDateTime.now())) {
			return true; 
		}
		else {
			System.out.println("La date fournie est déjà passée");
			return false;
		}
	}


	public boolean verifyEndDate(LocalDateTime startDate, LocalDateTime endDate) {
		if (endDate.isAfter(startDate)) {
			return true;
		}else {
			System.out.println("La date fournie est avant la date de début (date de debut: " + startDate + ")");
			return false;
		}
	}


	public boolean verifyReminderDate(LocalDateTime startDate, LocalDateTime reminderDate) {
		if (reminderDate.isBefore(startDate) && reminderDate.isAfter(LocalDateTime.now())) {
			return true;
		}
		else {
			System.out.println("La date fournie est après la date de début ou déjà passée (date de debut: " + startDate + ")");
			return false;
		}
	}

}
