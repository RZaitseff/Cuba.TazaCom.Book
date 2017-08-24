package ru.zrv.tazacom.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s %s %s|firstName,middleName,lastName")
@Table(name = "TAZACOM_AUTHOR")
@Entity(name = "tazacom$Author")
public class Author extends StandardEntity {
    private static final long serialVersionUID = 8736638148071497305L;

    @Column(name = "STATE")
    protected String state;

    @Column(name = "FIRST_NAME", length = 50)
    protected String firstName;

    @Column(name = "MIDDLE_NAME", length = 50)
    protected String middleName;

    @Column(name = "LAST_NAME", length = 50)
    protected String lastName;
    
    
    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "author")
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


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }


}