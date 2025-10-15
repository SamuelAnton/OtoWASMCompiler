package Syntaxer.ast;

import java.util.ArrayList;

public class ClassDecl {
    private String name;
    private String extensionName; // Need to replace with pointer to real class after parsing

    public ClassDecl(String name, String extension, ArrayList<ClassMember> members) {
        this.name = name;
        this.extensionName = extension;
        // Split the members to fields, methods and constructors lists
    }

    public String getName() {
        return name;
    }
}