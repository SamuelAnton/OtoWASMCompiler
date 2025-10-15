package Syntaxer.ast;

import java.util.ArrayList;


public class Program {
    private ArrayList<ClassDecl> classes;

    public Program(ArrayList<ClassDecl> classes) {
        this.classes = classes;
    }

    public ArrayList<ClassDecl> getClasses() {
        return classes;
    }
}