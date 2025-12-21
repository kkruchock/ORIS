package com.dmitry.project.consoleChat.dekstopChat.common;

//все протоколы, может быть многовато...
public class SimpleProtocol {

    public static final String AUTH = "AUTH";
    public static final String CREATE_ROOM = "CREATE";
    public static final String JOIN_ROOM = "JOIN";
    public static final String LEAVE_ROOM = "LEAVE";
    public static final String SEND_MSG = "MSG";
    public static final String GET_ROOMS = "GET_ROOMS";

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String ROOM_LIST = "ROOMS";
    public static final String CHAT_MSG = "CHAT";
    public static final String SYSTEM_MSG = "SYSTEM";

    public static final String DELIMITER = "|";

    public static String createAuth(String username, String color) {
        return AUTH + DELIMITER + username + DELIMITER + color;
    }

    public static String createCreateRoom(String roomName) {
        return CREATE_ROOM + DELIMITER + roomName;
    }

    public static String createJoinRoom(String roomId) {
        return JOIN_ROOM + DELIMITER + roomId;
    }

    public static String createLeaveRoom() {
        return LEAVE_ROOM;
    }

    public static String createSendMessage(String text) {
        return SEND_MSG + DELIMITER + escape(text);
    }

    public static String createGetRooms() {
        return GET_ROOMS;
    }

    public static String createError(String message) {
        return ERROR + DELIMITER + escape(message);
    }

    public static String createRoomList(String roomsData) {
        return ROOM_LIST + DELIMITER + roomsData;
    }

    public static String createChatMessage(String sender, String color, String text) {
        return CHAT_MSG + DELIMITER + escape(sender) + DELIMITER + escape(color) + DELIMITER + escape(text);
    }

    public static String createSystemMessage(String text) {
        return SYSTEM_MSG + DELIMITER + escape(text);
    }

    public static String[] parseCommand(String command) {
        return command.split("\\" + DELIMITER, -1);
    }

    //добавить экранирование
    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n");
    }

    //убрать экранирование
    public static String unescape(String text) {
        if (text == null) return "";
        return text.replace("\\n", "\n")
                .replace("\\|", "|")
                .replace("\\\\", "\\");
    }
}