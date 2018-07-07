package b00080902.mabs2;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Items {

    public String item;
    public String value;
    public String date;

    public Items() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Items(String item, String value, String date) {
        this.item = item;
        this.value = value;
        this.date = date;
    }

}
