package raf.sk_schedule.controller;

import raf.sk_schedule.api.ScheduleManager;
import raf.sk_schedule.exception.ScheduleException;
import raf.sk_schedule.model.RoomProperties;
import raf.sk_schedule.model.ScheduleSlot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;


public class ScheduleController {
    ScheduleManager scheduleManager;

    Scanner inputScanner;

    final String help;

    final String helpFilePath = "src/main/java/raf/sk_schedule/toolkit/help.txt";

    public ScheduleController(ScheduleManager scheduleManager) {

        this.inputScanner = new Scanner(System.in);
        this.scheduleManager = scheduleManager;

        try {
            help = Files.readString(Path.of(helpFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Help document not found!");
        }

    }

    public boolean actionScheduler() throws Exception {
        String line = inputScanner.nextLine().trim();


        if ("schedule_close".equalsIgnoreCase(line)) {
            System.out.println("Closing the program.");
            return false;
        }
        if ("--help".equalsIgnoreCase(line) || "-h".equalsIgnoreCase(line)) {
            System.out.println(help);
            return true;
        }
        if ("list_schedule".equalsIgnoreCase(line)) {
            getWholeSchedule();
            return true;
        }
        if ("schedule_slot".equalsIgnoreCase(line)) {
            scheduleTimeSlot();
            return true;
        }
        if ("move_slot".equalsIgnoreCase(line)) {

            return true;
        }
        if ("remove_slot".equalsIgnoreCase(line)) {

            return true;
        }

        if ("add_room".equalsIgnoreCase(line)) {
            addRoom();
            return true;
        }
        if ("update_room".equalsIgnoreCase(line)) {

            return true;
        }
        if ("remove_room".equalsIgnoreCase(line)) {

            return true;

        }
        if ("list_rooms".equalsIgnoreCase(line)) {
            getAllRooms();
            return true;
        }
        return false;
    }


    private void scheduleTimeSlot() throws Exception {

        ScheduleSlot.Builder slotBuilder = new ScheduleSlot.Builder();

        System.out.println("Enter date in format \"yyyy-mm-dd\" (\"2023-10-23\") or day of the week on witch the slot will be scheduled on : ");
        String scheduleOn = inputScanner.nextLine().trim();

        if (!scheduleOn.matches("\\d{4}-\\d{2}-\\d{2}")) {

            slotBuilder.setAttribute("weekly", scheduleOn);

            System.out.println("Enter Enter date in format \"yyyy-mm-dd\" (\"2023-10-23\"), starting from witch slot will be scheduled on every " + scheduleOn.toLowerCase() + " :");
            String startingDate = inputScanner.nextLine().trim();
            slotBuilder.setAttribute("startingDate", startingDate);

            System.out.println("Enter Enter date in format \"yyyy-mm-dd\" (\"2023-10-23\"), slot will be scheduled on every "
                    + scheduleOn.toLowerCase() + " starting from " + startingDate + " until date: ");
            String endingDate = inputScanner.nextLine().trim();
            slotBuilder.setAttribute("endingDate", endingDate);

            System.out.println("Slot will be scheduled every " + scheduleOn.toLowerCase() + " starting from date " + startingDate + " until date " + endingDate + ".");

            System.out.println("Enter starting time in format \"hours:minutes\" (\"12:25\") : ");
            String startingTime = inputScanner.nextLine().trim();
            slotBuilder.setAttribute("startTime", startingTime);

            System.out.println("Enter a duration in minutes (whole number), or ending time in format \"hours:minutes\" (\"12:25\") :");
            String endTime = inputScanner.nextLine().trim();

            if (endTime.matches("\\d{2}:\\d{2}"))
                slotBuilder.setAttribute("endTime", endTime);
            else
                slotBuilder.setDuration(Long.parseLong(endTime));

        } else {

            System.out.println("Enter starting time in format \"hours:minutes\" (\"12:25\") : ");
            String startingTime = inputScanner.nextLine().trim();
            slotBuilder.setStart(scheduleOn + " " + startingTime);


            System.out.println("Enter a duration in minutes (whole number), or ending time in format \"hours:minutes\" (\"12:25\") :");
            String endTime = inputScanner.nextLine().trim();
            if (endTime.matches("\\d{2}:\\d{2}"))
                slotBuilder.setEnd(scheduleOn + " " + endTime);
            else
                slotBuilder.setDuration(Long.parseLong(endTime));
        }

        System.out.println("Enter name of the room where you want to schedule the time slot: ");
        RoomProperties room = null;
        while (true) {
            room = scheduleManager.getRoom(inputScanner.nextLine().trim());
            if (room != null)
                break;

            System.out.println("There is no room with that name");
        }
        slotBuilder.setLocation(room);

        while (true) {
            System.out.println("Enter additional attributes name or type in \"done\" if there is no additional attributes associated with this slot: ");
            String line = inputScanner.nextLine().trim();

            if (line.isEmpty())
                continue;

            if (line.equalsIgnoreCase("done"))
                break;

            System.out.println("Enter value for attribute named \"" + line + "\" :");

            slotBuilder.setAttribute(line, inputScanner.nextLine().trim());
        }

        // ************** adding the slot to schedule ************** //
        ScheduleSlot slot = slotBuilder.build();
        scheduleManager.scheduleTimeSlot(slot);
        // ********************************************************* //

        if (slot.hasAttribute("weekly"))
            System.out.println("The new slot is has been scheduled on every " + slot.getAttribute("weekly")
                    + " starts at: " + slot.getAttribute("startTime")
                    + (slot.hasAttribute("endTime") ?
                    " and ends at: " + slot.getAttribute("endTime") : " with a duration of " + slot.getDuration() + " minutes ")
                    + " starting from date: " + slot.getAttribute("start")
                    + " until date: " + slot.getAttribute("until"));
        else
            System.out.println("The new slot has been scheduled on date: " + slot.getStartAsString().split(" ")[0]
                    + "starts at: " + slot.getStartAsString().split(" ")[1] + ", has duration of " + slot.getDuration() + " minutes, "
                    + " and ends at: " + slot.getEndAsString().split(" ")[1] + ".");


    }

    private void addRoom() {

        RoomProperties.Builder roomBuilder = new RoomProperties.Builder();

        System.out.println("Enter the room name: ");

        roomBuilder.setName(inputScanner.nextLine().trim());

        System.out.println("Enter room capacity that is positive whole number: ");

        roomBuilder.setCapacity(Integer.parseInt(inputScanner.nextLine()));

        System.out.println("Does this room have computers? Type \"y\" if yes or \"n\" if no.");

        roomBuilder.setHasComputers(inputScanner.nextLine().trim().equalsIgnoreCase("y"));

        System.out.println("Does this room have a projector? Type \"y\" if yes or \"n\" if no.");

        roomBuilder.setHasProjector(inputScanner.nextLine().trim().equalsIgnoreCase("y"));

        while (true) {
            System.out.println("Enter additional attributes name or type in \"done\" if there is no additional attributes associated with this room: ");
            String line = inputScanner.nextLine().trim();

            if (line.isEmpty())
                continue;

            if (line.equalsIgnoreCase("done"))
                break;

            System.out.println("Enter value for attribute named \"" + line + "\" :");

            roomBuilder.addExtra(line, inputScanner.nextLine().trim());
        }

        RoomProperties newRoom = roomBuilder.build();
        scheduleManager.addRoom(newRoom);

        System.out.println("Room " + newRoom.getName() + " has been created!");
    }


    private void removeRoom() {

    }

    private void updateRoom() {



    }

    private void getWholeSchedule() {

        List<ScheduleSlot> schedule = scheduleManager.getWholeSchedule();
        if (schedule.isEmpty())
            System.out.println("Your schedule is empty.");
        for (ScheduleSlot curr : schedule) {
            System.out.println(curr);
        }
    }

    private void getAllRooms() {
        List<RoomProperties> rooms = scheduleManager.getAllRooms();
        if (rooms.isEmpty())
            System.out.println("There is no rooms in schedule.");
        for (RoomProperties curr : rooms) {
            System.out.println(curr);
        }
    }
}
