
public class Card {
    // We can track the card by your id
    private final String id;
    private final String name;

    // protected --> constructor can be called by this class or a son
    protected Card(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

}