package b00080902.mabs2;

import java.io.Serializable;

public class User implements Serializable{

    private String id;
    private String name;


    // Empty constructor is needed
    public User() {

        // DO NOT REMOVE

    }

    public User(String id, String name){

        this.id = id;
        this.name = name;

    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){

        return id;

    }
}
