package b00080902.mabs2;


import java.io.Serializable;

public class Article implements Serializable{

    private String item;
    private String value;
    private String date;

    public Article() {

    }


    public Article(String item, String value, String date){

        this.item = item;
        this.value = value;
        this.date = date;

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

    @Override
    public  String toString(){

        return item;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
