package com.example.demo.hello;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloCommand {
    @ShellMethod(key = "hello", value = "I will say hello")
    public String hello(@ShellOption(value = "-n", defaultValue = "World") String arg) {
        return "Hello, " + arg + "!";
    }

    @ShellMethod(key = "goodbye", value = "I will say goodbye")
    public String goodbye() {
        return "Goodbye!";
    }
}
