package ru.zrv.tazacom.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.annotation.Lookup;
import com.haulmont.cuba.core.entity.annotation.LookupType;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import java.util.List;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@NamePattern("%s %s %s %s|author,name,year,edition")
@Table(name = "TAZACOM_BOOK")
@Entity(name = "tazacom$Book")
public class Book extends StandardEntity {
    private static final long serialVersionUID = 8889800189252119433L;

    @Column(name = "STATE")
    protected String state;

    @Lookup(type = LookupType.DROPDOWN, actions = {"lookup", "open", "clear"})
    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "AUTHOR_ID")
    protected Author author;

    @Column(name = "NAME", length = 50)
    protected String name;

    @Column(name = "YEAR_")
    protected Integer year;

    @Column(name = "EDITION")
    protected String edition;

    @JoinTable(name = "TAZACOM_BOOK_GENRE_LINK",
        joinColumns = @JoinColumn(name = "BOOK_ID"),
        inverseJoinColumns = @JoinColumn(name = "GENRE_ID"))
    @ManyToMany(cascade=CascadeType.MERGE)
    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.CASCADE)
    protected List<Genre> genre;




    public StateEnum getState() {
        return state == null ? null : StateEnum.fromId(state);
    }

    public void setState(StateEnum state) {
        this.state = state == null ? null : state.getId();
    }


    public List<Genre> getGenre() {
        return genre;
    }

    public void setGenre(List<Genre> genres) {
        this.genre = genres;
    }



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getEdition() {
        return edition;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Author getAuthor() {
        return author;
    }


}