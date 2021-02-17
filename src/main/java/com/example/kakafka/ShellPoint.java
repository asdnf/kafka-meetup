package com.example.kakafka;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShellPoint {

    @ShellMethod
    public String mmm(@ShellOption String text) {
        return text;
    }
}
