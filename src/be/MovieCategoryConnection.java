package be;

public class MovieCategoryConnection {
    private long id;
    private long movieID;
    private long categoryID;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMovieID(long movieID) {
        this.movieID = movieID;
    }

    public long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(long categoryID) {
        this.categoryID = categoryID;
    }
}
