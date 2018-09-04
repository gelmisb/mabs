package b00080902.mabs2;


import java.io.Serializable;

/**
 *  This a class to define the model for MVC
 *
 *  It is made serializable in order to
 *  make sure the information can be overwritten
 *
 *
 */
public class Article implements Serializable{


    private String item;
    private String value;
    private String date;
    private String category;

    // Empty constructor is needed
    public Article() {

        // DO NOT REMOVE

    }



    public Article(String item, String value, String date, String category){

        this.item = item;
        this.value = value;
        this.date = date;
        this.category = category;

    }


    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString(){

        return item;

    }
}
