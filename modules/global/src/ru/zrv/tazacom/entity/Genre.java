package ru.zrv.tazacom.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import java.util.List;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Table(name = "TAZACOM_GENRE")
@NamePattern("%s|name")
@Entity(name = "tazacom$Genre")
public class Genre extends StandardEntity {
	
    private static final long serialVersionUID = -9046056643531324627L;

    @Column(name = "STATE")
    protected String state;

    @Column(name = "NAME", unique = true, length = 50)
    protected String name;

    @JoinTable(name = "TAZACOM_BOOK_GENRE_LINK",
        joinColumns = @JoinColumn(name = "GENRE_ID"),
        inverseJoinColumns = @JoinColumn(name = "BOOK_ID"))
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @OnDelete(DeletePolicy.UNLINK)
    @ManyToMany(cascade=CascadeType.MERGE)
    protected List<Book> books;

    public StateEnum getState() {
        return state == null ? null : StateEnum.fromId(state);
    }

    public void setState(StateEnum state) {
        this.state = state == null ? null : state.getId();
    }


    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<Book> getBooks() {
        return books;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}