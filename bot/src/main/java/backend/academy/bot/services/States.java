package backend.academy.bot.services;

import java.util.NoSuchElementException;

public enum States {
    START("/start"),
    HELP("/help"),
    LIST("/list"),
    TRACK("/track"),
    UNTRACK("/untrack"),
    ENTER_TAGS(""),
    ENTER_FILTERS(""),
    DEFAULT("");

    private final String command;

    States(String value) {
        this.command = value;
    }

    public static States findByCommand(String command) {
        for (States state : values()) {
            if (state.command.equals(command)) {
                return state;
            }
        }
        throw new NoSuchElementException();
    }
}
